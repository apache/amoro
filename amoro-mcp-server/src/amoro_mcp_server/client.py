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
import json
import logging
import ssl
import time
from dataclasses import dataclass
from datetime import datetime
from typing import Any
from urllib.parse import unquote_plus
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

import httpx
from fastmcp.exceptions import ToolError

from amoro_mcp_server.compatibility import AmsCompatibility, classify_ams_version
from amoro_mcp_server.config import AmoroSettings
from amoro_mcp_server.request_context import (
    AmoroRequestCredentials,
    get_amoro_request_credentials,
)

RETRYABLE_STATUS_CODES = {429, 502, 503, 504}
COMPATIBILITY_CACHE_TTL_SECONDS = 300.0
LOGGER = logging.getLogger(__name__)


@dataclass(frozen=True)
class Readiness:
    ready: bool
    status: str
    ams_version: str | None
    compatibility: str


class ResponseTooLargeError(RuntimeError):
    pass


class AmoroCredentialValidationError(RuntimeError):
    """Raised when AMS cannot determine whether request credentials are valid."""


def calculate_amoro_signature(
    api_key: str,
    api_secret: str,
    params: list[tuple[str, str]],
    *,
    now: datetime | None = None,
    timezone_name: str = "UTC",
) -> str:
    """Replicate AMS ParamSignatureCalculator for query authentication."""

    grouped: dict[str, list[str]] = {}
    for key, value in params:
        if key in {"apiKey", "signature"}:
            continue
        grouped.setdefault(key, []).append(value)

    components: list[str] = []
    for key in sorted(grouped):
        values = sorted(value for value in grouped[key] if value and not value.isspace())
        if values:
            components.append(f"{key}{unquote_plus(values[0])}")

    if components:
        encrypted_string = "".join(components)
    else:
        try:
            timezone = ZoneInfo(timezone_name)
        except ZoneInfoNotFoundError as exc:
            raise ValueError(f"unknown signature timezone: {timezone_name}") from exc
        current = now.astimezone(timezone) if now else datetime.now(timezone)
        encrypted_string = current.strftime("%Y%m%d")

    plain_text = f"{api_key}{encrypted_string}{api_secret}"
    return hashlib.md5(plain_text.encode("utf-8"), usedforsecurity=False).hexdigest()  # noqa: S324


class AmoroClient:
    """Bounded, retrying, HTTP client for AMS."""

    def __init__(
        self,
        settings: AmoroSettings,
        response_max_bytes: int,
        *,
        require_request_credentials: bool = False,
        transport: httpx.AsyncBaseTransport | None = None,
    ) -> None:
        self.settings = settings
        self.response_max_bytes = response_max_bytes
        self.require_request_credentials = require_request_credentials
        self._transport = transport
        self._client: httpx.AsyncClient | None = None
        self._compatibility: AmsCompatibility | None = None
        self._compatibility_expires_at = 0.0
        self._compatibility_lock = asyncio.Lock()

    async def __aenter__(self) -> AmoroClient:
        verify: bool | ssl.SSLContext = self.settings.tls_verify
        if self.settings.tls_ca_file:
            verify = ssl.create_default_context(cafile=self.settings.tls_ca_file)
        timeout = httpx.Timeout(
            connect=self.settings.connect_timeout_seconds,
            read=self.settings.read_timeout_seconds,
            write=self.settings.read_timeout_seconds,
            pool=self.settings.connect_timeout_seconds,
        )
        auth: httpx.Auth | None = None
        if self.settings.auth_type == "basic":
            assert self.settings.username is not None and self.settings.password is not None
            auth = httpx.BasicAuth(
                self.settings.username,
                self.settings.password.get_secret_value(),
            )
        self._client = httpx.AsyncClient(
            base_url=self.settings.base_url,
            timeout=timeout,
            follow_redirects=False,
            verify=verify,
            auth=auth,
            transport=self._transport,
            limits=httpx.Limits(max_connections=100, max_keepalive_connections=20),
        )
        return self

    async def __aexit__(self, *_args: object) -> None:
        if self._client is not None:
            await self._client.aclose()
            self._client = None

    def _authenticated_params(
        self,
        params: list[tuple[str, str]],
        *,
        credentials: AmoroRequestCredentials | None = None,
        allow_without_request_credentials: bool = False,
    ) -> list[tuple[str, str]]:
        resolved = credentials or get_amoro_request_credentials()
        if resolved is not None:
            signature = calculate_amoro_signature(
                resolved.api_key,
                resolved.api_secret,
                params,
                timezone_name=self.settings.signature_timezone,
            )
            return [*params, ("apiKey", resolved.api_key), ("signature", signature)]

        if self.require_request_credentials and not allow_without_request_credentials:
            raise ToolError("Request-scoped AMS credentials are required")

        if self.settings.auth_type != "token":
            return params
        assert self.settings.api_key is not None and self.settings.api_secret is not None
        signature = calculate_amoro_signature(
            self.settings.api_key,
            self.settings.api_secret.get_secret_value(),
            params,
            timezone_name=self.settings.signature_timezone,
        )
        return [*params, ("apiKey", self.settings.api_key), ("signature", signature)]

    async def _request_once(
        self,
        path: str,
        params: list[tuple[str, str]],
        *,
        credentials: AmoroRequestCredentials | None = None,
        allow_without_request_credentials: bool = False,
        request_timeout: httpx.Timeout | None = None,
    ) -> tuple[int, bytes, dict[str, str]]:
        if self._client is None:
            raise RuntimeError("AmoroClient must be used as an async context manager")
        authenticated = self._authenticated_params(
            params,
            credentials=credentials,
            allow_without_request_credentials=allow_without_request_credentials,
        )
        query_values: list[tuple[str, str | int | float | bool | None]] = list(authenticated)
        query = httpx.QueryParams(query_values)
        async with self._client.stream(
            "GET", path, params=query, timeout=request_timeout
        ) as response:
            content_length = response.headers.get("content-length")
            if content_length:
                try:
                    if int(content_length) > self.response_max_bytes:
                        raise ResponseTooLargeError(
                            "AMS response exceeds the configured size limit"
                        )
                except ValueError:
                    pass
            chunks: list[bytes] = []
            size = 0
            async for chunk in response.aiter_bytes():
                size += len(chunk)
                if size > self.response_max_bytes:
                    raise ResponseTooLargeError("AMS response exceeds the configured size limit")
                chunks.append(chunk)
            return response.status_code, b"".join(chunks), dict(response.headers)

    async def get_json(self, path: str, params: list[tuple[str, str]]) -> Any:
        last_error: Exception | None = None
        for attempt in range(self.settings.max_retries + 1):
            try:
                status_code, body, headers = await self._request_once(path, params)
                if status_code in RETRYABLE_STATUS_CODES and attempt < self.settings.max_retries:
                    retry_after = headers.get("retry-after")
                    delay = 0.25 * (2**attempt)
                    if retry_after:
                        try:
                            delay = min(max(float(retry_after), 0.0), 5.0)
                        except ValueError:
                            pass
                    await asyncio.sleep(delay)
                    continue
                if status_code < 200 or status_code >= 300:
                    raise ToolError(f"AMS request failed with HTTP status {status_code}")
                try:
                    payload = json.loads(body)
                except (UnicodeDecodeError, json.JSONDecodeError) as exc:
                    raise ToolError("AMS returned an invalid JSON response") from exc
                if not isinstance(payload, dict):
                    raise ToolError("AMS returned an unexpected response envelope")
                code = payload.get("code")
                if code != 200:
                    message = str(payload.get("message") or "AMS request failed")[:500]
                    raise ToolError(f"AMS error {code}: {message}")
                return payload.get("result")
            except (httpx.TransportError, httpx.TimeoutException) as exc:
                last_error = exc
                if attempt >= self.settings.max_retries:
                    break
                await asyncio.sleep(0.25 * (2**attempt))
            except ResponseTooLargeError as exc:
                raise ToolError(str(exc)) from exc
        raise ToolError("AMS request failed after retries") from last_error

    async def validate_credentials(self, credentials: AmoroRequestCredentials) -> bool:
        """Validate client-owned credentials against a protected AMS GET endpoint."""

        timeout = httpx.Timeout(connect=2.0, read=5.0, write=5.0, pool=2.0)
        last_error: Exception | None = None
        for attempt in range(2):
            try:
                status_code, body, headers = await self._request_once(
                    "/api/ams/v1/catalogs",
                    [],
                    credentials=credentials,
                    request_timeout=timeout,
                )
                if (status_code == 429 or 500 <= status_code < 600) and attempt == 0:
                    retry_after = headers.get("retry-after")
                    delay = 0.25
                    if retry_after:
                        try:
                            delay = min(max(float(retry_after), 0.0), 5.0)
                        except ValueError:
                            pass
                    await asyncio.sleep(delay)
                    continue
                if status_code in {401, 403}:
                    return False
                if status_code < 200 or status_code >= 300:
                    raise AmoroCredentialValidationError(
                        f"AMS credential validation returned HTTP status {status_code}"
                    )
                try:
                    payload = json.loads(body)
                except (UnicodeDecodeError, json.JSONDecodeError) as exc:
                    raise AmoroCredentialValidationError(
                        "AMS credential validation returned invalid JSON"
                    ) from exc
                if not isinstance(payload, dict):
                    raise AmoroCredentialValidationError(
                        "AMS credential validation returned an unexpected envelope"
                    )
                code = payload.get("code")
                if code == 200:
                    return True
                if code in {401, 403}:
                    return False
                raise AmoroCredentialValidationError(
                    f"AMS credential validation returned error code {code}"
                )
            except (httpx.TransportError, httpx.TimeoutException) as exc:
                last_error = exc
                if attempt == 0:
                    await asyncio.sleep(0.25)
                    continue
            except ResponseTooLargeError as exc:
                raise AmoroCredentialValidationError(
                    "AMS credential validation response exceeded the size limit"
                ) from exc
            except AmoroCredentialValidationError:
                raise
        raise AmoroCredentialValidationError(
            "AMS credential validation failed after retries"
        ) from last_error

    async def _fetch_compatibility(self) -> AmsCompatibility:
        try:
            status_code, body, _headers = await self._request_once(
                "/api/ams/v1/versionInfo",
                [],
                allow_without_request_credentials=True,
                request_timeout=httpx.Timeout(self.settings.readiness_timeout_seconds),
            )
            if status_code != 200:
                compatibility = AmsCompatibility(None, "unverified")
            else:
                payload = json.loads(body)
                result = payload.get("result") if isinstance(payload, dict) else None
                raw_version = result.get("version") if isinstance(result, dict) else None
                compatibility = (
                    classify_ams_version(raw_version)
                    if isinstance(payload, dict) and payload.get("code") == 200
                    else AmsCompatibility(None, "unverified")
                )
        except Exception:
            compatibility = AmsCompatibility(None, "unverified")

        if compatibility.status == "unverified":
            LOGGER.warning(
                "ams_version_unverified",
                extra={
                    "ams_version": compatibility.normalized_version,
                    "compatibility": compatibility.status,
                },
            )
        return compatibility

    async def get_compatibility(self) -> AmsCompatibility:
        now = time.monotonic()
        if self._compatibility is not None and now < self._compatibility_expires_at:
            return self._compatibility
        async with self._compatibility_lock:
            now = time.monotonic()
            if self._compatibility is not None and now < self._compatibility_expires_at:
                return self._compatibility
            compatibility = await self._fetch_compatibility()
            self._compatibility = compatibility
            self._compatibility_expires_at = time.monotonic() + COMPATIBILITY_CACHE_TTL_SECONDS
            return compatibility

    async def ensure_compatible(self) -> None:
        compatibility = await self.get_compatibility()
        if compatibility.status == "unsupported":
            raise ToolError(
                f"AMS version {compatibility.normalized_version} is unsupported; "
                "minimum supported version is 0.8.0"
            )

    async def readiness(self) -> Readiness:
        healthy = False
        try:
            status_code, body, _headers = await self._request_once(
                "/api/ams/v1/health/status",
                [],
                allow_without_request_credentials=True,
                request_timeout=httpx.Timeout(self.settings.readiness_timeout_seconds),
            )
            payload = json.loads(body)
            healthy = (
                status_code == 200 and isinstance(payload, dict) and payload.get("code") == 200
            )
        except Exception as exc:
            LOGGER.warning(
                "ams_health_check_failed",
                extra={"error_type": type(exc).__name__},
            )

        compatibility = await self.get_compatibility()
        ready = healthy and compatibility.status != "unsupported"
        return Readiness(
            ready=ready,
            status="ready" if ready else "not_ready",
            ams_version=compatibility.normalized_version,
            compatibility=compatibility.status,
        )

    async def check_connectivity(self) -> dict[str, Any]:
        """Return a sanitized AMS health classification without response details."""

        try:
            status_code, body, _headers = await self._request_once(
                "/api/ams/v1/health/status",
                [],
                allow_without_request_credentials=True,
                request_timeout=httpx.Timeout(self.settings.readiness_timeout_seconds),
            )
        except (httpx.TransportError, httpx.TimeoutException):
            return {"reachable": False, "amsHealth": "unknown", "failureClass": "network"}
        except Exception:
            return {"reachable": False, "amsHealth": "unknown", "failureClass": "unknown"}

        if status_code in {401, 403}:
            return {
                "reachable": True,
                "amsHealth": "unknown",
                "failureClass": "auth",
                "httpStatus": status_code,
            }
        if status_code < 200 or status_code >= 300:
            return {
                "reachable": True,
                "amsHealth": "unhealthy",
                "failureClass": "server",
                "httpStatus": status_code,
            }
        try:
            payload = json.loads(body)
        except (UnicodeDecodeError, json.JSONDecodeError):
            return {"reachable": True, "amsHealth": "unknown", "failureClass": "unknown"}
        if not isinstance(payload, dict):
            return {"reachable": True, "amsHealth": "unknown", "failureClass": "unknown"}
        if payload.get("code") == 200:
            return {"reachable": True, "amsHealth": "healthy", "failureClass": None}
        return {"reachable": True, "amsHealth": "unhealthy", "failureClass": "server"}
