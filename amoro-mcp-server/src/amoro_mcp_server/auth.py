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
import hmac
import secrets
import time
from abc import ABC, abstractmethod
from collections import OrderedDict
from dataclasses import dataclass
from typing import Any, TypeVar

from fastmcp.server.auth import AccessToken, TokenVerifier
from starlette.responses import JSONResponse
from starlette.types import ASGIApp, Receive, Scope, Send

from amoro_mcp_server.client import AmoroClient, AmoroCredentialValidationError
from amoro_mcp_server.config import InboundAuthSettings
from amoro_mcp_server.request_context import (
    AmoroRequestCredentials,
    RequestAuthentication,
    get_request_authentication,
    reset_request_authentication,
    set_request_authentication,
)

POSITIVE_CACHE_TTL_SECONDS = 300.0
NEGATIVE_CACHE_TTL_SECONDS = 5.0
AUTH_CACHE_MAX_ENTRIES = 10_000

_AUTHORIZATION = b"authorization"
_API_KEY = b"x-api-key"
_AMORO_API_KEY = b"x-amoro-api-key"
_AMORO_API_SECRET = b"x-amoro-api-secret"
_AUTH_HEADER_NAMES = {
    _AUTHORIZATION,
    _API_KEY,
    _AMORO_API_KEY,
    _AMORO_API_SECRET,
}


class AuthenticationProviderUnavailable(RuntimeError):
    """The configured identity provider could not make an authentication decision."""


@dataclass(frozen=True)
class AuthIdentity:
    client_id: str
    subject: str
    claims: dict[str, Any]


@dataclass(frozen=True)
class _CacheEntry:
    expires_at: float
    identity: AuthIdentity | None


class _AuthenticationCache:
    def __init__(self, max_entries: int = AUTH_CACHE_MAX_ENTRIES) -> None:
        self._max_entries = max_entries
        self._entries: OrderedDict[str, _CacheEntry] = OrderedDict()

    def get(self, key: str) -> tuple[bool, AuthIdentity | None]:
        entry = self._entries.get(key)
        if entry is None:
            return False, None
        if entry.expires_at <= time.monotonic():
            del self._entries[key]
            return False, None
        self._entries.move_to_end(key)
        return True, entry.identity

    def set(self, key: str, identity: AuthIdentity | None) -> None:
        ttl = POSITIVE_CACHE_TTL_SECONDS if identity is not None else NEGATIVE_CACHE_TTL_SECONDS
        self._entries[key] = _CacheEntry(time.monotonic() + ttl, identity)
        self._entries.move_to_end(key)
        while len(self._entries) > self._max_entries:
            self._entries.popitem(last=False)


CredentialT = TypeVar("CredentialT")


class CachedAuthProvider[CredentialT](ABC):
    """Cache authentication decisions by credential digest and coalesce concurrent checks."""

    def __init__(self) -> None:
        self._cache = _AuthenticationCache()
        self._lock = asyncio.Lock()
        self._inflight: dict[str, asyncio.Task[AuthIdentity | None]] = {}

    async def __aenter__(self) -> CachedAuthProvider[CredentialT]:
        return self

    async def __aexit__(self, *_args: object) -> None:
        return None

    async def authenticate(self, credential: CredentialT) -> AuthIdentity | None:
        cache_key = self._cache_key(credential)
        async with self._lock:
            found, cached = self._cache.get(cache_key)
            if found:
                return cached
            task = self._inflight.get(cache_key)
            if task is None:
                task = asyncio.create_task(self._validate_and_cache(cache_key, credential))
                self._inflight[cache_key] = task

        try:
            return await asyncio.shield(task)
        finally:
            if task.done():
                async with self._lock:
                    if self._inflight.get(cache_key) is task:
                        del self._inflight[cache_key]

    async def _validate_and_cache(
        self,
        cache_key: str,
        credential: CredentialT,
    ) -> AuthIdentity | None:
        identity = await self._validate(credential)
        async with self._lock:
            self._cache.set(cache_key, identity)
        return identity

    @abstractmethod
    def _cache_key(self, credential: CredentialT) -> str:
        raise NotImplementedError

    @abstractmethod
    async def _validate(self, credential: CredentialT) -> AuthIdentity | None:
        raise NotImplementedError


class AmoroAuthProvider(CachedAuthProvider[AmoroRequestCredentials]):
    def __init__(self, client: AmoroClient) -> None:
        super().__init__()
        self._client = client

    def _cache_key(self, credential: AmoroRequestCredentials) -> str:
        material = f"amoro\0{credential.api_key}\0{credential.api_secret}".encode()
        return hashlib.sha256(material).hexdigest()

    async def _validate(self, credential: AmoroRequestCredentials) -> AuthIdentity | None:
        try:
            valid = await self._client.validate_credentials(credential)
        except AmoroCredentialValidationError as exc:
            raise AuthenticationProviderUnavailable from exc
        if not valid:
            return None
        return AuthIdentity(
            client_id=f"amoro:{credential.api_key}",
            subject=credential.api_key,
            claims={"auth_method": "amoro", "provider": "amoro"},
        )


class ContextTokenVerifier(TokenVerifier):
    """Expose a request-local, non-secret token to FastMCP's authentication layer."""

    def __init__(self) -> None:
        super().__init__()

    async def verify_token(self, token: str) -> AccessToken | None:
        authentication = get_request_authentication()
        if authentication is None or not hmac.compare_digest(
            token,
            authentication.internal_token,
        ):
            return None
        return AccessToken(
            token=authentication.internal_token,
            client_id=authentication.client_id,
            subject=authentication.subject,
            scopes=[],
            claims=authentication.claims,
        )


class AuthenticationRuntime:
    def __init__(
        self,
        settings: InboundAuthSettings,
        provider: CachedAuthProvider[Any] | None,
    ) -> None:
        self.settings = settings
        self.provider = provider
        self.verifier = ContextTokenVerifier() if provider is not None else None

    async def __aenter__(self) -> AuthenticationRuntime:
        if self.provider is not None:
            await self.provider.__aenter__()
        return self

    async def __aexit__(self, *_args: object) -> None:
        if self.provider is not None:
            await self.provider.__aexit__(*_args)

    async def authenticate(self, credential: object) -> AuthIdentity | None:
        if self.provider is None:
            raise RuntimeError("authentication provider is not configured")
        return await self.provider.authenticate(credential)


def build_auth_runtime(
    settings: InboundAuthSettings,
    amoro_client: AmoroClient,
) -> AuthenticationRuntime:
    if settings.mode == "none":
        provider: CachedAuthProvider[Any] | None = None
    elif settings.mode == "ams-api-key":
        provider = AmoroAuthProvider(amoro_client)
    else:
        provider = AmoroAuthProvider(amoro_client)
    return AuthenticationRuntime(settings, provider)


class InboundAuthenticationMiddleware:
    """Validate configured credential headers before requests reach FastMCP."""

    def __init__(self, app: ASGIApp, runtime: AuthenticationRuntime) -> None:
        self.app = app
        self.runtime = runtime

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http" or str(scope.get("path", "")).startswith("/health/"):
            await self.app(scope, receive, send)
            return

        header_values = self._authentication_headers(scope)
        if header_values is None:
            await self._respond(
                scope,
                receive,
                send,
                400,
                "duplicate_authentication_header",
                "Authentication headers must not be repeated",
            )
            return
        if header_values[_AUTHORIZATION]:
            await self._respond(
                scope,
                receive,
                send,
                400,
                "unsupported_authorization",
                "Authorization is not supported for the configured authentication mode",
            )
            return

        mode = self.runtime.settings.mode
        if mode == "none":
            if any(header_values[name] for name in _AUTH_HEADER_NAMES - {_AUTHORIZATION}):
                await self._respond(
                    scope,
                    receive,
                    send,
                    400,
                    "unexpected_authentication_header",
                    "Authentication headers are not accepted in none mode",
                )
                return
            await self.app(scope, receive, send)
            return

        if header_values[_API_KEY]:
            await self._mixed_headers(scope, receive, send)
            return
        api_key = header_values[_AMORO_API_KEY]
        api_secret = header_values[_AMORO_API_SECRET]
        if bool(api_key) != bool(api_secret):
            await self._respond(
                scope,
                receive,
                send,
                400,
                "incomplete_amoro_credentials",
                "Both X-Amoro-API-Key and X-Amoro-API-Secret are required",
            )
            return
        if not api_key or not api_secret:
            await self._unauthorized(scope, receive, send)
            return
        amoro_credentials = AmoroRequestCredentials(api_key=api_key, api_secret=api_secret)

        try:
            identity = await self.runtime.authenticate(amoro_credentials)
        except AuthenticationProviderUnavailable:
            await self._respond(
                scope,
                receive,
                send,
                503,
                "authentication_unavailable",
                "The authentication provider is temporarily unavailable",
            )
            return
        if identity is None:
            await self._unauthorized(scope, receive, send)
            return

        internal_token = secrets.token_urlsafe(32)
        authentication = RequestAuthentication(
            internal_token=internal_token,
            client_id=identity.client_id,
            subject=identity.subject,
            claims=identity.claims,
            amoro_credentials=amoro_credentials,
        )
        context_token = set_request_authentication(authentication)
        rewritten = dict(scope)
        rewritten["headers"] = [
            (name, value)
            for name, value in scope.get("headers", [])
            if name.lower() not in _AUTH_HEADER_NAMES
        ]
        rewritten["headers"].append((_AUTHORIZATION, f"Bearer {internal_token}".encode("latin-1")))
        try:
            await self.app(rewritten, receive, send)
        finally:
            reset_request_authentication(context_token)

    @staticmethod
    def _authentication_headers(scope: Scope) -> dict[bytes, str] | None:
        found: dict[bytes, list[str]] = {name: [] for name in _AUTH_HEADER_NAMES}
        for name, value in scope.get("headers", []):
            normalized = name.lower()
            if normalized in found:
                found[normalized].append(value.decode("latin-1"))
        if any(len(values) > 1 for values in found.values()):
            return None
        return {name: values[0] if values else "" for name, values in found.items()}

    async def _mixed_headers(self, scope: Scope, receive: Receive, send: Send) -> None:
        await self._respond(
            scope,
            receive,
            send,
            400,
            "mixed_authentication_headers",
            "Credential headers for different authentication modes must not be mixed",
        )

    async def _unauthorized(self, scope: Scope, receive: Receive, send: Send) -> None:
        await self._respond(
            scope,
            receive,
            send,
            401,
            "invalid_token",
            "Authentication required",
        )

    @staticmethod
    async def _respond(
        scope: Scope,
        receive: Receive,
        send: Send,
        status_code: int,
        error: str,
        description: str,
    ) -> None:
        response = JSONResponse(
            {"error": error, "error_description": description},
            status_code=status_code,
        )
        await response(scope, receive, send)
