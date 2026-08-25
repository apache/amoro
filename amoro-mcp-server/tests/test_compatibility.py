# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

import asyncio

import httpx
import pytest
from fastmcp.exceptions import ToolError

from amoro_mcp_server.client import AmoroClient
from amoro_mcp_server.compatibility import AmsCompatibility, classify_ams_version
from amoro_mcp_server.config import AmoroSettings


@pytest.mark.parametrize(
    ("raw_version", "expected"),
    [
        ("0.7.0-incubating(abcdef)", AmsCompatibility("0.7.0", "unsupported")),
        ("0.8.0-incubating(abcdef)", AmsCompatibility("0.8.0", "supported")),
        ("0.9.0-SNAPSHOT(abcdef)", AmsCompatibility("0.9.0", "supported")),
        ("1.0.0", AmsCompatibility("1.0.0", "unverified")),
        ("UNKNOWN(commit)", AmsCompatibility(None, "unverified")),
        (None, AmsCompatibility(None, "unverified")),
    ],
)
def test_classify_ams_version_discards_build_and_commit_metadata(
    raw_version: object,
    expected: AmsCompatibility,
) -> None:
    assert classify_ams_version(raw_version) == expected


@pytest.mark.asyncio
async def test_compatibility_check_is_cached_and_concurrent_requests_are_coalesced() -> None:
    requests = 0

    async def handler(_request: httpx.Request) -> httpx.Response:
        nonlocal requests
        requests += 1
        await asyncio.sleep(0)
        return httpx.Response(
            200,
            json={
                "code": 200,
                "result": {"version": "0.9.0-SNAPSHOT(commit-must-not-leak)"},
            },
        )

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        transport=httpx.MockTransport(handler),
    )
    async with client:
        results = await asyncio.gather(*(client.get_compatibility() for _ in range(10)))
        assert results == [AmsCompatibility("0.9.0", "supported")] * 10
        assert requests == 1

        client._compatibility_expires_at = 0.0
        assert await client.get_compatibility() == AmsCompatibility("0.9.0", "supported")
        assert requests == 2


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("version", "expected_ready", "expected_status", "expected_version"),
    [
        ("0.7.0-incubating(commit)", False, "unsupported", "0.7.0"),
        ("0.8.0-incubating(commit)", True, "supported", "0.8.0"),
        ("0.9.0-SNAPSHOT(commit)", True, "supported", "0.9.0"),
        ("1.0.0", True, "unverified", "1.0.0"),
        ("UNKNOWN(commit)", True, "unverified", None),
    ],
)
async def test_readiness_combines_health_and_normalized_version(
    version: str,
    expected_ready: bool,
    expected_status: str,
    expected_version: str | None,
) -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if request.url.path.endswith("/health/status"):
            return httpx.Response(200, json={"code": 200, "result": {"status": "ok"}})
        return httpx.Response(
            200,
            json={"code": 200, "result": {"version": version, "commitTime": "private"}},
        )

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        transport=httpx.MockTransport(handler),
    )
    async with client:
        readiness = await client.readiness()

    assert readiness.ready is expected_ready
    assert readiness.status == ("ready" if expected_ready else "not_ready")
    assert readiness.compatibility == expected_status
    assert readiness.ams_version == expected_version
    assert "commit" not in repr(readiness)
    assert "private" not in repr(readiness)


@pytest.mark.asyncio
async def test_readiness_is_not_ready_when_ams_is_unreachable() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        raise httpx.ConnectError("private upstream address", request=request)

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        transport=httpx.MockTransport(handler),
    )
    async with client:
        readiness = await client.readiness()

    assert readiness.ready is False
    assert readiness.status == "not_ready"
    assert readiness.ams_version is None
    assert readiness.compatibility == "unverified"


@pytest.mark.asyncio
async def test_unsupported_version_blocks_tool_requests() -> None:
    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        transport=httpx.MockTransport(
            lambda _request: httpx.Response(
                200,
                json={"code": 200, "result": {"version": "0.7.0-incubating(secret-commit)"}},
            )
        ),
    )
    async with client:
        with pytest.raises(ToolError, match=r"AMS version 0\.7\.0 is unsupported") as exc_info:
            await client.ensure_compatible()

    assert "secret-commit" not in str(exc_info.value)
