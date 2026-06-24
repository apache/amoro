# Paimon Primary Key HASH Compaction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Paimon 主键表 `HASH_FIXED` / `HASH_DYNAMIC` 增加独立自优化链路，支持 Amoro `MINOR / MAJOR / FULL` 语义，同时保持现有 APPEND 表优化行为不变。

**Architecture:** 新增主键表专用 Planner、Task/Input/Output、Executor、Committer 和 recovery 映射。Planner 使用 Paimon `SnapshotReader.bucketEntries()` 做轻量候选判断，`partition-idle-time` 只作用于 FULL 冷却候选；Executor 使用 `write-only=false` 的 Paimon table copy 调用 `BatchTableWrite.compact(partition,bucket,fullCompaction)` 并 `prepareCommit()`，FULL 执行前二次校验 latest snapshot 未变化；Committer 使用 `write-only=false` 的 table copy 和 Paimon `StreamTableCommit.filterAndCommit` 以单个 process commit identifier 提交并过滤 replay，FULL 提交前再次校验 latest snapshot，空 commit messages 视为合法 no-op。现有 APPEND 链路继续使用 `PaimonOptimizingPlanner / PaimonCompactionExecutor / PaimonTableCommit`。

**Tech Stack:** Java 11, Maven, JUnit 5, Apache Paimon public API, Amoro optimizing SPI, `amoro-format-paimon`, `amoro-ams` recovery converters。

---

## Spec 审查结论

已审查：

- `/Users/SL/.codex/worktrees/e09a/amoro/docs/superpowers/specs/2026-06-18-paimon-primary-key-hash-compaction-design.md`
- `/Users/SL/.codex/worktrees/e09a/amoro/docs/superpowers/specs/2026-06-17-paimon-hash-compaction-exploration.md`
- 当前分支 `amoro-format-paimon` 现有 APPEND 优化链路
- 当前分支 `TaskDescriptorRecoveryTypes` recovery 映射

关键补充：

1. Spec 方向成立，但实现必须修改 `PaimonTable.evaluatePendingInput()`，否则主键表即使有新 Planner，也不会进入 `OptimizingQueue`。
2. 主键表 committer 必须与 APPEND committer 分离。Executor 参考 Spark procedure 的 compact / prepareCommit 模式；AMS committer 使用 `StreamTableCommit.filterAndCommit`，避免 `BatchTableCommit.commit(messages)` 在 AMS replay 时重复提交同一 compact output。
3. 主键表 input 应复用 APPEND input 的 commit identity 思路：每个 process 固定 `commitUser` 与 `commitIdentifier=processId`，但不能复用 APPEND `PaimonCompactionInput` 类型。
4. Paimon API 版本必须以本仓库依赖实际编译结果为准；若 `BucketEntry` / `PartitionEntry` / `BatchTableWrite` 方法签名与外部源码不同，按编译错误调整，但不得改设计语义。
5. Paimon Spark Procedure 会强制 compact 使用 `write-only=false`；Amoro 主键 executor / committer 必须同样使用 table copy 覆盖 `write-only=false`。
6. Paimon compact 可能返回空 commit messages，这是合法 no-op；提交端不能把空 messages 当作失败。
7. FULL 的冷却判断需要执行与提交前双重保护；如果 latest snapshot 已不同于 plan 的 `targetSnapshotId`，FULL task / commit 必须 no-op。

## File Structure

### 新增文件

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyOptions.java`  
  主键表私有配置常量、解析和校验。`partition-idle-time` 需要支持 Paimon duration（如 `10s`、`5 min`）并兼容 ISO-8601（如 `PT30M`）。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonBucketCompactionUnit.java`  
  一个 `(partition,bucket)` 的轻量候选单元，保存 serialized partition 与 `BucketEntry` 指标。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionInput.java`  
  Optimizer 侧执行输入，携带 `PaimonTable`、bucket units、`OptimizingType`、`fullCompaction`、snapshot 和 commit identity。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionOutput.java`  
  Optimizer 侧输出，携带 serialized Paimon commit messages 和主键表 compact metrics。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionTask.java`  
  主键表专用 `StagedTaskDescriptor`。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutor.java`  
  对 task 内多个 bucket 循环调用 `BatchTableWrite.compact(BinaryRow partition, int bucket, boolean fullCompaction)`。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutorFactory.java`  
  Optimizer 反射创建 executor 的 factory。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyTableCommit.java`  
  AMS 侧主键表专用 committer，反序列化 commit messages 并用 `StreamTableCommit.filterAndCommit` 按 process commit identity 幂等提交。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonPrimaryKeyOptimizingPlanner.java`  
  主键表独立 Planner。

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionTask.java`

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionOutput.java`

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionExecutor.java`

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyTableCommit.java`

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPrimaryKeyOptimizingPlanner.java`

### 修改文件

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTable.java`  
  将开启主键自优化的 `HASH_FIXED/HASH_DYNAMIC` 主键表标记为 optimizing necessary。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/process/PaimonProcessFactory.java`  
  路由 APPEND planner / committer 与主键表 planner / committer。

- `amoro-ams/src/main/java/org/apache/amoro/server/persistence/converter/TaskDescriptorRecoveryTypes.java`  
  注册主键表 executor factory 到 task/input/output/summary 类型映射。

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTableDescriptor.java`  
  增加 `MAJOR` 优化类型展示。

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTablePendingInputEligibility.java`  
  覆盖主键表默认不入队、开启后入队。

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/process/TestPaimonProcessFactory.java`  
  覆盖 planner / committer 路由。

- `amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestPaimonOptimizingE2E.java`  
  增加主键 task recovery 映射验证。

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTableDescriptorOptimizingProcesses.java`  
  覆盖 descriptor `MAJOR` 类型。

---

### Task 1: 主键表配置与候选单元类型

**Files:**
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyOptions.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonBucketCompactionUnit.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyOptions.java`

- [ ] **Step 1: Write failing tests for option parsing**

Create `TestPaimonPrimaryKeyOptions.java`:

```java
package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class TestPaimonPrimaryKeyOptions {

  @Test
  void defaultsKeepPrimaryKeyOptimizingDisabled() {
    PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(new HashMap<>());

    assertFalse(options.enabled());
    assertEquals(16, options.maxBucketsPerTask());
    assertFalse(options.partitionIdleTime().isPresent());
    assertFalse(options.majorFileCountThreshold().isPresent());
  }

  @Test
  void parsesPrimaryKeySpecificOptions() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonPrimaryKeyOptions.ENABLED, "true");
    props.put(PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "8");
    props.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT30M");
    props.put(PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD, "12");

    PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(props);

    assertTrue(options.enabled());
    assertEquals(8, options.maxBucketsPerTask());
    assertEquals(Duration.ofMinutes(30), options.partitionIdleTime().orElseThrow(AssertionError::new));
    assertEquals(12L, options.majorFileCountThreshold().orElseThrow(AssertionError::new));
  }

  @Test
  void rejectsInvalidMaxBucketsPerTask() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "0");

    assertThrows(IllegalArgumentException.class, () -> PaimonPrimaryKeyOptions.from(props));
  }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyOptions
```

Expected: FAIL because `PaimonPrimaryKeyOptions` does not exist.

- [ ] **Step 3: Implement option parser and bucket unit**

Create `PaimonPrimaryKeyOptions.java`:

```java
package org.apache.amoro.formats.paimon.optimizing.primary;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class PaimonPrimaryKeyOptions {

  public static final String ENABLED = "paimon-optimizer.primary-key.enabled";
  public static final String MAX_BUCKETS_PER_TASK =
      "paimon-optimizer.primary-key.max-buckets-per-task";
  public static final String PARTITION_IDLE_TIME =
      "paimon-optimizer.primary-key.partition-idle-time";
  public static final String MAJOR_FILE_COUNT_THRESHOLD =
      "paimon-optimizer.primary-key.major.file-count-threshold";

  private final boolean enabled;
  private final int maxBucketsPerTask;
  private final Duration partitionIdleTime;
  private final Long majorFileCountThreshold;

  private PaimonPrimaryKeyOptions(
      boolean enabled,
      int maxBucketsPerTask,
      Duration partitionIdleTime,
      Long majorFileCountThreshold) {
    if (maxBucketsPerTask <= 0) {
      throw new IllegalArgumentException(MAX_BUCKETS_PER_TASK + " must be greater than 0.");
    }
    this.enabled = enabled;
    this.maxBucketsPerTask = maxBucketsPerTask;
    this.partitionIdleTime = partitionIdleTime;
    this.majorFileCountThreshold = majorFileCountThreshold;
  }

  public static PaimonPrimaryKeyOptions from(Map<String, String> properties) {
    Map<String, String> props = properties == null ? java.util.Collections.emptyMap() : properties;
    boolean enabled = Boolean.parseBoolean(props.getOrDefault(ENABLED, "false"));
    int maxBucketsPerTask = Integer.parseInt(props.getOrDefault(MAX_BUCKETS_PER_TASK, "16"));
    Duration partitionIdleTime =
        props.containsKey(PARTITION_IDLE_TIME) ? Duration.parse(props.get(PARTITION_IDLE_TIME)) : null;
    Long majorThreshold =
        props.containsKey(MAJOR_FILE_COUNT_THRESHOLD)
            ? Long.parseLong(props.get(MAJOR_FILE_COUNT_THRESHOLD))
            : null;
    return new PaimonPrimaryKeyOptions(enabled, maxBucketsPerTask, partitionIdleTime, majorThreshold);
  }

  public boolean enabled() {
    return enabled;
  }

  public int maxBucketsPerTask() {
    return maxBucketsPerTask;
  }

  public Optional<Duration> partitionIdleTime() {
    return Optional.ofNullable(partitionIdleTime);
  }

  public Optional<Long> majorFileCountThreshold() {
    return Optional.ofNullable(majorFileCountThreshold);
  }
}
```

Create `PaimonBucketCompactionUnit.java`:

```java
package org.apache.amoro.formats.paimon.optimizing.primary;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.io.Serializable;

public class PaimonBucketCompactionUnit implements Serializable {

  private static final long serialVersionUID = 1L;

  private byte[] partitionBytes;
  private int bucket;
  private long fileCount;
  private long fileSizeInBytes;
  private long recordCount;
  private long lastFileCreationTime;

  public PaimonBucketCompactionUnit() {}

  public PaimonBucketCompactionUnit(
      byte[] partitionBytes,
      int bucket,
      long fileCount,
      long fileSizeInBytes,
      long recordCount,
      long lastFileCreationTime) {
    this.partitionBytes = partitionBytes;
    this.bucket = bucket;
    this.fileCount = fileCount;
    this.fileSizeInBytes = fileSizeInBytes;
    this.recordCount = recordCount;
    this.lastFileCreationTime = lastFileCreationTime;
  }

  public byte[] getPartitionBytes() {
    return partitionBytes;
  }

  public int getBucket() {
    return bucket;
  }

  public long getFileCount() {
    return fileCount;
  }

  public long getFileSizeInBytes() {
    return fileSizeInBytes;
  }

  public long getRecordCount() {
    return recordCount;
  }

  public long getLastFileCreationTime() {
    return lastFileCreationTime;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("bucket", bucket)
        .add("fileCount", fileCount)
        .add("fileSizeInBytes", fileSizeInBytes)
        .add("recordCount", recordCount)
        .add("lastFileCreationTime", lastFileCreationTime)
        .toString();
  }
}
```

- [ ] **Step 4: Run test and verify it passes**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyOptions
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyOptions.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonBucketCompactionUnit.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyOptions.java
git commit -m "feat: add paimon primary key optimizing options"
```

### Task 2: 主键表 Task/Input/Output/ExecutorFactory 类型

**Files:**
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionInput.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionOutput.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionTask.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutorFactory.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionTask.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionOutput.java`

- [ ] **Step 1: Write failing serialization and summary tests**

Create `TestPaimonPrimaryKeyCompactionOutput.java`:

```java
package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

class TestPaimonPrimaryKeyCompactionOutput {

  @Test
  void summaryContainsBucketAndFileMetrics() {
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(
            Arrays.asList(new byte[] {1}, new byte[] {2}), 1, 2L, 9L, 900L, 30L, 3L, 300L);

    Map<String, String> summary = output.summary();

    assertEquals("2", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BUCKETS));
    assertEquals("9", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_FILES));
    assertEquals("900", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BYTES));
    assertEquals("30", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_RECORDS));
    assertEquals("3", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_FILES));
    assertEquals("300", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_BYTES));
  }
}
```

Create `TestPaimonPrimaryKeyCompactionTask.java`:

```java
package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class TestPaimonPrimaryKeyCompactionTask {

  @Test
  void descriptorDeserializesOutputAndBuildsSummary() {
    PaimonPrimaryKeyCompactionInput input =
        new PaimonPrimaryKeyCompactionInput(
            (PaimonTable) null,
            Collections.singletonList(new PaimonBucketCompactionUnit(new byte[0], 0, 5L, 500L, 10L, 1L)),
            OptimizingType.MAJOR,
            true,
            7L,
            "user",
            11L);
    Map<String, String> props = new HashMap<>();
    props.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
    PaimonPrimaryKeyCompactionTask task =
        new PaimonPrimaryKeyCompactionTask(1L, "primary-key-buckets", input, props);
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(Collections.emptyList(), 1, 1L, 5L, 500L, 10L, 2L, 200L);

    task.setOutput(SerializationUtil.simpleSerialize(output));

    assertTrue(task.getOutput() instanceof PaimonPrimaryKeyCompactionOutput);
    assertEquals("5", task.toMetricsSummary().summary(false).get("input-data-files"));
    assertEquals("200", task.toMetricsSummary().summary(false).get("output-data-size"));
  }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyCompactionTask,TestPaimonPrimaryKeyCompactionOutput
```

Expected: FAIL because task/input/output/factory classes do not exist.

- [ ] **Step 3: Implement input/output/task/factory classes**

Implement `PaimonPrimaryKeyCompactionInput` extending `BaseOptimizingInput`, with fields:

```java
private PaimonTable table;
private List<PaimonBucketCompactionUnit> units;
private OptimizingType optimizingType;
private boolean fullCompaction;
private long targetSnapshotId;
private String commitUser;
private long commitIdentifier;
```

Implement `describe()` as:

```java
return String.format(
    "Amoro paimon primary-key compaction task, table:%s, type:%s, buckets:%d",
    table == null ? "<unknown>" : table.id(),
    optimizingType,
    units == null ? 0 : units.size());
```

Implement `PaimonPrimaryKeyCompactionOutput` with constants:

```java
public static final String COMPACTED_BUCKETS = "compacted-buckets";
public static final String COMPACTED_FILES = "compacted-files";
public static final String COMPACTED_BYTES = "compacted-bytes";
public static final String COMPACTED_RECORDS = "compacted-records";
public static final String PRODUCED_FILES = "produced-files";
public static final String PRODUCED_BYTES = "produced-bytes";
```

Implement dashboard-compatible summary keys matching APPEND output:

```java
summary.put("input-data-files(rewrite)", Long.toString(compactedFileCount));
summary.put("input-data-size(rewrite)", Long.toString(compactedFileSize));
summary.put("output-data-files", Long.toString(producedFileCount));
summary.put("output-data-size", Long.toString(producedFileSize));
```

Implement `PaimonPrimaryKeyCompactionTask` extending:

```java
StagedTaskDescriptor<
    PaimonPrimaryKeyCompactionInput,
    PaimonPrimaryKeyCompactionOutput,
    PaimonMetricsSummary>
```

`calculateSummary()` must copy compacted/produced file counts and sizes from output into `PaimonMetricsSummary`.

Implement `PaimonPrimaryKeyCompactionExecutorFactory`:

```java
public class PaimonPrimaryKeyCompactionExecutorFactory
    implements OptimizingExecutorFactory<PaimonPrimaryKeyCompactionInput> {

  @Override
  public OptimizingExecutor<PaimonPrimaryKeyCompactionOutput> createExecutor(
      PaimonPrimaryKeyCompactionInput input) {
    return new PaimonPrimaryKeyCompactionExecutor(input);
  }
}
```

Create a temporary `PaimonPrimaryKeyCompactionExecutor` skeleton returning unsupported exception if Task 3 has not created it yet:

```java
public class PaimonPrimaryKeyCompactionExecutor
    implements OptimizingExecutor<PaimonPrimaryKeyCompactionOutput> {
  private final PaimonPrimaryKeyCompactionInput input;
  public PaimonPrimaryKeyCompactionExecutor(PaimonPrimaryKeyCompactionInput input) {
    this.input = input;
  }
  @Override
  public PaimonPrimaryKeyCompactionOutput execute() {
    throw new UnsupportedOperationException("Implemented in executor task");
  }
}
```

- [ ] **Step 4: Run tests and verify they pass**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyCompactionTask,TestPaimonPrimaryKeyCompactionOutput
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionTask.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionOutput.java
git commit -m "feat: add paimon primary key compaction task model"
```

### Task 3: 主键表 Planner 与入队条件

**Files:**
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonPrimaryKeyOptimizingPlanner.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTable.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPrimaryKeyOptimizingPlanner.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTablePendingInputEligibility.java`

- [ ] **Step 1: Write failing planner tests**

Create `TestPaimonPrimaryKeyOptimizingPlanner.java` with these helpers:

```java
@Test
void hashFixedMinorUsesEffectiveMinorThreshold(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table = createPrimaryKeyTable(catalog, "t_minor", mapOf("bucket", "2"));
  writePrimaryKeyCommits(table, 5);
  PaimonTable paimonTable = wrap(table, "t_minor");
  OptimizingConfig config = new OptimizingConfig().setMinorLeastFileCount(5).setMinorLeastInterval(0);

  PaimonPrimaryKeyOptimizingPlanner planner =
      new PaimonPrimaryKeyOptimizingPlanner(
          paimonTable, 1L, 99L, 8.0, 64L * 1024 * 1024, config, 0L, 0L, 0L);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result = planner.plan();

  assertEquals(OptimizingType.MINOR, result.getOptimizingType());
  assertFalse(result.getTasks().isEmpty());
  assertFalse(result.getTasks().iterator().next().getInput().isFullCompaction());
}
```

Add these tests with the same helper methods:

```java
@Test
void hashFixedMajorOverridesMinorWhenStopThresholdReached(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table = createPrimaryKeyTable(catalog, "t_major", mapOf("bucket", "2"));
  writePrimaryKeyCommits(table, 8);
  PaimonPrimaryKeyOptimizingPlanner planner =
      planner(wrap(table, "t_major"), new OptimizingConfig().setMinorLeastFileCount(5), 0L, 0L, 0L);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result = planner.plan();

  assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
  assertTrue(result.getTasks().iterator().next().getInput().isFullCompaction());
}

@Test
void hashDynamicUsesSamePlannerLogic(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table =
      createPrimaryKeyTable(
          catalog,
          "t_dynamic",
          mapOf("bucket", "-1", "dynamic-bucket.target-row-num", "1"));
  writePrimaryKeyCommits(table, 5);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_dynamic"), new OptimizingConfig().setMinorLeastFileCount(5), 0L, 0L, 0L)
          .plan();

  assertEquals(OptimizingType.MINOR, result.getOptimizingType());
  assertFalse(result.getTasks().isEmpty());
}

@Test
void nonEmptyFilterRejectsPlanning(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table = createPrimaryKeyTable(catalog, "t_filter", mapOf("bucket", "2"));
  writePrimaryKeyCommits(table, 5);
  OptimizingConfig config = new OptimizingConfig().setMinorLeastFileCount(5).setFilter("dt='2026-06-18'");

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_filter"), config, 0L, 0L, 0L).plan();

  assertTrue(result.getTasks().isEmpty());
}

@Test
void fullRequiresPartitionIdleTime(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table = createPrimaryKeyTable(catalog, "t_full_no_idle", mapOf("bucket", "2"));
  writePrimaryKeyCommits(table, 1);
  OptimizingConfig config = new OptimizingConfig().setMinorLeastFileCount(100).setFullTriggerInterval(1);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_full_no_idle"), config, 0L, 0L, 0L).plan();

  assertTrue(result.getTasks().isEmpty());
}

@Test
void fullRunsOnlyWhenNoMinorOrMajorCandidate(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table =
      createPrimaryKeyTable(
          catalog,
          "t_full_idle",
          mapOf("bucket", "2", PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT0S"));
  writePrimaryKeyCommits(table, 1);
  OptimizingConfig config = new OptimizingConfig().setMinorLeastFileCount(100).setFullTriggerInterval(1);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_full_idle"), config, 0L, 0L, 0L).plan();

  assertEquals(OptimizingType.FULL, result.getOptimizingType());
}

@Test
void packsAtMostConfiguredBucketsPerTask(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table =
      createPrimaryKeyTable(
          catalog,
          "t_pack",
          mapOf("bucket", "4", PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "1"));
  writePrimaryKeyCommits(table, 8);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_pack"), new OptimizingConfig().setMinorLeastFileCount(1), 0L, 0L, 0L)
          .plan();

  assertTrue(result.getTasks().size() > 1);
  result.getTasks().forEach(task -> assertTrue(task.getInput().getUnits().size() <= 1));
}

@Test
void majorThresholdOverrideMustNotBeBelowMinor(@TempDir Path warehouse) throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  Table table =
      createPrimaryKeyTable(
          catalog,
          "t_bad_major",
          mapOf("bucket", "2", PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD, "2"));
  writePrimaryKeyCommits(table, 5);

  OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
      planner(wrap(table, "t_bad_major"), new OptimizingConfig().setMinorLeastFileCount(5), 0L, 0L, 0L)
          .plan();

  assertTrue(result.getTasks().isEmpty());
}
```

Use primary-key table helper:

```java
private static Table createPrimaryKeyTable(
    Catalog catalog, String tableName, Map<String, String> options) throws Exception {
  catalog.createDatabase("db1", true);
  Schema.Builder builder =
      Schema.newBuilder()
          .column("id", DataTypes.INT())
          .column("name", DataTypes.STRING())
          .primaryKey("id");
  options.forEach(builder::option);
  Identifier id = Identifier.create("db1", tableName);
  catalog.createTable(id, builder.build(), true);
  return catalog.getTable(id);
}
```

Use dynamic bucket options:

```java
mapOf("bucket", "-1", "dynamic-bucket.target-row-num", "1")
```

- [ ] **Step 2: Add failing pending-input tests**

Modify `TestPaimonTablePendingInputEligibility.java`:

```java
@Test
@DisplayName("primary-key HASH_FIXED table is optimizing necessary when primary-key optimizer is enabled")
void primaryKeyHashFixedTableIsOptimizingNecessaryWhenEnabled(@TempDir Path warehouse)
    throws Exception {
  Catalog catalog = fsCatalog(warehouse);
  catalog.createDatabase("db1", true);
  Schema schema =
      Schema.newBuilder()
          .column("id", DataTypes.INT())
          .column("name", DataTypes.STRING())
          .primaryKey("id")
          .option("bucket", "2")
          .option(PaimonPrimaryKeyOptions.ENABLED, "true")
          .build();
  Identifier id = Identifier.create("db1", "t_pk_enabled");
  catalog.createTable(id, schema, true);
  PaimonTable paimonTable = wrap(catalog.getTable(id), "t_pk_enabled");

  PendingInputResult result =
      paimonTable.evaluatePendingInput(optimizationContext(true), 10).orElseThrow(AssertionError::new);

  assertTrue(result.optimizingNecessary());
}
```

- [ ] **Step 3: Run tests and verify they fail**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyOptimizingPlanner,TestPaimonTablePendingInputEligibility
```

Expected: FAIL because planner and pending input support do not exist.

- [ ] **Step 4: Implement planner**

Implement `PaimonPrimaryKeyOptimizingPlanner`:

```java
public class PaimonPrimaryKeyOptimizingPlanner
    implements TableOptimizingPlanner<PaimonPrimaryKeyCompactionTask> {
```

Constructor fields match `PaimonOptimizingPlanner`:

```java
PaimonTable paimonTable;
long tableId;
long processId;
double availableCore;
long maxInputSizePerThread;
OptimizingConfig optimizingConfig;
long lastMinorOptimizingTime;
long lastMajorOptimizingTime;
long lastFullOptimizingTime;
```

Planning algorithm:

```java
FileStoreTable table = requirePrimaryKeyHashTable(paimonTable.originalTable());
PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(paimonTable.properties());
if (!options.enabled()) return emptyResult();
if (optimizingConfig != null && optimizingConfig.getFilter() != null && !optimizingConfig.getFilter().isEmpty()) {
  LOG.warn("Paimon primary-key self-optimizing does not support self-optimizing.filter in this version; skip planning to avoid silently ignoring filter.");
  return emptyResult();
}
List<BucketEntry> buckets = table.newSnapshotReader().bucketEntries();
List<PaimonBucketCompactionUnit> units = toBucketUnits(bucketEntries);
List<PaimonBucketCompactionUnit> major = selectMajor(units, effectiveMinor, effectiveMajor);
if (!major.isEmpty()) return plan(OptimizingType.MAJOR, true, major);
List<PaimonBucketCompactionUnit> minor = selectMinor(units, effectiveMinor);
if (!minor.isEmpty() && reachMinorInterval()) return plan(OptimizingType.MINOR, false, minor);
if (reachFullInterval()) return planFullIfIdleConfigured(units, partitionEntries, options.partitionIdleTime());
return emptyResult();
```

Use `org.apache.paimon.utils.SerializationUtils.serializeBinaryRow(entry.partition())` and deserialize in executor.

Compute thresholds:

```java
int effectiveMinor =
    explicitlyConfigured(table.options(), "num-sorted-run.compaction-trigger")
        ? table.coreOptions().numSortedRunCompactionTrigger()
        : optimizingConfig.getMinorLeastFileCount();
long effectiveMajor =
    options.majorFileCountThreshold()
        .orElseGet(() ->
            explicitlyConfigured(table.options(), "num-sorted-run.stop-trigger")
                ? (long) table.coreOptions().numSortedRunStopTrigger()
                : (long) effectiveMinor + 3L);
```

Packing:

```java
for (int i = 0; i < selected.size(); i += options.maxBucketsPerTask()) {
  List<PaimonBucketCompactionUnit> units =
      selected.subList(i, Math.min(i + options.maxBucketsPerTask(), selected.size()));
  PaimonPrimaryKeyCompactionInput input =
      new PaimonPrimaryKeyCompactionInput(
          paimonTable, new ArrayList<>(units), type, fullCompaction, targetSnapshotId, commitUser, processId);
  props.put(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
      PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
  tasks.add(new PaimonPrimaryKeyCompactionTask(tableId, "primary-key-buckets", input, props));
}
```

- [ ] **Step 5: Modify pending input eligibility**

In `PaimonTable.evaluatePendingInput()`:

```java
boolean appendBucketUnaware = isAppendBucketUnawareTable();
boolean primaryKeyHashOptimizing = isPrimaryKeyHashOptimizingTable();
PaimonPendingInput pendingInput =
    appendBucketUnaware ? collectPaimonPendingInput() : new PaimonPendingInput();
boolean optimizingNecessary =
    isSelfOptimizingEnabled(context) && (appendBucketUnaware || primaryKeyHashOptimizing);
```

Add helper:

```java
private boolean isPrimaryKeyHashOptimizingTable() {
  if (!(table instanceof FileStoreTable) || table instanceof AppendOnlyFileStoreTable) {
    return false;
  }
  FileStoreTable fileStoreTable = (FileStoreTable) table;
  BucketMode mode = fileStoreTable.bucketMode();
  if (mode != BucketMode.HASH_FIXED && mode != BucketMode.HASH_DYNAMIC) {
    return false;
  }
  return PaimonPrimaryKeyOptions.from(properties()).enabled();
}
```

- [ ] **Step 6: Run tests and verify they pass**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyOptimizingPlanner,TestPaimonTablePendingInputEligibility
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonPrimaryKeyOptimizingPlanner.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTable.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPrimaryKeyOptimizingPlanner.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTablePendingInputEligibility.java
git commit -m "feat: plan paimon primary key hash compaction"
```

### Task 4: Executor 与 Committer

**Files:**
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutor.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyTableCommit.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionExecutor.java`
- Create: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyTableCommit.java`

- [ ] **Step 1: Write failing executor integration test**

Create a primary-key table, write multiple commits, build a `PaimonPrimaryKeyCompactionInput` with two bucket units from `bucketEntries()`, execute, then assert output has commit message bytes:

```java
PaimonPrimaryKeyCompactionExecutor executor = new PaimonPrimaryKeyCompactionExecutor(input);
PaimonPrimaryKeyCompactionOutput output = executor.execute();
assertFalse(output.getCommitMessageBytesList().isEmpty());
assertEquals(input.getUnits().size(), output.getCompactedBucketCount());
```

Add separate tests:

```java
@Test void minorExecutorUsesFullCompactionFalse()
@Test void majorExecutorUsesFullCompactionTrue()
```

If direct spying on Paimon `BatchTableWrite` is too invasive, verify by executing against a real Paimon table and asserting commit messages exist for both types.

- [ ] **Step 2: Write failing committer test**

Use executor output, set it on task, call `PaimonPrimaryKeyTableCommit.commit()`, then assert latest snapshot exists and is generated after commit:

```java
long before = latestSnapshotId(table);
new PaimonPrimaryKeyTableCommit(paimonTable, fileStoreTable, Collections.singletonList(task)).commit();
long after = latestSnapshotId(table);
assertTrue(after > before);
```

- [ ] **Step 3: Run tests and verify they fail**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyCompactionExecutor,TestPaimonPrimaryKeyTableCommit
```

Expected: FAIL because executor skeleton throws unsupported and committer does not exist.

- [ ] **Step 4: Implement executor**

Executor algorithm:

```java
if (input == null || input.getTable() == null || input.getUnits() == null || input.getUnits().isEmpty()) {
  throw new IllegalStateException("Paimon primary-key compaction input is incomplete.");
}
Object raw = input.getTable().originalTable();
if (!(raw instanceof FileStoreTable) || raw instanceof AppendOnlyFileStoreTable) {
  throw new IllegalStateException("PaimonPrimaryKeyCompactionExecutor requires primary-key FileStoreTable.");
}
FileStoreTable table = (FileStoreTable) raw;
FileStoreTable compactTable = table.copy(Collections.singletonMap(CoreOptions.WRITE_ONLY.key(), "false"));
if (input.getOptimizingType() == OptimizingType.FULL
    && latestSnapshotId(table) != input.getTargetSnapshotId()) {
  return emptyNoOpOutput();
}
BatchWriteBuilder builder = compactTable.newBatchWriteBuilder();
try (BatchTableWrite write = builder.newWrite()) {
  for (PaimonBucketCompactionUnit unit : input.getUnits()) {
    BinaryRow partition = SerializationUtils.deserializeBinaryRow(unit.getPartitionBytes());
    write.compact(partition, unit.getBucket(), input.isFullCompaction());
  }
  List<CommitMessage> messages = write.prepareCommit();
  List<byte[]> bytes = CommitMessageSerializer.serializeAll(messages);
  OutputMetrics metrics = outputMetrics(input.getUnits(), messages);
  return new PaimonPrimaryKeyCompactionOutput(
      bytes,
      serializer.getVersion(),
      metrics.compactedBucketCount,
      metrics.compactedFileCount,
      metrics.compactedFileSize,
      metrics.compactedRecordCount,
      metrics.producedFileCount,
      metrics.producedFileSize);
}
```

Metrics:

```java
compactedBucketCount = input.getUnits().size();
compactedFileCount = sum(unit.getFileCount());
compactedFileSize = sum(unit.getFileSizeInBytes());
compactedRecordCount = sum(unit.getRecordCount());
producedFileCount = sum(((CommitMessageImpl) message).compactIncrement().compactAfter().size());
producedFileSize = sum fileSize() for every DataFileMeta in compactAfter().
```

Executor additional rules:

```text
write-only=true table must still compact by using table.copy({write-only=false});
FULL task must no-op if latest snapshot changed after planning;
empty commit message list from prepareCommit is valid output.
```

- [ ] **Step 5: Implement committer**

Committer algorithm:

```java
List<CommitMessage> messages = new ArrayList<>();
CommitIdentity identity = null;
for (PaimonPrimaryKeyCompactionTask task : successTasks) {
  identity = mergeAndValidateCommitIdentity(identity, task.getInput());
  PaimonPrimaryKeyCompactionOutput output = task.getOutput();
  if (output.getCommitMessageBytesList().isEmpty()) {
    continue;
  }
  messages.addAll(CommitMessageSerializer.deserializeAll(output.getCommitMessageBytesList()));
}
if (messages.isEmpty()) {
  return;
}
if (identity.optimizingType == OptimizingType.FULL
    && latestSnapshotId(table) != identity.targetSnapshotId) {
  return;
}
FileStoreTable commitTable = table.copy(Collections.singletonMap(CoreOptions.WRITE_ONLY.key(), "false"));
try (StreamTableCommit commit = commitTable.newCommit(identity.commitUser)) {
  commit.filterAndCommit(Collections.singletonMap(identity.commitIdentifier, messages));
}
```

Rules:

```java
if successTasks is null or empty: log info and return;
if output missing: throw OptimizingCommitException;
if output commit message list is empty: skip this task as legal no-op;
if every success task has empty commit messages: log info and return;
if commitUser / commitIdentifier / optimizingType / targetSnapshotId missing or inconsistent: throw OptimizingCommitException;
if FULL targetSnapshotId differs from latest snapshot before commit: log info and return no-op;
if replaying an already committed identifier: filterAndCommit returns 0 and no snapshot is created;
if Paimon commit fails: wrap in OptimizingCommitException;
```

- [ ] **Step 6: Run tests and verify they pass**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyCompactionExecutor,TestPaimonPrimaryKeyTableCommit
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutor.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyTableCommit.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionExecutor.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyTableCommit.java
git commit -m "feat: execute paimon primary key hash compaction"
```

### Task 5: ProcessFactory 路由与 Recovery 映射

**Files:**
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/process/PaimonProcessFactory.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-ams/src/main/java/org/apache/amoro/server/persistence/converter/TaskDescriptorRecoveryTypes.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/process/TestPaimonProcessFactory.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestPaimonOptimizingE2E.java`

- [ ] **Step 1: Write failing factory routing tests**

Add to `TestPaimonProcessFactory`:

```java
@Test
@DisplayName("createPlanner routes enabled primary-key HASH_FIXED table to primary-key planner")
void createPlannerRoutesPrimaryKeyHashFixed() throws Exception {
  PaimonTable table = primaryKeyTableWithOptions(PaimonPrimaryKeyOptions.ENABLED, "true");
  TableOptimizingPlanner planner = factory.createPlanner(runtime, table, 1.0, 64L * 1024 * 1024);
  assertTrue(planner instanceof PaimonPrimaryKeyOptimizingPlanner);
}
```

Add committer routing test:

```java
TableOptimizingCommitter committer =
    factory.createCommitter(paimonTable, snapshotId, -1L, Collections.singletonList(primaryKeyTask), emptyMap(), emptyMap());
assertTrue(committer instanceof PaimonPrimaryKeyTableCommit);
```

- [ ] **Step 2: Write failing recovery mapping test**

Add to `TestPaimonOptimizingE2E`:

```java
PaimonPrimaryKeyCompactionTask task = new PaimonPrimaryKeyCompactionTask();
task.ensureExecutorFactoryImpl(PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
PaimonPrimaryKeyCompactionInput input = new PaimonPrimaryKeyCompactionInput();
task.setOutput(SerializationUtil.simpleSerialize(new PaimonPrimaryKeyCompactionOutput()));
TaskDescriptorRecoveryTypes.validateRecoveredTask(task, input, TableFormat.PAIMON);
```

- [ ] **Step 3: Run tests and verify they fail**

Run:

```bash
./mvnw test -pl amoro-format-paimon,amoro-ams -Dtest=TestPaimonProcessFactory,TestPaimonOptimizingE2E
```

Expected: FAIL because routing and recovery mapping are missing.

- [ ] **Step 4: Implement factory route**

In `PaimonProcessFactory.createPlanner()`:

```java
if (PaimonPrimaryKeyOptimizingPlanner.supports(paimonTable)) {
  return new PaimonPrimaryKeyOptimizingPlanner(
      paimonTable,
      tableId,
      processId,
      availableCore,
      maxInputSizePerThread,
      optimizingConfig,
      lastMinor,
      lastMajor,
      lastFull);
}
return new PaimonOptimizingPlanner(
    paimonTable,
    tableId,
    processId,
    availableCore,
    maxInputSizePerThread,
    optimizingConfig,
    lastMinor,
    lastMajor,
    lastFull,
    null);
```

In `createCommitter()`:

```java
if (allSuccessTasksArePrimaryKey(successTasks)) {
  FileStoreTable fileStoreTable = (FileStoreTable) paimonTable.originalTable();
  return new PaimonPrimaryKeyTableCommit(paimonTable, fileStoreTable, primaryKeyTasks);
}
// existing APPEND path unchanged
```

Fail fast if mixed APPEND and primary-key task descriptors appear in the same successTasks collection.

- [ ] **Step 5: Register recovery mapping**

In `TaskDescriptorRecoveryTypes` add imports and mapping:

```java
mappings.put(
    PaimonPrimaryKeyCompactionExecutorFactory.class.getName(),
    new RecoveryTypes(
        PaimonPrimaryKeyCompactionTask.class,
        PaimonPrimaryKeyCompactionInput.class,
        PaimonPrimaryKeyCompactionOutput.class,
        PaimonMetricsSummary.class));
```

- [ ] **Step 6: Run tests and verify they pass**

Run:

```bash
./mvnw test -pl amoro-format-paimon,amoro-ams -Dtest=TestPaimonProcessFactory,TestPaimonOptimizingE2E
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/process/PaimonProcessFactory.java \
  amoro-ams/src/main/java/org/apache/amoro/server/persistence/converter/TaskDescriptorRecoveryTypes.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/process/TestPaimonProcessFactory.java \
  amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestPaimonOptimizingE2E.java
git commit -m "feat: route paimon primary key optimizing process"
```

### Task 6: Descriptor MAJOR 展示与 APPEND 回归

**Files:**
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTableDescriptor.java`
- Modify: `/Users/SL/.codex/worktrees/e09a/amoro/amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTableDescriptorOptimizingProcesses.java`
- Run existing APPEND tests.

- [ ] **Step 1: Write failing descriptor test**

Add:

```java
@Test
void tableOptimizingTypesExposeMajor() {
  Map<String, String> types = descriptor.getTableOptimizingTypes(amoroTable);
  assertEquals("FULL", types.get("FULL"));
  assertEquals("MAJOR", types.get("MAJOR"));
  assertEquals("MINOR", types.get("MINOR"));
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonTableDescriptorOptimizingProcesses
```

Expected: FAIL because `MAJOR` is not exposed.

- [ ] **Step 3: Implement descriptor change**

In `PaimonTableDescriptor`:

```java
private static final String PAIMON_OPTIMIZING_TYPE_MAJOR = "MAJOR";
```

In `getTableOptimizingTypes`:

```java
types.put("FULL", "FULL");
types.put("MAJOR", "MAJOR");
types.put("MINOR", "MINOR");
```

Where process type is inferred, preserve existing FULL/MINOR inference. Do not invent snapshot-level MAJOR inference unless runtime/history already provides it.

- [ ] **Step 4: Run descriptor and APPEND regression tests**

Run:

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonTableDescriptorOptimizingProcesses,TestPaimonOptimizingPlanner,TestPaimonCompactionExecutor,TestPaimonTableCommit,TestPaimonTablePendingInputEligibility
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTableDescriptor.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTableDescriptorOptimizingProcesses.java
git commit -m "feat: expose paimon major optimizing type"
```

### Task 7: Spec 对齐 Review、格式化与最终验证

**Files:**
- Review all files modified by Tasks 1-6.

- [ ] **Step 1: Run code review checklist**

Check each Spec invariant:

```text
[ ] HASH_FIXED supported
[ ] HASH_DYNAMIC supported
[ ] HASH_FIXED/HASH_DYNAMIC share primary-key implementation
[ ] APPEND planner/executor/committer unchanged semantically
[ ] primary-key.enabled defaults false
[ ] max-buckets-per-task defaults 16
[ ] non-empty self-optimizing.filter is rejected with LOG.warn
[ ] MINOR threshold uses explicit Paimon trigger before Amoro minor file count
[ ] MAJOR threshold uses override > explicit stop-trigger > effective minor + 3
[ ] MAJOR > MINOR > FULL
[ ] FULL requires partition-idle-time
[ ] FULL no-ops in executor or committer if latest snapshot changed after planning
[ ] no partial commit
[ ] empty Paimon commit messages are legal no-op
[ ] write-only=true primary-key table compacts via write-only=false copy
[ ] committed replay is filtered by process commit identity
[ ] real Paimon commit conflict bubbles to Amoro failure
[ ] recovery mapping registered
[ ] descriptor exposes MAJOR
```

- [ ] **Step 2: Run formatting**

Run:

```bash
dev/reformat
```

Expected: Java formatting completes without manual conflicts.

- [ ] **Step 3: Run focused tests**

Run:

```bash
./mvnw test -pl amoro-format-paimon,amoro-ams -Dtest=TestPaimonPrimaryKeyOptions,TestPaimonPrimaryKeyCompactionTask,TestPaimonPrimaryKeyCompactionOutput,TestPaimonPrimaryKeyOptimizingPlanner,TestPaimonPrimaryKeyCompactionExecutor,TestPaimonPrimaryKeyTableCommit,TestPaimonProcessFactory,TestPaimonOptimizingE2E,TestPaimonTableDescriptorOptimizingProcesses,TestPaimonTablePendingInputEligibility,TestPaimonOptimizingPlanner,TestPaimonCompactionExecutor,TestPaimonTableCommit
```

Expected: PASS.

- [ ] **Step 4: Run module validation**

Run:

```bash
./mvnw test -pl amoro-format-paimon -am -DskipITs
```

Expected: PASS. If runtime is too long, run `./mvnw test -pl amoro-format-paimon -DskipITs` and record the coverage limitation in the final response.

- [ ] **Step 5: Commit final review fixes**

Only if formatting or review changed files:

```bash
git add <changed-files>
git commit -m "test: verify paimon primary key optimizing"
```

---

## Plan Self-Review

### Spec coverage

- `HASH_FIXED/HASH_DYNAMIC` 支持：Task 3 planner tests and implementation。
- APPEND 隔离：Task 3 pending input update is additive; Task 5 routing preserves APPEND path; Task 6 APPEND regression tests。
- `MINOR/MAJOR/FULL` 语义：Task 3 planner thresholds and priority tests。
- `partition-idle-time`：Task 3 FULL tests and planner filtering。
- `self-optimizing.filter` 拒绝：Task 3 tests and planner guard。
- task batch buckets：Task 3 packing tests; Task 4 executor loops units。
- no partial commit：Task 4 committer design and test。
- commit conflict strategy：Task 4 filters committed replay by process commit identity; real Paimon commit failure still fails current process。
- recovery mapping：Task 5。
- descriptor MAJOR：Task 6。

### Placeholder scan

Plan intentionally contains no placeholder markers or unspecified future work. Where implementation depends on Paimon public API availability, the plan gives the required fallback rule: compile against the repository dependency and preserve Spec semantics.

### Type consistency

The task model consistently uses:

- `PaimonPrimaryKeyCompactionInput`
- `PaimonPrimaryKeyCompactionOutput`
- `PaimonPrimaryKeyCompactionTask`
- `PaimonPrimaryKeyCompactionExecutor`
- `PaimonPrimaryKeyCompactionExecutorFactory`
- `PaimonPrimaryKeyTableCommit`
- `PaimonBucketCompactionUnit`
- `PaimonPrimaryKeyOptions`

All primary-key classes live under `org.apache.amoro.formats.paimon.optimizing.primary` except the planner, which lives under existing planner package `org.apache.amoro.formats.paimon.optimizing.plan`.
