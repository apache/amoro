# Amoro Alerting Module Design

**Date**: 2026-06-02
**Status**: Draft
**Author**: ConradJam

## 1. Overview

Add an `amoro-alerting` module to Amoro AMS that provides a pluggable alerting framework. The module abstracts alert channel interfaces (Feishu, WeChat Work, etc.) via SPI, enabling external notification when optimization operations fail.

The first feature triggers an alert when small-file compaction (optimizing) fails. A `DummyAlertChannel` (log-based) implementation demonstrates the framework.

## 2. Requirements

| Item | Decision |
|------|----------|
| Module name | `amoro-alerting` |
| Module structure | Single JAR module (interfaces in `amoro-common`, implementation in `amoro-alerting`) |
| Trigger timing | Every failure (including retries) |
| Interface pattern | SPI plugin via `AbstractPluginManager` (like `EventsManager`) |
| Alert content | Table identifier, fail reason, phase + retry info, time info |
| Trigger location | `OptimizingQueue` failure points only |
| Configuration | `conf/plugins/alert-channels.yaml` (plugin config convention) |

## 3. Module Structure

### 3.1 New Files

```
amoro-alerting/
├── pom.xml
└── src/main/
    ├── java/org/apache/amoro/alerting/
    │   ├── AlertManager.java              # Extends AbstractPluginManager<AlertChannel>
    │   └── dummy/
    │       └── DummyAlertChannel.java     # Dummy implementation (logs alerts)
    └── resources/
        └── META-INF/services/
            └── org.apache.amoro.alert.AlertChannel

amoro-common/src/main/java/org/apache/amoro/alert/
├── AlertChannel.java          # SPI interface
├── AlertMessage.java          # Alert data class
└── AlertPhase.java            # Failure phase enum

conf/plugins/
└── alert-channels.yaml        # Plugin configuration

dist/src/main/amoro-bin/conf/plugins/
└── alert-channels.yaml        # Distribution template (copied to conf/plugins/ on install)
```

### 3.2 Modified Files

| File | Change |
|------|--------|
| `pom.xml` (root) | Add `amoro-alerting` to `<modules>` |
| `amoro-ams/pom.xml` | Add `amoro-alerting` compile dependency |
| `dist/pom.xml` | Add `amoro-alerting` dependency for distribution |
| `AmoroServiceContainer.java` | Call `AlertManager.getInstance()` during startup, `AlertManager.dispose()` during shutdown |
| `OptimizingQueue.java` | Add alert triggers in `acceptResult()` (task failure), `commit()` (commit failure + empty tasks), and `planInternal()` (plan failure + owner conflict) |

Note: All triggers are in `OptimizingQueue` only. `DefaultTableRuntime` is NOT modified — it already handles state transitions, and the failures are best captured at the `OptimizingQueue` level where full context (retry count, fail reason, phase) is available.

## 4. SPI Interface Design

### 4.1 Interface Hierarchy

Follows the existing `AmoroPlugin → ActivePlugin` plugin system. The `AlertChannel` interface resides in `amoro-common` (where all SPI interfaces are defined), while implementations live in `amoro-alerting` and future sub-modules.

```
AmoroPlugin (existing, in amoro-common)
  └── name(): String
      └── ActivePlugin (existing, in amoro-common)
            ├── open(Map<String,String> properties)
            └── close()
                └── AlertChannel (new, in amoro-common)
                      └── send(AlertMessage): void
```

### 4.2 AlertChannel Interface

```java
package org.apache.amoro.alert;

/**
 * SPI interface for alert channels.
 * Implementations are discovered via ServiceLoader through AbstractPluginManager.
 * Configuration is loaded from conf/plugins/alert-channels.yaml.
 */
public interface AlertChannel extends ActivePlugin {
  /**
   * Send an alert message through this channel.
   * Implementations should handle exceptions internally and not throw.
   */
  void send(AlertMessage message);
}
```

### 4.3 AlertMessage Data Class

```java
package org.apache.amoro.alert;

public class AlertMessage {
  private String tableIdentifier;   // e.g. "catalog_a.db.table_b"
  private AlertPhase phase;         // PLAN, EXECUTE, or COMMIT
  private String failReason;        // Failure reason summary
  private int retryCount;           // Current retry count (0 for PLAN/COMMIT)
  private int maxRetryCount;        // Maximum retry count
  private long startTimeMs;         // When the failure occurred
  private long durationMs;          // Duration of the failed operation

  // Builder pattern for construction
  public static Builder builder() { ... }
}
```

### 4.4 AlertPhase Enum

```java
package org.apache.amoro.alert;

public enum AlertPhase {
  PLAN,      // Optimizing plan failed
  EXECUTE,   // Task execution failed
  COMMIT     // Commit phase failed
}
```

## 5. AlertManager

`AlertManager` extends `AbstractPluginManager<AlertChannel>` and follows the **exact same pattern** as `EventsManager` — singleton, async dispatch via thread pool, plugin config from `conf/plugins/`.

```java
package org.apache.amoro.alerting;

public class AlertManager extends AbstractPluginManager<AlertChannel> {

  public static final String PLUGIN_TYPE = "alert-channels";
  private static volatile AlertManager INSTANCE;
  private Executor pluginVisitorPool;

  /** Get the singleton instance. Lazy-initialized on first access. */
  public static AlertManager getInstance() {
    if (INSTANCE == null) {
      synchronized (AlertManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new AlertManager();
          INSTANCE.initialize();
        }
      }
    }
    return INSTANCE;
  }

  /** Close the manager during AMS shutdown. */
  public static void dispose() {
    synchronized (AlertManager.class) {
      if (INSTANCE != null) {
        INSTANCE.close();
      }
      INSTANCE = null;
    }
  }

  public AlertManager() {
    super(PLUGIN_TYPE);
  }

  @Override
  public void initialize() {
    super.initialize();
    this.pluginVisitorPool =
        new ThreadPoolExecutor(
            0, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            runnable -> {
              Thread thread = new Thread(runnable);
              thread.setName("PluginVisitor-" + pluginCategory() + "-0");
              thread.setDaemon(true);
              return thread;
            });
  }

  @Override
  protected String pluginCategory() {
    return PLUGIN_TYPE;
  }

  /**
   * Send alert to all installed channels asynchronously.
   * Uses the same async dispatch pattern as EventsManager.emit().
   * Failures in individual channels are caught by AbstractPluginManager.forEach().
   */
  public void send(AlertMessage message) {
    this.pluginVisitorPool.execute(
        () -> forEach(channel -> channel.send(message)));
  }
}
```

### Key Design Decisions

1. **Extends AbstractPluginManager**: Reuses SPI discovery, external plugin JAR loading, config-driven installation, and install/uninstall lifecycle. No custom ServiceLoader code.
2. **Singleton pattern**: Same as `EventsManager.getInstance()`. No need for constructor injection — `OptimizingQueue` accesses it via `AlertManager.getInstance()`.
3. **Async dispatch**: `send()` submits to a single-thread daemon pool, identical to `EventsManager.emit()`. This ensures alerts never block the optimization scheduling thread, even inside lock-held code paths.
4. **Fail-safe**: `AbstractPluginManager.forEach()` catches per-channel exceptions. The main AMS flow is never affected.
5. **No-op when no channels**: If no channels are installed (config absent or all disabled), `forEach()` iterates over zero plugins. Zero overhead.

## 6. Dummy Implementation

```java
package org.apache.amoro.alerting.dummy;

public class DummyAlertChannel implements AlertChannel {
  private static final Logger LOG = LoggerFactory.getLogger(DummyAlertChannel.class);

  @Override
  public String name() {
    return "dummy";
  }

  @Override
  public void open(Map<String, String> properties) {
    LOG.info("DummyAlertChannel opened");
  }

  @Override
  public void send(AlertMessage message) {
    LOG.warn("[ALERT] Table: {} | Phase: {} | Retry: {}/{} | Reason: {} | Time: {}",
        message.getTableIdentifier(),
        message.getPhase(),
        message.getRetryCount(),
        message.getMaxRetryCount(),
        message.getFailReason(),
        Instant.ofEpochMilli(message.getStartTimeMs()));
  }

  @Override
  public void close() {
    LOG.info("DummyAlertChannel closed");
  }
}
```

SPI registration file at `META-INF/services/org.apache.amoro.alert.AlertChannel`:
```
org.apache.amoro.alerting.dummy.DummyAlertChannel
```

## 7. Trigger Points

All triggers are in `OptimizingQueue`. The `AlertManager.getInstance().send()` call is safe to make inside lock-held code because `send()` dispatches to an async thread pool (never blocks the caller).

### 7.1 Task Execution Failure (Every Failure Including Retries)

**Location**: `OptimizingQueue.TableOptimizingProcess.acceptResult()` — inside the `FAILED` status branch (line ~945)

```java
// When taskRuntime.getStatus() == TaskRuntime.Status.FAILED
AlertManager.getInstance().send(AlertMessage.builder()
    .tableIdentifier(tableRuntime.getTableIdentifier().toString())
    .phase(AlertPhase.EXECUTE)
    .failReason(taskRuntime.getFailReason())
    .retryCount(taskRuntime.getRetry())
    .maxRetryCount(tableRuntime.getOptimizingConfig().getMaxExecuteRetryCount())
    .startTimeMs(taskRuntime.getStartTime())
    .durationMs(taskRuntime.getEndTime() - taskRuntime.getStartTime())
    .build());
```

This fires on every task failure — both retried failures and final failures after max retry exhausted.

### 7.2 Commit Failure

**Location**: `OptimizingQueue.TableOptimizingProcess.commit()` — two failure paths:

**Path A — No successful tasks** (line ~1087):
```java
if (successTasks.isEmpty()) {
  AlertManager.getInstance().send(AlertMessage.builder()
      .tableIdentifier(tableRuntime.getTableIdentifier().toString())
      .phase(AlertPhase.COMMIT)
      .failReason("No successful task is available for commit")
      .retryCount(0)
      .maxRetryCount(0)
      .startTimeMs(System.currentTimeMillis())
      .durationMs(0)
      .build());
  // ... existing failure handling (status = FAILED, persistAndSetCompleted)
}
```

**Path B — Commit exception** (line ~1111, generic Throwable catch):
```java
} catch (Throwable t) {
  LOG.error("{} Commit optimizing failed", tableRuntime.getTableIdentifier(), t);
  AlertManager.getInstance().send(AlertMessage.builder()
      .tableIdentifier(tableRuntime.getTableIdentifier().toString())
      .phase(AlertPhase.COMMIT)
      .failReason(ExceptionUtil.getErrorMessage(t, 4000))
      .retryCount(0)
      .maxRetryCount(0)
      .startTimeMs(System.currentTimeMillis())
      .durationMs(0)
      .build());
  // ... existing failure handling
}
```

Note: `PersistenceException` (line ~1106) is **excluded** from alerting — it is a retryable persistence issue that does not represent an optimizing failure.

### 7.3 Plan Failure

**Location**: `OptimizingQueue.planInternal()` — two failure paths:

**Path A — OptimizingOwnerConflictException** (line ~616):
```java
catch (OptimizingOwnerConflictException e) {
  tableRuntime.planFailed();
  LOG.info("Skip planning table {} because optimizing owner {} is already held", ...);
  AlertManager.getInstance().send(AlertMessage.builder()
      .tableIdentifier(tableRuntime.getTableIdentifier().toString())
      .phase(AlertPhase.PLAN)
      .failReason("Optimizing owner conflict: " + e.getMessage())
      .retryCount(0)
      .maxRetryCount(0)
      .startTimeMs(System.currentTimeMillis())
      .durationMs(0)
      .build());
  return null;
}
```

**Path B — Generic planning exception** (line ~633):
```java
} catch (Throwable throwable) {
  tableRuntime.planFailed();
  LOG.error("Planning table {} failed", tableRuntime.getTableIdentifier(), throwable);
  AlertManager.getInstance().send(AlertMessage.builder()
      .tableIdentifier(tableRuntime.getTableIdentifier().toString())
      .phase(AlertPhase.PLAN)
      .failReason(ExceptionUtil.getErrorMessage(throwable, 4000))
      .retryCount(0)
      .maxRetryCount(0)
      .startTimeMs(System.currentTimeMillis())
      .durationMs(0)
      .build());
  throw throwable;
}
```

## 8. Configuration

### 8.1 Plugin Config File

Create `conf/plugins/alert-channels.yaml` following the existing plugin configuration convention:

```yaml
alert-channels:
  - name: dummy
    enabled: true
  # Future extensions:
  # - name: feishu
  #   enabled: true
  #   properties:
  #     webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
  # - name: wechat
  #   enabled: true
  #   properties:
  #     webhook-url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
```

This file is loaded by `AbstractPluginManager.loadPluginConfigurations()` automatically. When the file is absent or no channels are enabled, `AlertManager` operates with zero channels (all `send()` calls are no-ops).

### 8.2 Lifecycle in AmoroServiceContainer

```java
// In AmoroServiceContainer.startService():
AlertManager.getInstance();  // Lazy-init triggers config loading

// In AmoroServiceContainer.stopService():
AlertManager.dispose();      // Close all channels
```

## 9. Error Handling

1. **AlertChannel.open() failure**: Caught by `AbstractPluginManager.install()`, throws `LoadingPluginException`. The channel is not installed. AMS continues without that channel.
2. **AlertChannel.send() failure**: Caught by `AbstractPluginManager.forEach()`. Logged as error. Does not affect other channels or the main AMS flow.
3. **AlertManager initialization failure**: If config loading fails, `LoadingPluginException` propagates. For production robustness, the caller should catch and fall back to a no-op manager.
4. **No channels configured**: `forEach()` iterates over zero plugins. `send()` submits a no-op task to the thread pool. Minimal overhead.
5. **Lock contention**: `send()` dispatches to an async thread pool and returns immediately. It never blocks the calling thread, even when called inside a `ReentrantLock` block in `acceptResult()` or `commit()`.

## 10. Test Strategy

### 10.1 Unit Tests

| Test | Location | Description |
|------|----------|-------------|
| `DummyAlertChannelTest` | `amoro-alerting/src/test/` | Verify `send()` logs expected format; verify `open()/close()` lifecycle |
| `AlertMessageTest` | `amoro-common/src/test/` | Verify builder, getters, and field completeness |

### 10.2 Integration Tests

| Test | Location | Description |
|------|----------|-------------|
| `AlertManagerIntegrationTest` | `amoro-ams/src/test/` | Verify `AlertManager` loads `DummyAlertChannel` via SPI, installs it, and `send()` triggers the channel |
| `OptimizingAlertTest` | `amoro-ams/src/test/` | Mock an optimizing failure scenario, verify `AlertManager.getInstance().send()` is called with correct `AlertPhase` and message content |

### 10.3 Manual Verification

Run AMS locally, trigger an optimizing plan failure (e.g., configure an invalid table), observe `[ALERT]` log output from `DummyAlertChannel`.

## 11. Future Extensions

This design supports the following future work without changing existing code:

1. **Feishu channel**: Drop a JAR containing `FeishuAlertChannel implements AlertChannel` into the `conf/plugins/alert-channels/` directory. Add entry in `alert-channels.yaml`. No code changes to AMS.
2. **WeChat Work channel**: Same pattern as Feishu.
3. **Alert rules**: Filter which tables/phases trigger alerts (e.g., only alert on COMMIT failures for production catalogs).
4. **Rate limiting**: Add dedup/throttle logic inside `AlertManager.send()` to prevent alert storms.
5. **Dashboard integration**: Show recent alerts in the AMS Dashboard UI.

**Important**: Future channel implementations (Feishu, WeChat) should be packaged as **separate JARs dropped into the plugin directory** (`conf/plugins/alert-channels/`), NOT added as Maven compile dependencies of `amoro-ams`. This is how `AbstractPluginManager` discovers external plugins — via `URLClassLoader` from the plugin directory.

## 12. Out of Scope

- Alert rules/filtering (alert on all failures for now)
- Rate limiting or deduplication
- Dashboard UI for alert history
- Email alerting
- Converting `amoro-alerting` to a parent POM with sub-modules (single module is sufficient for now; split only when external dependencies justify it)
