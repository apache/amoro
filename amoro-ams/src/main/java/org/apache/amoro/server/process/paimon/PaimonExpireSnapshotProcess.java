/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.process.paimon;

import org.apache.amoro.Action;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.HttpRemoteSparkStandAloneSubmit;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Remote table process that submits Paimon expire_snapshots procedure to Spark via HTTP. The SQL is
 * assembled from table properties and submitted through {@link HttpRemoteSparkStandAloneSubmit}.
 */
public class PaimonExpireSnapshotProcess extends TableProcess {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonExpireSnapshotProcess.class);

  private static final String DEFAULT_SNAPSHOT_TIME_RETAINED = "1 h";
  private static final int DEFAULT_SNAPSHOT_NUM_RETAINED_MAX = 10;

  private static final String PROP_SNAPSHOT_TIME_RETAINED = "snapshot.time-retained";
  private static final String PROP_SNAPSHOT_NUM_RETAINED_MAX = "snapshot.num-retained.max";

  private final int sparkVersion;

  public PaimonExpireSnapshotProcess(
      TableRuntime tableRuntime, ExecuteEngine engine, int sparkVersion) {
    super(tableRuntime, engine);
    this.sparkVersion = sparkVersion;
  }

  public static Optional<PaimonExpireSnapshotProcess> trigger(
      TableRuntime tableRuntime, ExecuteEngine engine, int sparkVersion, Duration interval) {
    if (tableRuntime instanceof DefaultTableRuntime) {
      DefaultTableRuntime prt = (DefaultTableRuntime) tableRuntime;
      long lastExecuteTime = prt.getLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING);
      if (System.currentTimeMillis() - lastExecuteTime < interval.toMillis()) {
        LOG.debug(
            "Skip expire snapshots for table {}, last execute time: {}",
            tableRuntime.getTableIdentifier(),
            lastExecuteTime);
        return Optional.empty();
      }
    }
    return Optional.of(new PaimonExpireSnapshotProcess(tableRuntime, engine, sparkVersion));
  }

  @Override
  public Action getAction() {
    return PaimonActions.EXPIRE_SNAPSHOTS;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    Map<String, String> params = new HashMap<>();
    params.put("hql", buildExpireSnapshotsSql());
    params.put("curUser", "sljdp");
    params.put("logUser", "sljdp");
    params.put("group", "sljdp");
    params.put("userId", "470");
    params.put("sparkVersion", String.valueOf(sparkVersion));
    params.put("sourceTag", "AMORO");
    params.put("conf", "{\"sparkVersion\":\"" + sparkVersion + "\"}");
    return params;
  }

  @Override
  public Map<String, String> getSummary() {
    Map<String, String> summary = new HashMap<>();
    summary.put("table", getTableIdentifier().toString());
    summary.put("action", getAction().getName());
    return summary;
  }

  @Override
  public void afterComplete(ProcessStatus status) {
    if (status == ProcessStatus.SUCCESS && tableRuntime instanceof DefaultTableRuntime) {
      ((DefaultTableRuntime) tableRuntime)
          .updateLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING, System.currentTimeMillis());
      LOG.info(
          "Updated lastSnapshotsExpiringTime for table {} after successful expire snapshots",
          getTableIdentifier());
    }
  }

  String buildExpireSnapshotsSql() {
    Map<String, String> tableConfig = tableRuntime.getTableConfig();
    ServerTableIdentifier tableId = getTableIdentifier();

    Duration timeRetained = parseTimeRetained(tableConfig);
    int retainMax = parseRetainMax(tableConfig);

    long olderThanTimestampMillis = System.currentTimeMillis() - timeRetained.toMillis();

    String fullTableName = String.format("%s.%s", tableId.getDatabase(), tableId.getTableName());

    String sql =
        String.format(
            "CALL sys.expire_snapshots(table => '%s', retain_max => %d, older_than => %s)",
            fullTableName, retainMax, String.valueOf(olderThanTimestampMillis));

    LOG.info("Built expire snapshots SQL for table {}: {}", tableId, sql);
    return sql;
  }

  private Duration parseTimeRetained(Map<String, String> tableConfig) {
    String value = tableConfig.get(PROP_SNAPSHOT_TIME_RETAINED);
    if (value == null || value.isEmpty()) {
      return parseDuration(DEFAULT_SNAPSHOT_TIME_RETAINED);
    }
    try {
      return parseDuration(value);
    } catch (Exception e) {
      LOG.warn(
          "Failed to parse {} value '{}', using default {}",
          PROP_SNAPSHOT_TIME_RETAINED,
          value,
          DEFAULT_SNAPSHOT_TIME_RETAINED);
      return parseDuration(DEFAULT_SNAPSHOT_TIME_RETAINED);
    }
  }

  private int parseRetainMax(Map<String, String> tableConfig) {
    String value = tableConfig.get(PROP_SNAPSHOT_NUM_RETAINED_MAX);
    if (value == null || value.isEmpty()) {
      return DEFAULT_SNAPSHOT_NUM_RETAINED_MAX;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOG.warn(
          "Failed to parse {} value '{}', using default {}",
          PROP_SNAPSHOT_NUM_RETAINED_MAX,
          value,
          DEFAULT_SNAPSHOT_NUM_RETAINED_MAX);
      return DEFAULT_SNAPSHOT_NUM_RETAINED_MAX;
    }
  }

  /**
   * Parse a Paimon-style duration string (e.g. "1 h", "2d", "30 min") to {@link Duration}. Uses the
   * same format as Paimon's TimeUtils.
   */
  static Duration parseDuration(String text) {
    text = text.trim();
    // Try standard Java Duration first (e.g. PT1H)
    try {
      return Duration.parse(text);
    } catch (Exception ignored) {
      // Continue with Paimon-style parsing
    }

    // Paimon-style: number + unit (d, h, min, ms, s, us, ns)
    int i = 0;
    while (i < text.length() && Character.isDigit(text.charAt(i))) {
      i++;
    }
    String numberPart = text.substring(0, i).trim();
    String unitPart = text.substring(i).trim().toLowerCase();

    long number = Long.parseLong(numberPart);
    switch (unitPart) {
      case "d":
        return Duration.ofDays(number);
      case "h":
        return Duration.ofHours(number);
      case "min":
        return Duration.ofMinutes(number);
      case "s":
      case "":
        return Duration.ofSeconds(number);
      case "ms":
        return Duration.ofMillis(number);
      default:
        throw new IllegalArgumentException("Unknown duration unit: " + unitPart);
    }
  }
}
