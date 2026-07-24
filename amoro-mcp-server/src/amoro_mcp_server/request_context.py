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

from contextvars import ContextVar, Token
from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class AmoroRequestCredentials:
    api_key: str
    api_secret: str


@dataclass(frozen=True)
class RequestAuthentication:
    internal_token: str
    client_id: str
    subject: str
    claims: dict[str, Any]
    amoro_credentials: AmoroRequestCredentials | None = None


_REQUEST_AUTH: ContextVar[RequestAuthentication | None] = ContextVar(
    "amoro_mcp_request_auth",
    default=None,
)


def get_request_authentication() -> RequestAuthentication | None:
    return _REQUEST_AUTH.get()


def get_amoro_request_credentials() -> AmoroRequestCredentials | None:
    authentication = get_request_authentication()
    return authentication.amoro_credentials if authentication is not None else None


def set_request_authentication(
    authentication: RequestAuthentication,
) -> Token[RequestAuthentication | None]:
    return _REQUEST_AUTH.set(authentication)


def reset_request_authentication(token: Token[RequestAuthentication | None]) -> None:
    _REQUEST_AUTH.reset(token)
