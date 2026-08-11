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

from typing import Annotated, Any

from fastmcp import FastMCP
from pydantic import Field

from amoro_mcp_server.actions import ActionService
from amoro_mcp_server.tools.common import (
    READ_ONLY_ANNOTATIONS,
    NameArg,
    PageArg,
    PageSizeArg,
)


def register(mcp: FastMCP, service: ActionService) -> None:
    @mcp.tool(
        title="List table optimizing processes",
        description="List paginated optimizing processes with optional type and status filters.",
        annotations=READ_ONLY_ANNOTATIONS,
    )
    async def list_optimizing_processes(
        catalog: NameArg,
        database: NameArg,
        table: NameArg,
        type: Annotated[str | None, Field(max_length=128)] = None,
        status: Annotated[str | None, Field(max_length=128)] = None,
        page: PageArg = 1,
        page_size: PageSizeArg = 20,
    ) -> dict[str, Any]:
        return await service.execute(
            "list_optimizing_processes",
            {
                "catalog": catalog,
                "database": database,
                "table": table,
                "type": type,
                "status": status,
                "page": page,
                "page_size": page_size,
            },
        )
