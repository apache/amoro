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

from typing import Any

from fastmcp import FastMCP

from amoro_mcp_server.actions import ActionService
from amoro_mcp_server.tools.common import READ_ONLY_ANNOTATIONS


def register(mcp: FastMCP, service: ActionService) -> None:
    @mcp.tool(
        title="List optimizer resource groups",
        description="List resource groups with occupied resources and sanitized properties.",
        annotations=READ_ONLY_ANNOTATIONS,
    )
    async def list_resource_groups() -> dict[str, Any]:
        return await service.execute("list_resource_groups", {})
