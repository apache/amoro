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

import httpx
import pytest
from fastmcp import Client
from fastmcp.exceptions import ToolError
from starlette.testclient import TestClient

from amoro_mcp_server.client import calculate_amoro_signature
from amoro_mcp_server.config import InboundAuthSettings, Settings
from amoro_mcp_server.server import build_server, create_app
from amoro_mcp_server.tools import TOOL_REGISTRARS

EXPECTED_TOOLS = {
    "list_catalogs",
    "list_databases",
    "list_tables",
    "get_table_details",
    "list_table_snapshots",
    "list_optimizing_processes",
    "list_optimizing_tasks",
    "list_optimizers",
    "list_optimizing_tables",
    "list_resource_groups",
    "check_ams_connectivity",
}


@pytest.mark.asyncio
async def test_server_lists_promoted_read_only_tools_and_returns_structured_output(
    settings: Settings,
) -> None:
    assert len(TOOL_REGISTRARS) == len(EXPECTED_TOOLS)

    async def handler(request: httpx.Request) -> httpx.Response:
        if request.url.path.endswith("/versionInfo"):
            result = {"version": "0.9.0-SNAPSHOT(commit-must-not-leak)", "commitTime": "secret"}
        elif request.url.path.endswith("/tasks"):
            result = {
                "list": [
                    {
                        "taskId": 7,
                        "processId": "p1",
                        "status": "SUCCESS",
                        "optimizerToken": "never-return",
                        "summary": {"optimizer.token": "never-return", "retry-count": "0"},
                    }
                ],
                "total": 1,
            }
        elif request.url.path.endswith("/snapshots"):
            result = {
                "list": [
                    {
                        "snapshotId": "123",
                        "commitTime": 1_735_689_600_000,
                        "operation": "replace",
                        "producer": "OPTIMIZE",
                        "records": 100,
                        "fileCount": 4,
                        "originalFileSize": 4096,
                        "summary": {
                            "total-records": "100",
                            "new-iceberg-metric": "7",
                            "apiSecret": "never-return",
                        },
                    }
                ],
                "total": 1,
            }
        else:
            result = []
        return httpx.Response(200, json={"code": 200, "message": "success", "result": result})

    mcp = build_server(settings, transport=httpx.MockTransport(handler))
    async with Client(mcp) as client:
        tools = await client.list_tools()
        assert {tool.name for tool in tools} == EXPECTED_TOOLS
        for tool in tools:
            assert tool.annotations is not None
            assert tool.annotations.readOnlyHint is True
            assert tool.annotations.destructiveHint is False
            assert tool.annotations.idempotentHint is True
            assert tool.annotations.openWorldHint is True

        result = await client.call_tool(
            "list_optimizing_tasks",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "process_id": "p1",
            },
        )
        assert result.structured_content is not None
        task = result.structured_content["data"]["list"][0]
        assert task["taskId"] == 7
        assert "optimizerToken" not in task
        assert "optimizer.token" not in task["summary"]
        assert result.content

        snapshot_result = await client.call_tool(
            "list_table_snapshots",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "commit_type": "optimizing",
            },
        )
        assert snapshot_result.structured_content is not None
        snapshot = snapshot_result.structured_content["data"]["list"][0]
        assert snapshot["producer"] == "OPTIMIZE"
        assert snapshot["records"] == 100
        assert snapshot["fileCount"] == 4
        assert snapshot["originalFileSize"] == 4096
        assert snapshot["summary"]["new-iceberg-metric"] == "7"
        assert "apiSecret" not in snapshot["summary"]


@pytest.mark.asyncio
async def test_unsupported_ams_version_blocks_tool_execution(settings: Settings) -> None:
    requested_paths: list[str] = []

    def handler(request: httpx.Request) -> httpx.Response:
        requested_paths.append(request.url.path)
        assert request.url.path == "/api/ams/v1/versionInfo"
        return httpx.Response(
            200,
            json={
                "code": 200,
                "result": {"version": "0.7.0-incubating(commit-must-not-leak)"},
            },
        )

    mcp = build_server(settings, transport=httpx.MockTransport(handler))
    async with Client(mcp) as client:
        with pytest.raises(ToolError, match=r"AMS version 0\.7\.0 is unsupported") as exc_info:
            await client.call_tool("list_catalogs", {})

    assert requested_paths == ["/api/ams/v1/versionInfo"]
    assert "commit-must-not-leak" not in str(exc_info.value)


INITIALIZE_REQUEST = {
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
        "protocolVersion": "2025-06-18",
        "capabilities": {},
        "clientInfo": {"name": "test", "version": "1.0"},
    },
}
MCP_HEADERS = {
    "Accept": "application/json, text/event-stream",
    "Content-Type": "application/json",
}


def test_http_app_none_mode_and_health(settings: Settings) -> None:
    app = create_app(settings, transport=httpx.MockTransport(lambda _request: httpx.Response(500)))
    with TestClient(app) as client:
        assert client.get("/health/live").status_code == 200
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers=MCP_HEADERS,
            ).status_code
            == 200
        )
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers={**MCP_HEADERS, "X-API-Key": "misleading"},
            ).status_code
            == 400
        )


@pytest.mark.parametrize(
    ("version", "expected_status", "expected_body"),
    [
        (
            "0.8.0-incubating(commit-must-not-leak)",
            200,
            {"status": "ready", "amsVersion": "0.8.0", "compatibility": "supported"},
        ),
        (
            "0.7.0-incubating(commit-must-not-leak)",
            503,
            {"status": "not_ready", "amsVersion": "0.7.0", "compatibility": "unsupported"},
        ),
        (
            "UNKNOWN(commit-must-not-leak)",
            200,
            {"status": "ready", "amsVersion": None, "compatibility": "unverified"},
        ),
    ],
)
def test_readiness_exposes_only_normalized_compatibility(
    settings: Settings,
    version: str,
    expected_status: int,
    expected_body: dict[str, object],
) -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if request.url.path.endswith("/health/status"):
            result: object = {"status": "ok"}
        else:
            assert request.url.path.endswith("/versionInfo")
            result = {"version": version, "commitTime": "must-not-leak"}
        return httpx.Response(200, json={"code": 200, "result": result})

    app = create_app(settings, transport=httpx.MockTransport(handler))
    with TestClient(app) as client:
        response = client.get("/health/ready")

    assert response.status_code == expected_status
    assert response.json() == expected_body
    assert "commit" not in response.text
    assert "must-not-leak" not in response.text


def test_http_app_amoro_credentials_authenticate_and_reject_mixed_headers(
    settings: Settings,
    capsys: object,
) -> None:
    api_key = "client-key"
    api_secret = "client-secret"  # noqa: S105 - synthetic test value

    def handler(request: httpx.Request) -> httpx.Response:
        expected = calculate_amoro_signature(api_key, api_secret, [])
        code = (
            200
            if request.url.params.get("apiKey") == api_key
            and request.url.params.get("signature") == expected
            else 403
        )
        result: object = []
        if request.url.path == "/api/ams/v1/versionInfo":
            result = {"version": "0.9.0-SNAPSHOT(commit-must-not-leak)"}
        else:
            assert request.url.path == "/api/ams/v1/catalogs"
        return httpx.Response(200, json={"code": code, "result": result})

    secured = settings.model_copy(update={"auth": InboundAuthSettings(mode="ams-api-key")})
    app = create_app(secured, transport=httpx.MockTransport(handler))
    with TestClient(app) as client:
        assert client.get("/health/live").status_code == 200
        assert client.post("/mcp", json=INITIALIZE_REQUEST, headers=MCP_HEADERS).status_code == 401
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers={**MCP_HEADERS, "X-Amoro-API-Key": api_key},
            ).status_code
            == 400
        )
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers={
                    **MCP_HEADERS,
                    "X-Amoro-API-Key": api_key,
                    "X-Amoro-API-Secret": "wrong-secret",
                },
            ).status_code
            == 401
        )
        authenticated = client.post(
            "/mcp",
            json=INITIALIZE_REQUEST,
            headers={
                **MCP_HEADERS,
                "X-Amoro-API-Key": api_key,
                "X-Amoro-API-Secret": api_secret,
            },
        )
        assert authenticated.status_code == 200
        tool_response = client.post(
            "/mcp",
            json={
                "jsonrpc": "2.0",
                "id": 2,
                "method": "tools/call",
                "params": {"name": "list_catalogs", "arguments": {}},
            },
            headers={
                **MCP_HEADERS,
                "X-Amoro-API-Key": api_key,
                "X-Amoro-API-Secret": api_secret,
            },
        )
        assert tool_response.status_code == 200
        assert tool_response.json()["result"]["isError"] is False
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers={
                    **MCP_HEADERS,
                    "X-API-Key": "udp-token",
                    "X-Amoro-API-Key": api_key,
                    "X-Amoro-API-Secret": api_secret,
                },
            ).status_code
            == 400
        )
        assert (
            client.post(
                "/mcp",
                json=INITIALIZE_REQUEST,
                headers={**MCP_HEADERS, "Authorization": "Bearer token"},
            ).status_code
            == 400
        )
    captured = capsys.readouterr()  # type: ignore[attr-defined]
    assert api_secret not in captured.err
    assert "wrong-secret" not in captured.err
