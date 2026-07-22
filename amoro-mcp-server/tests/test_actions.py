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

from typing import Any

import pytest
from fastmcp.exceptions import ToolError

from amoro_mcp_server.actions import ACTION_SPECS, ACTIONS, ActionService

PROMOTED_ACTIONS = {
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
}


class FakeClient:
    def __init__(self, result: Any) -> None:
        self.result = result
        self.calls: list[tuple[str, list[tuple[str, str]]]] = []
        self.compatibility_checks = 0

    async def ensure_compatible(self) -> None:
        self.compatibility_checks += 1

    async def get_json(self, path: str, params: list[tuple[str, str]]) -> Any:
        self.calls.append((path, params))
        return self.result


def test_action_catalog_is_get_only_and_excludes_unsafe_routes() -> None:
    excluded = (
        "/files",
        "/operations",
        "/settings",
        "/terminal",
        "/login",
        "/config/",
    )
    assert len(ACTIONS) == len(ACTION_SPECS)
    assert set(ACTIONS) == PROMOTED_ACTIONS
    assert all(spec.path.startswith("/api/ams/v1/") for spec in ACTION_SPECS)
    assert not any(fragment in spec.path for fragment in excluded for spec in ACTION_SPECS)
    assert {spec.path for spec in ACTION_SPECS if "/snapshots" in spec.path} == {
        "/api/ams/v1/tables/catalogs/{catalog}/dbs/{database}/tables/{table}/snapshots"
    }


@pytest.mark.asyncio
async def test_execute_encodes_path_and_maps_query_parameters() -> None:
    client = FakeClient({"list": [], "total": 0})
    service = ActionService(client, 1_048_576)  # type: ignore[arg-type]

    await service.execute(
        "list_optimizing_tasks",
        {
            "catalog": "prod/catalog",
            "database": "db name",
            "table": "orders",
            "process_id": "process/1",
            "page": 2,
            "page_size": 25,
        },
    )

    path, query = client.calls[0]
    assert "prod%2Fcatalog" in path
    assert "db%20name" in path
    assert "process%2F1" in path
    assert query == [("page", "2"), ("pageSize", "25")]
    assert client.compatibility_checks == 1


@pytest.mark.asyncio
async def test_optimizing_processes_adds_compatibility_category_without_public_parameter() -> None:
    client = FakeClient({"list": [], "total": 0})
    service = ActionService(client, 1_048_576)  # type: ignore[arg-type]

    await service.execute(
        "list_optimizing_processes",
        {
            "catalog": "prod",
            "database": "sales",
            "table": "orders",
            "page": 1,
            "page_size": 20,
        },
    )

    assert client.calls == [
        (
            "/api/ams/v1/tables/catalogs/prod/dbs/sales/tables/orders/optimizing-processes",
            [("processCategory", "optimizing"), ("page", "1"), ("pageSize", "20")],
        )
    ]
    with pytest.raises(ToolError, match="Extra inputs are not permitted"):
        await service.execute(
            "list_optimizing_processes",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "processCategory": "other",
            },
        )


@pytest.mark.asyncio
async def test_snapshot_action_maps_filters_and_preserves_safe_new_metrics() -> None:
    client = FakeClient(
        {
            "list": [
                {
                    "snapshotId": "123",
                    "producer": "OPTIMIZE",
                    "futureMetric": 42,
                    "summary": {
                        "total-records": "100",
                        "new-iceberg-metric": "7",
                        "optimizerToken": "never-return",
                        "storageConfiguration": {"endpoint": "internal"},
                    },
                }
            ],
            "total": 1,
        }
    )
    service = ActionService(client, 1_048_576)  # type: ignore[arg-type]

    result = await service.execute(
        "list_table_snapshots",
        {
            "catalog": "prod/catalog",
            "database": "db name",
            "table": "orders",
            "ref": "release/v1",
            "commit_type": "optimizing",
            "page": 2,
            "page_size": 10,
        },
    )

    path, query = client.calls[0]
    assert path.endswith("/prod%2Fcatalog/dbs/db%20name/tables/orders/snapshots")
    assert query == [
        ("ref", "release/v1"),
        ("operation", "optimizing"),
        ("page", "2"),
        ("pageSize", "10"),
    ]
    snapshot = result["data"]["list"][0]
    assert snapshot["producer"] == "OPTIMIZE"
    assert snapshot["futureMetric"] == 42
    assert snapshot["summary"] == {
        "total-records": "100",
        "new-iceberg-metric": "7",
    }


@pytest.mark.asyncio
async def test_optimizing_tables_action_maps_group_and_projects_capacity_fields() -> None:
    client = FakeClient(
        {
            "list": [
                {
                    "tableIdentifier": "prod.sales.orders",
                    "optimizeStatus": "PENDING",
                    "duration": 10_800_000,
                    "fileCount": 900,
                    "fileSize": 4096,
                    "quota": 0.1,
                    "quotaOccupation": 0.1,
                    "groupName": "external-prod",
                    "optimizerToken": "never-return",
                    "configuration": {"secret": "never-return"},
                }
            ],
            "total": 1,
        }
    )
    service = ActionService(client, 1_048_576)  # type: ignore[arg-type]

    result = await service.execute(
        "list_optimizing_tables",
        {"optimizer_group": "external/prod", "page": 2, "page_size": 25},
    )

    assert client.calls == [
        (
            "/api/ams/v1/optimize/optimizerGroups/external%2Fprod/tables",
            [("page", "2"), ("pageSize", "25")],
        )
    ]
    assert result["data"] == {
        "list": [
            {
                "tableIdentifier": "prod.sales.orders",
                "optimizeStatus": "PENDING",
                "duration": 10_800_000,
                "fileCount": 900,
                "fileSize": 4096,
                "quota": 0.1,
                "quotaOccupation": 0.1,
                "groupName": "external-prod",
            }
        ],
        "total": 1,
    }


@pytest.mark.asyncio
async def test_default_projection_keeps_new_metrics_but_drops_configuration() -> None:
    client = FakeClient(
        {
            "tableName": "orders",
            "icebergMetrics": {"positionDeleteRecords": 17},
            "storageConfig": {"endpoint": "internal"},
        }
    )
    service = ActionService(client, 1_048_576)  # type: ignore[arg-type]

    result = await service.execute(
        "get_table_details",
        {"catalog": "prod", "database": "sales", "table": "orders"},
    )

    assert result["data"] == {
        "tableName": "orders",
        "icebergMetrics": {"positionDeleteRecords": 17},
    }


@pytest.mark.asyncio
async def test_unknown_action_and_invalid_pagination_are_rejected() -> None:
    service = ActionService(FakeClient(None), 1_048_576)  # type: ignore[arg-type]
    with pytest.raises(ToolError, match="Unknown action"):
        await service.execute("get_any_url", {})
    with pytest.raises(ToolError, match="less than or equal to 100"):
        await service.execute(
            "list_optimizers", {"optimizer_group": "all", "page": 1, "page_size": 101}
        )
    with pytest.raises(ToolError, match="Input should be"):
        await service.execute(
            "list_table_snapshots",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "commit_type": "rewrite",
            },
        )
    with pytest.raises(ToolError, match="greater than or equal to 1"):
        await service.execute(
            "list_table_snapshots",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "page": 0,
            },
        )
    with pytest.raises(ToolError, match="Extra inputs are not permitted"):
        await service.execute(
            "list_table_snapshots",
            {
                "catalog": "prod",
                "database": "sales",
                "table": "orders",
                "snapshot_id": "123",
            },
        )
