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

from typing import Literal
from urllib.parse import urlparse
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

from pydantic import BaseModel, ConfigDict, Field, SecretStr, field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


def _validate_http_url(value: str, *, field_name: str) -> str:
    parsed = urlparse(value)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise ValueError(f"{field_name} must be an absolute HTTP(S) URL")
    if parsed.query or parsed.fragment:
        raise ValueError(f"{field_name} must not contain a query string or fragment")
    return value.rstrip("/")


class InboundAuthSettings(BaseModel):
    """Authentication settings for clients connecting to the MCP endpoint."""

    model_config = ConfigDict(extra="forbid")

    mode: Literal["none", "ams-api-key"]


class AmoroSettings(BaseModel):
    """Connection settings for the upstream Amoro Management Service."""

    base_url: str = "http://127.0.0.1:1630"
    auth_type: Literal["basic", "token", "none"] = "none"
    username: str | None = None
    password: SecretStr | None = None
    api_key: str | None = None
    api_secret: SecretStr | None = None
    signature_timezone: str = "UTC"
    connect_timeout_seconds: float = Field(default=5.0, gt=0, le=60)
    read_timeout_seconds: float = Field(default=30.0, gt=0, le=300)
    readiness_timeout_seconds: float = Field(default=2.0, gt=0, le=30)
    max_retries: int = Field(default=2, ge=0, le=5)
    tls_verify: bool = True
    tls_ca_file: str | None = None

    @field_validator("base_url")
    @classmethod
    def validate_base_url(cls, value: str) -> str:
        return _validate_http_url(value, field_name="base_url")

    @field_validator("signature_timezone")
    @classmethod
    def validate_signature_timezone(cls, value: str) -> str:
        try:
            ZoneInfo(value)
        except ZoneInfoNotFoundError as exc:
            raise ValueError("signature_timezone must be a valid IANA timezone") from exc
        return value

    @model_validator(mode="after")
    def validate_credentials(self) -> AmoroSettings:
        if self.auth_type == "basic" and not (self.username and self.password):
            raise ValueError("basic auth requires username and password")
        if self.auth_type == "token" and not (self.api_key and self.api_secret):
            raise ValueError("token auth requires api_key and api_secret")
        return self


class Settings(BaseSettings):
    """Application settings loaded from AMORO_MCP_* environment variables."""

    model_config = SettingsConfigDict(
        env_prefix="AMORO_MCP_",
        env_nested_delimiter="__",
        case_sensitive=False,
        extra="ignore",
    )

    host: str = "0.0.0.0"  # noqa: S104 - intentional server bind address
    port: int = Field(default=8000, ge=1, le=65535)
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"] = "INFO"
    response_max_bytes: int = Field(default=1_048_576, ge=1024, le=10_485_760)
    allowed_hosts: list[str] = Field(default_factory=list)
    allowed_origins: list[str] = Field(default_factory=list)
    auth: InboundAuthSettings
    amoro: AmoroSettings = Field(default_factory=AmoroSettings)

    @model_validator(mode="after")
    def validate_auth_mode_with_amoro(self) -> Settings:
        if self.auth.mode == "ams-api-key":
            configured_credentials = any(
                value is not None
                for value in (
                    self.amoro.username,
                    self.amoro.password,
                    self.amoro.api_key,
                    self.amoro.api_secret,
                )
            )
            if self.amoro.auth_type != "none" or configured_credentials:
                raise ValueError(
                    "ams-api-key inbound authentication uses request credentials; "
                    "server-wide AMS authentication and credentials must be absent"
                )
        return self
