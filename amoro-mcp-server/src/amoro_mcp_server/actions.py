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

import json
import logging
import time
from collections.abc import Callable
from dataclasses import dataclass, field
from typing import Any
from urllib.parse import quote

from fastmcp.exceptions import ToolError
from fastmcp.server.dependencies import get_access_token
from pydantic import BaseModel, ValidationError

from amoro_mcp_server.client import AmoroClient
from amoro_mcp_server.models import (
    CatalogSearchParams,
    EmptyParams,
    OptimizerParams,
    OptimizingProcessParams,
    OptimizingTableParams,
    OptimizingTaskParams,
    SnapshotParams,
    TableListParams,
    TableParams,
)
from amoro_mcp_server.sanitize import (
    project_catalogs,
    project_optimizers,
    project_optimizing_tables,
    project_resource_groups,
    project_safe,
    project_snapshots,
    project_tasks,
)

LOGGER = logging.getLogger(__name__)
Projector = Callable[[Any], Any]


@dataclass(frozen=True)
class ActionSpec:
    action_id: str
    path: str
    params_model: type[BaseModel]
    query_fields: dict[str, str] = field(default_factory=dict)
    fixed_query: tuple[tuple[str, str], ...] = ()
    projector: Projector = project_safe


def _action(
    action_id: str,
    path: str,
    params_model: type[BaseModel],
    *,
    query_fields: dict[str, str] | None = None,
    fixed_query: tuple[tuple[str, str], ...] = (),
    projector: Projector = project_safe,
) -> ActionSpec:
    return ActionSpec(
        action_id=action_id,
        path=path,
        params_model=params_model,
        query_fields=query_fields or {},
        fixed_query=fixed_query,
        projector=projector,
    )


TABLE_PATH = "/api/ams/v1/tables/catalogs/{catalog}/dbs/{database}/tables/{table}"

ACTION_SPECS = (
    _action(
        "list_catalogs",
        "/api/ams/v1/catalogs",
        EmptyParams,
        projector=project_catalogs,
    ),
    _action(
        "list_databases",
        "/api/ams/v1/catalogs/{catalog}/databases",
        CatalogSearchParams,
        query_fields={"keywords": "keywords"},
    ),
    _action(
        "list_tables",
        "/api/ams/v1/catalogs/{catalog}/databases/{database}/tables",
        TableListParams,
        query_fields={"keywords": "keywords"},
    ),
    _action(
        "get_table_details",
        f"{TABLE_PATH}/details",
        TableParams,
    ),
    _action(
        "list_table_snapshots",
        f"{TABLE_PATH}/snapshots",
        SnapshotParams,
        query_fields={
            "ref": "ref",
            "commit_type": "operation",
            "page": "page",
            "page_size": "pageSize",
        },
        projector=project_snapshots,
    ),
    _action(
        "list_optimizing_processes",
        f"{TABLE_PATH}/optimizing-processes",
        OptimizingProcessParams,
        query_fields={"type": "type", "status": "status", "page": "page", "page_size": "pageSize"},
        fixed_query=(("processCategory", "optimizing"),),
    ),
    _action(
        "list_optimizing_tasks",
        f"{TABLE_PATH}/optimizing-processes/{{process_id}}/tasks",
        OptimizingTaskParams,
        query_fields={"page": "page", "page_size": "pageSize"},
        projector=project_tasks,
    ),
    _action(
        "list_optimizers",
        "/api/ams/v1/optimize/optimizerGroups/{optimizer_group}/optimizers",
        OptimizerParams,
        query_fields={"page": "page", "page_size": "pageSize"},
        projector=project_optimizers,
    ),
    _action(
        "list_optimizing_tables",
        "/api/ams/v1/optimize/optimizerGroups/{optimizer_group}/tables",
        OptimizingTableParams,
        query_fields={"page": "page", "page_size": "pageSize"},
        projector=project_optimizing_tables,
    ),
    _action(
        "list_resource_groups",
        "/api/ams/v1/optimize/resourceGroups",
        EmptyParams,
        projector=project_resource_groups,
    ),
)

ACTIONS = {spec.action_id: spec for spec in ACTION_SPECS}


class ActionService:
    def __init__(self, client: AmoroClient, response_max_bytes: int) -> None:
        self.client = client
        self.response_max_bytes = response_max_bytes

    @staticmethod
    def _request_parts(spec: ActionSpec, params: BaseModel) -> tuple[str, list[tuple[str, str]]]:
        values = params.model_dump(exclude_none=True)
        path = spec.path
        for field_name, value in values.items():
            placeholder = "{" + field_name + "}"
            if placeholder in path:
                path = path.replace(placeholder, quote(str(value), safe=""))

        query = list(spec.fixed_query)
        for field_name, query_name in spec.query_fields.items():
            if field_name not in values:
                continue
            value = values[field_name]
            if isinstance(value, list):
                query.extend((query_name, str(item)) for item in value)
            else:
                query.append((query_name, str(value)))
        if "{" in path or "}" in path:
            raise ToolError("Action parameters did not resolve the allowlisted path")
        return path, query

    async def execute(self, action_id: str, parameters: dict[str, Any]) -> dict[str, Any]:
        spec = ACTIONS.get(action_id)
        if spec is None:
            raise ToolError(f"Unknown action '{action_id}'")
        try:
            validated = spec.params_model.model_validate(parameters)
        except ValidationError as exc:
            issues = "; ".join(
                f"{'.'.join(str(part) for part in error['loc'])}: {error['msg']}"
                for error in exc.errors(include_input=False)
            )
            raise ToolError(f"Invalid parameters for {action_id}: {issues}") from exc

        path, query = self._request_parts(spec, validated)
        token = get_access_token()
        client_id = token.client_id if token is not None else "unauthenticated"
        started = time.perf_counter()
        try:
            await self.client.ensure_compatible()
            result = await self.client.get_json(path, query)
            projected = spec.projector(result)
            response = {"action": action_id, "data": projected}
            encoded = json.dumps(response, ensure_ascii=False, separators=(",", ":")).encode()
            if len(encoded) > self.response_max_bytes:
                raise ToolError("Projected MCP result exceeds the configured size limit")
            LOGGER.info(
                "amoro_action_complete",
                extra={
                    "action": action_id,
                    "client_id": client_id,
                    "duration_ms": round((time.perf_counter() - started) * 1000, 2),
                },
            )
            return response
        except Exception as exc:
            LOGGER.error(
                "amoro_action_failed",
                extra={
                    "action": action_id,
                    "client_id": client_id,
                    "duration_ms": round((time.perf_counter() - started) * 1000, 2),
                    "error_type": type(exc).__name__,
                },
            )
            raise
