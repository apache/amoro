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

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any

import httpx
import uvicorn
from fastmcp import FastMCP
from fastmcp.server.middleware.logging import StructuredLoggingMiddleware
from starlette.responses import JSONResponse
from starlette.types import ASGIApp

from amoro_mcp_server import __version__
from amoro_mcp_server.actions import ActionService
from amoro_mcp_server.auth import (
    AuthenticationRuntime,
    InboundAuthenticationMiddleware,
    build_auth_runtime,
)
from amoro_mcp_server.client import AmoroClient
from amoro_mcp_server.config import Settings
from amoro_mcp_server.logging_config import configure_logging
from amoro_mcp_server.tools import register_tools


def _build_server_components(
    settings: Settings,
    *,
    transport: httpx.AsyncBaseTransport | None = None,
) -> tuple[FastMCP, AuthenticationRuntime]:
    protocol_logger = configure_logging(settings.log_level)
    client = AmoroClient(
        settings.amoro,
        settings.response_max_bytes,
        require_request_credentials=settings.auth.mode == "ams-api-key",
        transport=transport,
    )
    auth_runtime = build_auth_runtime(settings.auth, client)
    service = ActionService(client, settings.response_max_bytes)

    @asynccontextmanager
    async def lifespan(_server: FastMCP) -> AsyncIterator[None]:
        async with client, auth_runtime:
            yield

    mcp = FastMCP(
        name="Apache Amoro",
        version=__version__,
        auth=auth_runtime.verifier,
        lifespan=lifespan,
        middleware=[
            StructuredLoggingMiddleware(
                logger=protocol_logger,
                include_payloads=False,
                include_payload_length=False,
            )
        ],
        mask_error_details=True,
        strict_input_validation=True,
    )
    register_tools(mcp, service)

    @mcp.custom_route("/health/live", methods=["GET"])
    async def health_live(_request: Any) -> JSONResponse:
        return JSONResponse({"status": "ok", "service": "amoro-mcp-server"})

    @mcp.custom_route("/health/ready", methods=["GET"])
    async def health_ready(_request: Any) -> JSONResponse:
        readiness = await client.readiness()
        return JSONResponse(
            {
                "status": readiness.status,
                "amsVersion": readiness.ams_version,
                "compatibility": readiness.compatibility,
            },
            status_code=200 if readiness.ready else 503,
        )

    return mcp, auth_runtime


def build_server(
    settings: Settings,
    *,
    transport: httpx.AsyncBaseTransport | None = None,
) -> FastMCP:
    mcp, _auth_runtime = _build_server_components(settings, transport=transport)
    return mcp


def create_app(
    settings: Settings | None = None,
    *,
    transport: httpx.AsyncBaseTransport | None = None,
) -> ASGIApp:
    resolved = settings or Settings()  # type: ignore[call-arg]
    mcp, auth_runtime = _build_server_components(resolved, transport=transport)
    app = mcp.http_app(
        path="/mcp",
        stateless_http=True,
        json_response=True,
        host_origin_protection=True,
        allowed_hosts=resolved.allowed_hosts or ["127.0.0.1:*", "localhost:*"],
        allowed_origins=resolved.allowed_origins,
    )
    return InboundAuthenticationMiddleware(app, auth_runtime)


def main() -> None:
    settings = Settings()  # type: ignore[call-arg]
    uvicorn.run(
        "amoro_mcp_server.server:create_app",
        factory=True,
        host=settings.host,
        port=settings.port,
        log_level=settings.log_level.lower(),
        access_log=False,
    )


if __name__ == "__main__":
    main()
