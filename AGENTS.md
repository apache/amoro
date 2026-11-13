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

# AGENTS.md — Apache Amoro (Incubating)

## Project Overview

Apache Amoro is a **Lakehouse management system** (Java 11, Maven, Vue 3 frontend). The central component is **AMS** (Amoro Management Service), which manages table metadata, self-optimizing, and catalog services for Iceberg, Hive, Paimon, and Hudi table formats.

## Before Making Changes

- Read `CONTRIBUTING.md`, applicable nested `AGENTS.md` files, and the affected module's README, POM, and nearby tests. Use current templates, build files, and CI workflows as the source of truth for commands and supported versions.
- Inspect `git status --short`, the current branch, and remotes. Preserve existing user changes and untracked files; stage only files belonging to the requested task.
- Keep each contribution focused on one problem. Avoid unrelated refactoring, dependency upgrades, generated output, or formatting churn. Inspect the diff after running `dev/reformat`, which formats multiple profiles.
- For substantial changes, read the linked issue and design discussion and record the approach, compatibility impact, and validation plan. Follow `CONTRIBUTING.md` for community discussion; do not claim consensus or assignment without evidence. Continue local investigation and preparation within the user's authorized scope.

## Architecture

- **`amoro-common`** — Core abstractions (`FormatCatalog`, `AmoroTable`, `TableFormat`), Thrift IDL definitions (`src/main/thrift/*.thrift`), and shared client code. All other modules depend on this.
- **`amoro-ams`** — The AMS server. Entry point: `AmoroServiceContainer.main()`. Exposes Thrift services (table metastore on port 1260, optimizer service on port 1261) and an HTTP/REST API via Javalin (port 1630). Routes are registered in `DashboardServer.java`.
- **`amoro-web`** — Vue 3 + TypeScript dashboard (Vite, Ant Design Vue, pnpm). API calls in `src/services/`. Built into AMS static resources during Maven build.
- **`amoro-format-{iceberg,mixed,paimon,hudi}`** — Table format integrations loaded via **Java SPI** (`ServiceLoader<FormatCatalogFactory>`). See `CommonUnifiedCatalog.initializeFormatCatalogs()`.
- **`amoro-optimizer`** — Self-optimizing executor with standalone/Flink/Spark implementations. Communicates with AMS via Thrift (`OptimizingService`).
- **`amoro-metrics`** — Metrics subsystem with pluggable reporters (e.g., Prometheus).
- **Plugin system** — AMS plugins are configured via YAML in `dist/src/main/amoro-bin/conf/plugins/` and managed by `AbstractPluginManager`. Categories: `event-listeners`, `metric-reporters`, `rest-extensions`, `table-runtime-factories`.

## Build & Test

```bash
# Build all (skip tests, skip dashboard frontend):
./mvnw clean package -DskipTests -Pskip-dashboard-build

# Build everything including tests:
./mvnw clean package

# Run checkstyle + spotless validation only:
./mvnw validate

# Format all Java/Scala code (Spotless + google-java-format):
dev/reformat

# Run a single test class:
./mvnw test -pl amoro-ams -Dtest=TestDefaultOptimizingService

# Regenerate configuration docs after modifying ConfigOptions:
UPDATE=1 ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest

# Frontend dev (from amoro-web/):
pnpm install && pnpm dev:mock
```

Key Maven profiles: `-Pspark-3.3`/`-Pspark-3.5`, `-Psupport-all-formats`, `-Pbuild-mixed-format-trino` (requires JDK 17).

### Validation Expectations

- Start with the affected test method or class, then the affected modules. Use `-am` when changed upstream modules must be included; inspect Surefire reports to confirm the intended tests actually ran. A build with skipped tests or zero matching tests is not test evidence.
- For bug fixes, add a regression test for the failing behavior where practical. Verify that it fails without the fix and passes with it; if that comparison cannot be run, state the limitation. Do not revert user changes to perform this comparison.
- Reuse existing fixtures such as `AMSServiceTestBase`, `AMSManagerTestBase`, and `DerbyPersistence` where appropriate. Cover observable behavior, failure paths, and relevant boundaries. Prefer bounded condition waits or controllable clocks over fixed sleeps in new asynchronous tests; release executors, clients, and temporary resources.
- Run `./mvnw validate` for applicable Java/Scala/POM changes. The Apache RAT check is bound to `verify` in the root POM; `validate` alone does not verify licensing. Do not disable checks, weaken assertions, or add exclusions solely to make a change pass.
- Select relevant JDK, Spark, Flink, and format profiles from the affected POMs and `.github/workflows/`. Shared API or build changes may require downstream coverage; do not infer support for an entire matrix from one local run.
- For dashboard changes, use the current scripts in `amoro-web/package.json`: `pnpm lint`, `pnpm typecheck`, and `pnpm build` as applicable. Exercise changed interactions and capture screenshots when useful. Keep dependency changes consistent with `pnpm-lock.yaml`.
- For documentation-only changes, check content, links, and any affected generation/build workflow; Java tests are unnecessary unless the change affects code or its build.
- Report the exact commands, scope, and results. Separate checks that passed from checks that failed, were skipped, or were blocked by the environment. Investigate CI failures using logs; do not label a failure unrelated or flaky without evidence.

## Conventions & Patterns

- **PR title format**: `[AMORO-{issue_number}][{module}]{description}`
- **Code style**: google-java-format (v1.7) enforced by Spotless; Scala uses scalafmt. Run `dev/reformat` before committing.
- **Checkstyle**: Config at `tools/maven/checkstyle.xml`, runs during `validate` phase.
- **Copyright header**: Apache License 2.0 header required on all source files.
- **Database layer**: MyBatis with annotation-based mappers (no XML). See `amoro-ams/src/main/java/.../persistence/mapper/`. Services extend `PersistentBase` and use `doAs(MapperClass, lambda)` for DB operations. Supports Derby (dev), MySQL, and PostgreSQL. SQL init scripts in `amoro-ams/src/main/resources/{derby,mysql,postgres}/`.
- **REST API**: Javalin-based, all routes defined in `DashboardServer.apiGroup()` under `/api/ams/v1`. Controllers in `server/dashboard/controller/`.
- **SPI extension points**: `FormatCatalogFactory` (table formats), `FormatTableDescriptor` (metadata display), `SparkTableFormat` (Spark catalog), `ProcessFactory` (optimizing processes). Register implementations in `META-INF/services/`.
- **Configuration**: Typed config via `ConfigOption<T>` pattern. Server configs in `AmoroManagementConf.java`, runtime config in `config.yaml`.
- **Thrift**: IDL files in `amoro-common/src/main/thrift/`. Generated code uses shaded Thrift (`org.apache.amoro.shade.thrift`). Uses `ThriftClientPool` for connection management.
- **Tests**: JUnit 5 for new tests. AMS integration tests use `DerbyPersistence` for in-memory DB. Testcontainers for external service tests.

## Compatibility and Change Impact

Evaluate the applicable rows before submitting a change. Describe material compatibility risks in the existing PR sections; do not replace the repository template with a different project's template.

| Change area | Required considerations |
|-------------|-------------------------|
| Java and dependencies | Preserve Java 11 compatibility unless the module explicitly targets another version. Use existing dependency management and Amoro's shaded dependencies. `tools/maven/checkstyle.xml` forbids unshaded Guava/Jackson and specified foreign Guava relocations; consult its current rules. Check transitive conflicts and distribution licensing when changing dependencies, including `LICENSE`, `NOTICE`, `LICENSE-binary`, `NOTICE-binary`, and frontend license files as applicable. |
| Module boundaries and table formats | Keep shared abstractions in lower-level modules and format-specific behavior in the corresponding integration. Avoid circular dependencies. Verify behavior for the actual table format and engine version; do not generalize one format's compaction or metadata semantics to all formats. |
| Thrift and generated code | Preserve field IDs and meanings and consider old/new client-server interoperability. Edit the IDL, then regenerate using `amoro-common/src/main/thrift/README.md` (`./mvnw -pl amoro-common -Pthrift-gen generate-sources`); inspect the generated diff and shaded imports. Do not hand-edit generated classes as the primary fix. |
| REST and public APIs | Check request/response compatibility, validation, error behavior, and existing callers. Keep `amoro-ams/src/main/resources/openapi/openapi.yaml`, the dashboard services, and `amoro-openapi-sdk` aligned when affected, following their generation workflows. |
| Persistence | Check mapper SQL against Derby, MySQL, and PostgreSQL. Update applicable initialization and upgrade scripts together and cover migration from existing data; Derby-only tests do not establish cross-database compatibility. |
| Configuration and documentation | Consider existing keys, defaults, deprecated aliases, and upgrade behavior. Regenerate configuration docs with `ConfigurationsTest` when ConfigOptions change, inspect generated output, and update affected hand-written docs and `dist/src/main/amoro-bin/conf/config.yaml` when applicable. Document new user-visible behavior and examples. |
| Optimizing, concurrency, and performance | Check retries and idempotence, task state transitions, locking, resource ownership, and failure recovery where affected. Support performance claims with measurements, or explicitly identify them as expectations. |

## Git and Contribution Workflow

- Follow Amoro's `CONTRIBUTING.md`: normally target `master`, use the Amoro PR title format, and link the corresponding issue. Search for duplicate issues and existing PRs before creating either. Use a closing keyword only when the PR resolves the issue; use a normal reference for partial work.
- Before a push or PR creation, verify the actual repository URLs, push destination, head branch, and base branch. Do not assume `origin` is the user's fork. Default to a feature branch on the user's fork; direct pushes to Apache upstream require explicit authorization. Do not silently fall back to upstream if the fork is unavailable.
- Before committing or pushing, inspect both the working/staged diff and the full PR diff against the intended base (`git diff <base-ref>...HEAD`). Include only intended changes and exclude credentials, local configuration, build output, and unrelated files. Commit, push, or publish only within the user's authorized scope.
- Preserve shared history. Destructive cleanup, branch deletion, and rewriting published history require explicit authorization; never use these operations to remove user changes or bypass failing checks.
- Use a draft PR or the template's `[WIP]` title convention for unfinished work. Address review feedback with concrete changes and validation, and follow Amoro's own review-resolution guidance rather than another project's convention.
- When AI disclosure is required by the current contribution policy or template, identify the actual tool used. Never claim human review, tests, approvals, or community agreement that have not occurred. Leave merge and release decisions to authorized maintainers.

## Creating Pull Requests and Issues

Repository templates are mandatory when creating or updating a PR or issue, including through `gh`, APIs, and connectors. Do not assume these tools automatically apply the GitHub UI templates.

1. **Read the current template before drafting.** For PRs, use `.github/PULL_REQUEST_TEMPLATE`. For issues, read `.github/ISSUE_TEMPLATE/config.yml` and choose the matching form below. The template files are the source of truth if they change.

   | Issue type | Template | Title prefix | Label |
   |------------|----------|--------------|-------|
   | Bug | `.github/ISSUE_TEMPLATE/bug_report.yml` | `[Bug]: ` | `type:bug` |
   | Improvement | `.github/ISSUE_TEMPLATE/improvement.yml` | `[Improvement]: ` | `type:improvement` |
   | Feature | `.github/ISSUE_TEMPLATE/feature_request.yml` | `[Feature]: ` | `type:feature` |
   | Subtask | `.github/ISSUE_TEMPLATE/subtask.yml` | `[Subtask]: ` | `type:subtask` |

2. **Fill the template completely and truthfully.** Preserve PR headings, testing checklists, and documentation questions. Replace placeholders with actual details and link the related issue when applicable; never submit `Close #xxx` or invent an issue number. For YAML issue forms submitted through CLI/API, convert input fields into Markdown sections in template order, using their labels as headings; preserve checkbox text and supply the selected values. Apply the template's title prefix and labels. Required fields and checkboxes must be satisfied before submission; obtain missing required facts from the user rather than guessing. Mark checks only when supported by actual work or authorization, including searches, tests, contribution commitments, and Code of Conduct agreement. Optional unanswered fields may say `Not provided` or `Not applicable` as appropriate.
3. **Check before submitting.** Compare the final title, body, and labels against the selected template. Complete any required duplicate search before marking it done. Do not bypass a matching template merely because blank issues are enabled. If no template fits, explain the mismatch before choosing another format.
4. **Submit the prepared content explicitly and verify it.** With `gh`, write multiline bodies to a temporary file and pass `--body-file`; with APIs/connectors, pass the full body as a structured argument. After creation or update, read back the saved title, body, and issue labels and correct any formatting or template omissions.

## Key Files to Start With

| Area | File |
|------|------|
| Server entry point | `amoro-ams/src/main/java/.../server/AmoroServiceContainer.java` |
| REST routes | `amoro-ams/src/main/java/.../server/dashboard/DashboardServer.java` |
| Server config keys | `amoro-ams/src/main/java/.../server/AmoroManagementConf.java` |
| Format SPI | `amoro-common/src/main/java/.../FormatCatalogFactory.java` |
| Unified catalog | `amoro-common/src/main/java/.../CommonUnifiedCatalog.java` |
| DB mappers | `amoro-ams/src/main/java/.../server/persistence/mapper/` |
| Plugin system | `amoro-ams/src/main/java/.../server/manager/AbstractPluginManager.java` |
| Default config | `dist/src/main/amoro-bin/conf/config.yaml` |
| Thrift IDL | `amoro-common/src/main/thrift/` |
| Frontend API layer | `amoro-web/src/services/` |

## Running AMS Locally (IDE)

1. Copy config: `mkdir -p conf && cp dist/src/main/amoro-bin/conf/config.yaml conf/config.yaml`
2. Update Derby path in `conf/config.yaml` to use absolute path under `conf/derby`
3. Run `AmoroServiceContainer.main()` — dashboard at http://localhost:1630 (admin/admin)
4. Optimizer: Run `StandaloneOptimizer.main()` with args `-a thrift://127.0.0.1:1261 -p 1 -g local`
