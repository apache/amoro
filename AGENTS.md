# AGENTS.md — Apache Amoro (Incubating)

## Project Overview

Apache Amoro is a **Lakehouse management system** (Java 11, Maven, Vue 3 frontend). The central component is **AMS** (Amoro Management Service), which manages table metadata, self-optimizing, and catalog services for Iceberg, Hive, Paimon, and Hudi table formats.

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

Key Maven profiles: `-Phadoop2` (Hadoop 2.x), `-Pspark-3.3`/`-Pspark-3.5`, `-Psupport-all-formats`, `-Pbuild-mixed-format-trino` (requires JDK 17).

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

