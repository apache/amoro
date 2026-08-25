<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

# Apache Amoro MCP Server

The Apache Amoro MCP server provides access to the Amoro Management Service (AMS) over
the Model Context Protocol (MCP). It is implemented with Python and FastMCP, uses the
stateless Streamable HTTP transport, and exposes its MCP endpoint at `/mcp`.

The server exposes eleven dedicated tools for catalog, table, snapshot, optimizing,
optimizer, and resource-group queries. Additional capabilities can be added as dedicated tool
modules without changing the transport or deployment model. There is no generic action executor
or arbitrary URL access.

## Architecture

```text
    MCP clients
        |
        | Streamable HTTP: /mcp
        | X-Amoro-API-Key + X-Amoro-API-Secret
        v
+-----------------------------+
| inbound auth mode dispatch  |
| request-scoped credentials  |
| strict MCP input validation |
+-----------------------------+
        |
        v
+-----------------------------+
| Dedicated MCP tools         |
| one Python module per tool  |
+-----------------------------+
        |
        v
+-----------------------------+
| ActionService               |
| static GET path allowlist   |
| path encoding and projection|
+-----------------------------+
        |
        v
+-----------------------------+       HTTPS / HTTP Basic or
| Async AmoroClient           | ----> client-owned signature ----> AMS
| timeout, retry, size limit  |
+-----------------------------+
```

In `ams-api-key` authentication mode, each MCP client's Amoro credentials validate the MCP request and
sign that client's subsequent AMS requests. The secret is held only in request-local context and
is not stored in FastMCP access-token claims or authentication caches. In `none` mode, the MCP
endpoint does not authenticate clients and AMS uses the server's upstream configuration.

Before a tool accesses AMS, a compatibility layer checks the AMS version and blocks versions
older than the supported baseline. The same cached check is included in readiness.

## AMS compatibility

The minimum supported AMS version is `0.8.0-incubating`. The initial compatibility matrix is:

| AMS version | Status | Notes |
| --- | --- | --- |
| 0.7.x and earlier | Unsupported | These releases use the older `/ams/v1` REST prefix |
| 0.8.x | Supported | Minimum line supported by this MCP server |
| 0.9.x | Supported | Optimizing-process requests include `processCategory=optimizing` |
| Later versions | Unverified | A warning is logged, but requests are allowed |

The server reads `GET /api/ams/v1/versionInfo`, normalizes versions such as
`0.8.0-incubating(...)` and `0.9.0-SNAPSHOT(...)` to `0.8.0` and `0.9.0`, and never returns the
commit suffix. Results are cached for 300 seconds and concurrent checks are coalesced so AMS is
not queried once per tool call. An unsupported version makes readiness return HTTP 503 and blocks
tool execution. An unparseable version is treated as unverified and does not stop service.

Each MCP release will declare its AMS compatibility matrix. Removal of a previously supported
AMS line follows the Apache Amoro deprecation process instead of implicitly tracking only the
latest AMS release.

## Tools

| Tool | Parameters | Result |
| --- | --- | --- |
| `list_catalogs` | None | Catalog names and types, without catalog configuration |
| `list_databases` | `catalog`, optional `keywords` | Databases in a catalog |
| `list_tables` | `catalog`, `database`, optional `keywords` | Tables and formats in a database |
| `get_table_details` | `catalog`, `database`, `table` | Schema, partitioning, summary, and safe table metadata |
| `list_table_snapshots` | Table identity, optional `ref` and `commit_type`, `page`, `page_size` | Snapshot commits and sanitized record/file/size metrics |
| `list_optimizing_processes` | Table identity, optional `type` and `status`, `page`, `page_size` | Paginated optimizing processes |
| `list_optimizing_tasks` | Table identity, `process_id`, `page`, `page_size` | Task state, timing, failure reason, and aggregate input/output file statistics |
| `list_optimizers` | Optional `optimizer_group`, `page`, `page_size` | Runtime state, resources, container, start time, and heartbeat time |
| `list_optimizing_tables` | `optimizer_group`, `page`, `page_size` | Group table state, duration, quota occupation, and file metrics |
| `list_resource_groups` | None | Resource groups, occupied core/memory, container, and sanitized properties |
| `check_ams_connectivity` | None | Sanitized AMS API reachability and health classification |

`page` starts at 1. `page_size` defaults to 20 and must be between 1 and 100. The default optimizer
group is `all`.

`list_optimizing_tasks` never returns optimizer credentials or individual input/output file
records. `list_optimizers` never returns optimizer tokens. System settings and properties whose
keys indicate configuration or credentials are removed recursively.

`list_table_snapshots` accepts `commit_type` values `all`, `optimizing`, and `non-optimizing`.
Snapshots whose `producer` is `OPTIMIZE` were produced by optimizing commits. Snapshot file detail,
partition-file, and table-operation endpoints remain unavailable because they enumerate files or
expose a larger operational surface.

To compare adjacent commits, use total metrics such as `total-records` and `total-files-size`
together with the corresponding `added-*` and `removed-*` values in `summary`. The server does not
derive cross-page deltas.

Cross-catalog `search_tables` and aggregate `get_table_health_summary` diagnostics are planned
follow-up tools. They are intentionally not part of this initial eleven-tool API.

## Source layout

```text
amoro-mcp-server/
├── pyproject.toml
├── uv.lock
├── src/amoro_mcp_server/
│   ├── server.py          # FastMCP, middleware, transport, and health routes
│   ├── auth.py            # none/ams-api-key authentication and request middleware
│   ├── request_context.py # isolated per-request Amoro credentials
│   ├── config.py          # AMORO_MCP_* environment settings
│   ├── client.py          # bounded async GET client and AMS signing
│   ├── compatibility.py   # AMS version normalization and support policy
│   ├── actions.py         # internal action allowlist and result projection
│   ├── sanitize.py        # sensitive/configuration field filtering
│   └── tools/             # one public MCP tool per Python module
└── tests/
```

## Configuration

Settings are loaded from environment variables with the `AMORO_MCP_` prefix. Nested settings use
double underscores. Lists and maps must be JSON strings.

### Server settings

| Environment variable | Default | Description |
| --- | --- | --- |
| `AMORO_MCP_HOST` | `0.0.0.0` | Bind address when using the Python entry point |
| `AMORO_MCP_PORT` | `8000` | Bind port when using the Python entry point |
| `AMORO_MCP_LOG_LEVEL` | `INFO` | `DEBUG`, `INFO`, `WARNING`, `ERROR`, or `CRITICAL` |
| `AMORO_MCP_RESPONSE_MAX_BYTES` | `1048576` | Maximum upstream and projected response size |
| `AMORO_MCP_ALLOWED_HOSTS` | `[]` | JSON host allowlist for Host/Origin protection |
| `AMORO_MCP_ALLOWED_ORIGINS` | `[]` | JSON origin allowlist |

### MCP client authentication

Select one core authentication mode:

```shell
# No MCP client authentication; use only for local development or behind a trusted gateway.
export AMORO_MCP_AUTH__MODE=none

# Each MCP client authenticates with its own AMS OpenAPI credentials.
export AMORO_MCP_AUTH__MODE=ams-api-key
```

In `ams-api-key` mode, clients send `X-Amoro-API-Key` and `X-Amoro-API-Secret` on every MCP request. The
server validates them with a signed `GET /api/ams/v1/catalogs`, then uses the same credentials for
tool calls. AMS API keys identify OpenAPI clients, not users or groups, and the current AMS token
model does not provide expiry or per-tool authorization. The client secret must therefore be sent
only over HTTPS to a trusted MCP server. `/health/live` and `/health/ready` remain unauthenticated.

### AMS connection and authentication

The AMS API host and port are configured together in `AMORO_MCP_AMORO__BASE_URL`:

```shell
export AMORO_MCP_AMORO__BASE_URL='http://ams.example.internal:1630'
```

Do not append `/api/ams/v1`; the client adds allowlisted API paths. The URL may be HTTP or HTTPS
and must not contain a query or fragment.

In `none` mode, supported server-owned upstream authentication modes are:

```shell
# No upstream authentication
export AMORO_MCP_AMORO__AUTH_TYPE=none

# HTTP Basic
export AMORO_MCP_AMORO__AUTH_TYPE=basic
export AMORO_MCP_AMORO__USERNAME='<username>'
export AMORO_MCP_AMORO__PASSWORD='<password>'

# Amoro apiKey + apiSecret query signing
export AMORO_MCP_AMORO__AUTH_TYPE=token
export AMORO_MCP_AMORO__API_KEY='<ams-api-key>'
export AMORO_MCP_AMORO__API_SECRET='<ams-api-secret>'
export AMORO_MCP_AMORO__SIGNATURE_TIMEZONE=UTC
```

The signature timezone must match the AMS JVM timezone. The defaults are a 5-second connect
timeout, 30-second read timeout, 2-second readiness timeout, and two retries for connection errors
and HTTP 429/502/503/504.

When `AMORO_MCP_AUTH__MODE=ams-api-key`, set `AMORO_MCP_AMORO__AUTH_TYPE=none`. Server-owned Basic or
token credentials are rejected in this mode so client requests can never fall back to a shared
privileged identity.

TLS verification is enabled by default. To use a private CA, mount the CA file and set:

```shell
export AMORO_MCP_AMORO__TLS_VERIFY=true
export AMORO_MCP_AMORO__TLS_CA_FILE=/etc/amoro-mcp/tls/ca.crt
```

## Local development

Install [uv](https://docs.astral.sh/uv/), then run from this directory:

```shell
uv sync --locked

export AMORO_MCP_AUTH__MODE=none
export AMORO_MCP_AMORO__BASE_URL='http://127.0.0.1:1630'
export AMORO_MCP_AMORO__AUTH_TYPE=none

uv run amoro-mcp-server
```

The endpoints are:

- MCP: `http://127.0.0.1:8000/mcp`
- Liveness: `http://127.0.0.1:8000/health/live`
- Readiness: `http://127.0.0.1:8000/health/ready`

Liveness checks only the server process. Readiness makes bounded requests to the AMS health and
version endpoints. Its JSON response contains `status`, normalized `amsVersion` (or `null`), and
`compatibility` (`supported`, `unsupported`, or `unverified`). It returns HTTP 503 when AMS is
unavailable or the version is unsupported. Only the normalized semantic version is returned;
commit hashes and the raw version response are never exposed.

## Configure Codex

The Codex desktop app reads MCP servers from `~/.codex/config.toml`. For `ams-api-key` mode, configure
the user's own AMS OpenAPI credentials directly in that file.

Add the Streamable HTTP server to `~/.codex/config.toml`:

```toml
[mcp_servers.amoro-local]
url = "http://127.0.0.1:8000/mcp"
http_headers = { "X-Amoro-API-Key" = "<ams-api-key>", "X-Amoro-API-Secret" = "<ams-api-secret>" }
enabled = true
tool_timeout_sec = 60
```

`http_headers` sends both values on every MCP request. This stores the raw secret in plaintext, so
do not commit or share `~/.codex/config.toml`, ensure it is readable only by the local user, and use
HTTPS for remote MCP connections. Omit `http_headers` entirely when the server runs in `none` mode.

In the Codex desktop app:

1. Open **Settings** and select **MCP servers**.
2. Confirm that `amoro-local` is listed and enabled as a Streamable HTTP server.
3. Select **Restart**, or fully quit and reopen Codex after changing `config.toml`.
4. Start a new task so Codex refreshes the MCP tool inventory.

Do not configure an `Authorization` header for this server.

Example prompts for verifying the connection include:

- `List all Amoro catalogs.`
- `Show details for catalog.database.table.`
- `Show the latest optimizing processes for catalog.database.table, then list the tasks for the latest process.`
- `Check optimizer and resource-group status.`

## Logging and operations

The service writes structured JSON logs to stderr with Python logging and FastMCP structured
logging middleware. Logs may contain request ID, client ID, tool/action name, duration, status, and
error type. They do not contain request parameter values, response bodies, headers, or credentials;
HTTP client URL logging is suppressed because signed AMS URLs contain authentication material.

Use a process supervisor or logging agent to route stderr to the preferred logging backend.
Do not enable raw HTTP access logging around this service unless the proxy is configured to redact
Authorization, API-key, and signed-query values.

## Test and verify

Run Python checks from `amoro-mcp-server`:

```shell
uv sync --locked
uv run pytest
uv run ruff check .
uv run ruff format --check .
uv run mypy
```

Run the Apache license check from the repository root:

```shell
./mvnw -DskipTests apache-rat:check
```

The test suite covers the GET allowlist, path encoding, pagination, response filtering, signature
algorithm, retry/timeout behavior, request-scoped Amoro authentication, credential isolation,
Authorization-header rejection, version compatibility and caching, MCP tool registration and
annotations, logging redaction, and the process-to-task/optimizer/resource-group flows.
