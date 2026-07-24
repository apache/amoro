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

from collections.abc import Callable

from fastmcp import FastMCP

from amoro_mcp_server.actions import ActionService
from amoro_mcp_server.tools.check_ams_connectivity import register as register_ams_connectivity
from amoro_mcp_server.tools.get_table_details import register as register_table_details
from amoro_mcp_server.tools.list_catalogs import register as register_catalogs
from amoro_mcp_server.tools.list_databases import register as register_databases
from amoro_mcp_server.tools.list_optimizers import register as register_optimizers
from amoro_mcp_server.tools.list_optimizing_processes import (
    register as register_optimizing_processes,
)
from amoro_mcp_server.tools.list_optimizing_tables import register as register_optimizing_tables
from amoro_mcp_server.tools.list_optimizing_tasks import register as register_optimizing_tasks
from amoro_mcp_server.tools.list_resource_groups import register as register_resource_groups
from amoro_mcp_server.tools.list_table_snapshots import register as register_table_snapshots
from amoro_mcp_server.tools.list_tables import register as register_tables

ToolRegistrar = Callable[[FastMCP, ActionService], None]

TOOL_REGISTRARS: tuple[ToolRegistrar, ...] = (
    register_catalogs,
    register_databases,
    register_tables,
    register_table_details,
    register_table_snapshots,
    register_optimizing_processes,
    register_optimizing_tasks,
    register_optimizers,
    register_optimizing_tables,
    register_resource_groups,
    register_ams_connectivity,
)


def register_tools(mcp: FastMCP, service: ActionService) -> None:
    """Register every public Amoro MCP tool on the server instance."""
    for register in TOOL_REGISTRARS:
        register(mcp, service)
