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

import pytest
from pydantic import SecretStr, ValidationError
from starlette.applications import Starlette
from starlette.responses import JSONResponse
from starlette.routing import Route
from starlette.testclient import TestClient

import amoro_mcp_server.auth as auth_module
from amoro_mcp_server.auth import (
    AuthIdentity,
    CachedAuthProvider,
    ContextTokenVerifier,
    InboundAuthenticationMiddleware,
    build_auth_runtime,
)
from amoro_mcp_server.client import AmoroClient
from amoro_mcp_server.config import (
    AmoroSettings,
    InboundAuthSettings,
    Settings,
)
from amoro_mcp_server.request_context import (
    RequestAuthentication,
    reset_request_authentication,
    set_request_authentication,
)


class _CountingProvider(CachedAuthProvider[str]):
    def __init__(self, result: AuthIdentity | None) -> None:
        super().__init__()
        self.result = result
        self.calls = 0

    def _cache_key(self, credential: str) -> str:
        return credential

    async def _validate(self, credential: str) -> AuthIdentity | None:
        self.calls += 1
        await asyncio.sleep(0)
        return self.result


def test_auth_modes_are_strictly_validated(monkeypatch: pytest.MonkeyPatch) -> None:
    assert InboundAuthSettings(mode="none").mode == "none"
    assert InboundAuthSettings(mode="ams-api-key").mode == "ams-api-key"

    with pytest.raises(ValidationError):
        InboundAuthSettings.model_validate({"mode": "amoro"})

    with pytest.raises(ValidationError):
        InboundAuthSettings.model_validate({"mode": "udp"})
    with pytest.raises(ValidationError):
        InboundAuthSettings.model_validate(
            {"mode": "none", "udp": {"user_info_url": "https://udp.test/user"}}
        )
    with pytest.raises(ValidationError):
        InboundAuthSettings.model_validate(
            {"mode": "none", "api_keys": {"legacy": {"sha256": "0" * 64}}}
        )
    with pytest.raises(ValidationError):
        InboundAuthSettings.model_validate({"mode": "none", "allow_unauthenticated": True})

    monkeypatch.setenv("AMORO_MCP_AUTH__MODE", "udp")
    with pytest.raises(ValidationError):
        Settings()  # type: ignore[call-arg]

    monkeypatch.setenv("AMORO_MCP_AUTH__MODE", "none")
    monkeypatch.setenv("AMORO_MCP_AUTH__API_KEYS", "{}")
    with pytest.raises(ValidationError):
        Settings()  # type: ignore[call-arg]


def test_amoro_mode_rejects_server_wide_upstream_credentials() -> None:
    with pytest.raises(ValidationError, match="request credentials"):
        Settings(
            auth=InboundAuthSettings(mode="ams-api-key"),
            amoro=AmoroSettings(
                base_url="https://amoro.test",
                auth_type="token",
                api_key="server-key",
                api_secret=SecretStr("server-secret"),
            ),
        )

    with pytest.raises(ValidationError, match="IANA timezone"):
        AmoroSettings(signature_timezone="Not/A-Timezone")


@pytest.mark.asyncio
async def test_context_verifier_exposes_identity_without_client_secret() -> None:
    request_auth = RequestAuthentication(
        internal_token="internal-only-token",  # noqa: S106 - synthetic test value
        client_id="amoro:client-key",
        subject="client-key",
        claims={"auth_method": "amoro", "provider": "amoro"},
    )
    context_token = set_request_authentication(request_auth)
    try:
        verifier = ContextTokenVerifier()
        access = await verifier.verify_token("internal-only-token")
        assert access is not None
        assert access.client_id == "amoro:client-key"
        assert access.subject == "client-key"
        assert access.scopes == []
        assert await verifier.verify_token("wrong") is None
    finally:
        reset_request_authentication(context_token)


@pytest.mark.asyncio
async def test_cached_provider_coalesces_concurrent_checks_and_expires_results(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    clock = [0.0]
    monkeypatch.setattr(auth_module.time, "monotonic", lambda: clock[0])
    identity = AuthIdentity("amoro:key", "key", {"auth_method": "amoro"})
    positive = _CountingProvider(identity)
    results = await asyncio.gather(*(positive.authenticate("same") for _ in range(10)))
    assert results == [identity] * 10
    assert positive.calls == 1
    assert await positive.authenticate("same") == identity
    assert positive.calls == 1
    clock[0] = 301.0
    assert await positive.authenticate("same") == identity
    assert positive.calls == 2

    negative = _CountingProvider(None)
    assert await negative.authenticate("bad") is None
    assert await negative.authenticate("bad") is None
    assert negative.calls == 1
    clock[0] += 6.0
    assert await negative.authenticate("bad") is None
    assert negative.calls == 2


def test_none_mode_rejects_misleading_authentication_headers() -> None:
    async def ok(_request: object) -> JSONResponse:
        return JSONResponse({"ok": True})

    client = AmoroClient(AmoroSettings(base_url="https://amoro.test"), 1024)
    runtime = build_auth_runtime(InboundAuthSettings(mode="none"), client)
    app = InboundAuthenticationMiddleware(
        Starlette(routes=[Route("/mcp", ok)]),
        runtime,
    )
    with TestClient(app) as test_client:
        assert test_client.get("/mcp").status_code == 200
        assert test_client.get("/mcp", headers={"X-API-Key": "ignored"}).status_code == 400
        assert test_client.get("/mcp", headers={"Authorization": "Bearer token"}).status_code == 400
        assert test_client.get("/health/live", headers={"X-API-Key": "ignored"}).status_code == 404
