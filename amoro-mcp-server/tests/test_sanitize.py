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

from amoro_mcp_server.sanitize import (
    project_catalogs,
    project_optimizers,
    project_resource_groups,
    project_safe,
    project_tasks,
)


def test_catalog_projection_drops_configuration_and_unknown_fields() -> None:
    catalogs = project_catalogs(
        [
            {
                "catalogName": "prod",
                "catalogType": "hive",
                "authConfigs": {"password": "leak"},
                "storageConfigs": {"secret": "leak"},
            }
        ]
    )
    assert catalogs == [{"catalogName": "prod", "catalogType": "hive"}]


def test_optimizer_task_and_resource_group_projection_is_fail_closed() -> None:
    optimizer = project_optimizers(
        {
            "list": [
                {
                    "token": "optimizer-secret",
                    "jobId": "job-1",
                    "groupName": "default",
                    "coreNumber": 2,
                    "memory": 1024,
                    "jobStatus": "RUNNING",
                    "container": "flink",
                    "unexpected": "drop-me",
                }
            ],
            "total": 1,
        }
    )
    assert optimizer["list"][0]["jobId"] == "job-1"
    assert "token" not in optimizer["list"][0]
    assert "unexpected" not in optimizer["list"][0]

    tasks = project_tasks(
        {
            "list": [
                {
                    "taskId": 1,
                    "processId": "p1",
                    "optimizerToken": "optimizer-secret",
                    "summary": {"retry-count": "0", "optimizer.token": "secret"},
                    "properties": {"safe": "yes", "password": "secret"},
                    "inputFiles": {"fileCnt": 2, "totalSize": 10, "path": "/secret"},
                }
            ],
            "total": 1,
        }
    )
    task = tasks["list"][0]
    assert "optimizerToken" not in task
    assert task["summary"] == {"retry-count": "0"}
    assert task["properties"] == {"safe": "yes"}
    assert task["inputFiles"] == {"fileCnt": 2, "totalSize": 10}

    groups = project_resource_groups(
        [
            {
                "resourceGroup": {
                    "name": "default",
                    "container": "flink",
                    "properties": {"parallelism": "2", "secretKey": "secret"},
                },
                "occupationCore": 2,
                "occupationMemory": 1024,
                "unknown": "drop",
            }
        ]
    )
    assert groups == [
        {
            "resourceGroup": {
                "name": "default",
                "container": "flink",
                "properties": {"parallelism": "2"},
            },
            "occupationCore": 2,
            "occupationMemory": 1024,
        }
    ]


def test_global_projection_preserves_new_metrics_and_drops_sensitive_configuration() -> None:
    assert project_safe(
        {
            "tableName": "orders",
            "properties": {"owner": "analytics", "credential": "secret"},
            "icebergMetrics": {
                "positionDeleteRecords": 17,
                "newNestedMetric": {"value": 4, "apiToken": "secret"},
            },
            "systemSettings": {"server.port": 1234},
            "storageConfig": {"endpoint": "internal"},
        }
    ) == {
        "tableName": "orders",
        "properties": {"owner": "analytics"},
        "icebergMetrics": {
            "positionDeleteRecords": 17,
            "newNestedMetric": {"value": 4},
        },
    }
