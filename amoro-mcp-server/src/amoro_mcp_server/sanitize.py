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

import re
from collections.abc import Mapping
from typing import Any

SENSITIVE_KEY = re.compile(
    r"(?:password|passwd|secret|token|credential|access.?key|private.?key|"
    r"authorization|cookie|signature|api.?key)",
    re.IGNORECASE,
)
CONFIGURATION_KEY = re.compile(r"(?:config(?:uration)?s?|settings?)", re.IGNORECASE)
FILE_STAT_FIELDS = {"fileCnt", "totalSize", "averageSize"}


def _is_blocked_key(key: str) -> bool:
    return bool(SENSITIVE_KEY.search(key) or CONFIGURATION_KEY.search(key))


def project_safe(value: Any) -> Any:
    """Recursively retain business fields while removing credentials and configuration."""

    if isinstance(value, list):
        return [project_safe(item) for item in value]
    if not isinstance(value, Mapping):
        return value

    projected: dict[str, Any] = {}
    for key, item in value.items():
        key_string = str(key)
        if _is_blocked_key(key_string):
            continue
        projected[key_string] = project_safe(item)
    return projected


def _project_file_statistics(value: Any) -> dict[str, Any]:
    if not isinstance(value, Mapping):
        return {}
    return {
        key: project_safe(item)
        for key, item in value.items()
        if key in FILE_STAT_FIELDS and not _is_blocked_key(key)
    }


def project_catalogs(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []
    return [
        {
            key: item[key]
            for key in ("catalogName", "catalogType")
            if isinstance(item, Mapping) and key in item
        }
        for item in value
        if isinstance(item, Mapping)
    ]


def _project_page(value: Any, item_projector: Any) -> dict[str, Any]:
    if not isinstance(value, Mapping):
        return {"list": [], "total": 0}
    raw_list = value.get("list")
    return {
        "list": [item_projector(item) for item in raw_list] if isinstance(raw_list, list) else [],
        "total": value.get("total", 0),
    }


def project_snapshots(value: Any) -> dict[str, Any]:
    """Preserve safe snapshot metrics while enforcing the paginated response shape."""

    return _project_page(value, project_safe)


def project_optimizers(value: Any) -> dict[str, Any]:
    allowed = {
        "startTime",
        "touchTime",
        "jobId",
        "groupName",
        "coreNumber",
        "memory",
        "jobStatus",
        "container",
    }

    def item_projector(item: Any) -> dict[str, Any]:
        if not isinstance(item, Mapping):
            return {}
        return {key: project_safe(data) for key, data in item.items() if key in allowed}

    return _project_page(value, item_projector)


def project_optimizing_tables(value: Any) -> dict[str, Any]:
    """Return only table scheduling and capacity fields used by AIOps diagnosis."""

    allowed = {
        "tableIdentifier",
        "catalogName",
        "databaseName",
        "tableName",
        "optimizeStatus",
        "optimizingStatus",
        "duration",
        "fileCount",
        "fileSize",
        "quota",
        "quotaOccupation",
        "groupName",
    }

    def item_projector(item: Any) -> dict[str, Any]:
        if not isinstance(item, Mapping):
            return {}
        return {key: project_safe(data) for key, data in item.items() if key in allowed}

    return _project_page(value, item_projector)


def project_tasks(value: Any) -> dict[str, Any]:
    allowed = {
        "tableId",
        "processId",
        "taskId",
        "partitionData",
        "status",
        "retryNum",
        "threadId",
        "startTime",
        "endTime",
        "costTime",
        "failReason",
        "inputFiles",
        "outputFiles",
        "summary",
        "properties",
    }

    def item_projector(item: Any) -> dict[str, Any]:
        if not isinstance(item, Mapping):
            return {}
        result: dict[str, Any] = {}
        for key, data in item.items():
            if key not in allowed or _is_blocked_key(key):
                continue
            result[key] = (
                _project_file_statistics(data)
                if key in {"inputFiles", "outputFiles"}
                else project_safe(data)
            )
        return result

    return _project_page(value, item_projector)


def project_resource_groups(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []
    projected: list[dict[str, Any]] = []
    for item in value:
        if not isinstance(item, Mapping):
            continue
        resource = item.get("resourceGroup")
        safe_resource: dict[str, Any] = {}
        if isinstance(resource, Mapping):
            for key in ("name", "container", "properties"):
                if key in resource:
                    safe_resource[key] = (
                        project_safe(resource[key]) if key == "properties" else resource[key]
                    )
        projected.append(
            {
                "resourceGroup": safe_resource,
                "occupationCore": item.get("occupationCore", 0),
                "occupationMemory": item.get("occupationMemory", 0),
            }
        )
    return projected
