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
import hashlib
from datetime import UTC, datetime

import httpx
import pytest
from fastmcp.exceptions import ToolError
from pydantic import SecretStr

from amoro_mcp_server.client import AmoroClient, calculate_amoro_signature
from amoro_mcp_server.config import AmoroSettings
from amoro_mcp_server.request_context import (
    AmoroRequestCredentials,
    RequestAuthentication,
    reset_request_authentication,
    set_request_authentication,
)


def test_signature_matches_ams_parameter_rules() -> None:
    params = [("page", "2"), ("actions[]", "b"), ("actions[]", "a"), ("blank", "")]
    expected = hashlib.md5(  # noqa: S324
        b"keyactions[]apage2secret", usedforsecurity=False
    ).hexdigest()
    assert calculate_amoro_signature("key", "secret", params) == expected

    expected_date = hashlib.md5(b"key20260710secret", usedforsecurity=False).hexdigest()  # noqa: S324
    assert (
        calculate_amoro_signature(
            "key",
            "secret",
            [],
            now=datetime(2026, 7, 9, 23, 0, tzinfo=UTC),
            timezone_name="Asia/Shanghai",
        )
        == expected_date
    )


@pytest.mark.asyncio
async def test_token_auth_is_added_and_response_envelope_is_unwrapped() -> None:
    captured_url: httpx.URL | None = None

    async def handler(request: httpx.Request) -> httpx.Response:
        nonlocal captured_url
        captured_url = request.url
        return httpx.Response(200, json={"code": 200, "message": "success", "result": ["db"]})

    settings = AmoroSettings(
        base_url="https://amoro.test",
        auth_type="token",
        api_key="key",
        api_secret=SecretStr("secret"),
    )
    client = AmoroClient(settings, 1024, transport=httpx.MockTransport(handler))
    async with client:
        assert await client.get_json("/api/ams/v1/catalogs/c/databases", []) == ["db"]

    assert captured_url is not None
    assert captured_url.params["apiKey"] == "key"
    assert len(captured_url.params["signature"]) == 32


@pytest.mark.asyncio
async def test_basic_auth_error_envelope_and_retry() -> None:
    attempts = 0

    async def retrying_handler(request: httpx.Request) -> httpx.Response:
        nonlocal attempts
        attempts += 1
        assert request.headers["authorization"].startswith("Basic ")
        if attempts == 1:
            return httpx.Response(503, json={"code": 503, "message": "busy"})
        return httpx.Response(200, json={"code": 403, "message": "denied"})

    settings = AmoroSettings(
        base_url="https://amoro.test",
        auth_type="basic",
        username="admin",
        password=SecretStr("password"),
        max_retries=1,
    )
    client = AmoroClient(settings, 1024, transport=httpx.MockTransport(retrying_handler))
    async with client:
        with pytest.raises(ToolError, match="AMS error 403: denied"):
            await client.get_json("/api/ams/v1/catalogs", [])
    assert attempts == 2


@pytest.mark.asyncio
async def test_response_size_limit_is_enforced() -> None:
    transport = httpx.MockTransport(
        lambda _request: httpx.Response(
            200,
            content=b'{"code":200,"result":"' + b"x" * 1024 + b'"}',
        )
    )
    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        128,
        transport=transport,
    )
    async with client:
        with pytest.raises(ToolError, match="size limit"):
            await client.get_json("/api/ams/v1/catalogs", [])


@pytest.mark.asyncio
async def test_request_scoped_credentials_are_isolated_across_concurrent_calls() -> None:
    captured: list[tuple[str, str]] = []

    async def handler(request: httpx.Request) -> httpx.Response:
        api_key = request.url.params["apiKey"]
        signature = request.url.params["signature"]
        secret = {"client-a": "secret-a", "client-b": "secret-b"}[api_key]
        assert signature == calculate_amoro_signature(api_key, secret, [])
        assert secret not in str(request.url)
        captured.append((api_key, signature))
        return httpx.Response(200, json={"code": 200, "result": api_key})

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        require_request_credentials=True,
        transport=httpx.MockTransport(handler),
    )

    async def call(api_key: str, api_secret: str) -> str:
        context_token = set_request_authentication(
            RequestAuthentication(
                internal_token=f"internal-{api_key}",
                client_id=f"amoro:{api_key}",
                subject=api_key,
                claims={"auth_method": "amoro"},
                amoro_credentials=AmoroRequestCredentials(api_key, api_secret),
            )
        )
        try:
            return await client.get_json("/api/ams/v1/catalogs", [])
        finally:
            reset_request_authentication(context_token)

    async with client:
        results = await asyncio.gather(
            call("client-a", "secret-a"),
            call("client-b", "secret-b"),
        )
    assert results == ["client-a", "client-b"]
    assert {item[0] for item in captured} == {"client-a", "client-b"}


@pytest.mark.asyncio
async def test_amoro_mode_never_falls_back_to_server_credentials() -> None:
    settings = AmoroSettings(
        base_url="https://amoro.test",
        auth_type="token",
        api_key="server-key",
        api_secret=SecretStr("server-secret"),
    )
    client = AmoroClient(
        settings,
        1024,
        require_request_credentials=True,
        transport=httpx.MockTransport(
            lambda _request: httpx.Response(200, json={"code": 200, "result": []})
        ),
    )
    async with client:
        with pytest.raises(ToolError, match="Request-scoped AMS credentials"):
            await client.get_json("/api/ams/v1/catalogs", [])


@pytest.mark.asyncio
async def test_amoro_credentials_are_validated_with_protected_catalog_get() -> None:
    requests: list[httpx.Request] = []

    def handler(request: httpx.Request) -> httpx.Response:
        requests.append(request)
        if request.url.params.get("apiKey") != "client-key":
            return httpx.Response(200, json={"code": 403, "message": "denied"})
        expected = calculate_amoro_signature("client-key", "client-secret", [])
        if request.url.params.get("signature") != expected:
            return httpx.Response(200, json={"code": 403, "message": "denied"})
        return httpx.Response(200, json={"code": 200, "result": ["catalog"]})

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test"),
        1024,
        require_request_credentials=True,
        transport=httpx.MockTransport(handler),
    )
    async with client:
        assert await client.validate_credentials(
            AmoroRequestCredentials("client-key", "client-secret")
        )
        assert not await client.validate_credentials(
            AmoroRequestCredentials("client-key", "wrong-secret")
        )
    assert all(request.method == "GET" for request in requests)
    assert all(request.url.path == "/api/ams/v1/catalogs" for request in requests)
    assert all("client-secret" not in str(request.url) for request in requests)


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("response", "expected"),
    [
        (
            httpx.Response(200, json={"code": 200, "result": {"status": "ok"}}),
            {"reachable": True, "amsHealth": "healthy", "failureClass": None},
        ),
        (
            httpx.Response(403),
            {
                "reachable": True,
                "amsHealth": "unknown",
                "failureClass": "auth",
                "httpStatus": 403,
            },
        ),
        (
            httpx.Response(503),
            {
                "reachable": True,
                "amsHealth": "unhealthy",
                "failureClass": "server",
                "httpStatus": 503,
            },
        ),
    ],
)
async def test_check_connectivity_returns_sanitized_classification(
    response: httpx.Response,
    expected: dict[str, object],
) -> None:
    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test", max_retries=0),
        1024,
        transport=httpx.MockTransport(lambda _request: response),
    )
    async with client:
        assert await client.check_connectivity() == expected


@pytest.mark.asyncio
async def test_check_connectivity_classifies_network_failure_without_details() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        raise httpx.ConnectError("secret internal hostname", request=request)

    client = AmoroClient(
        AmoroSettings(base_url="https://amoro.test", max_retries=0),
        1024,
        transport=httpx.MockTransport(handler),
    )
    async with client:
        assert await client.check_connectivity() == {
            "reachable": False,
            "amsHealth": "unknown",
            "failureClass": "network",
        }
