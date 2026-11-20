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

package org.apache.amoro.formats.paimon.process;

import org.apache.amoro.Action;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.paimon.options.ExpireConfig;
import org.apache.paimon.table.FileStoreTable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One native snapshot expiration invocation using the current table's retention settings. */
public class PaimonSnapshotsExpiringProcess extends TableProcess implements LocalProcess {
  public static final String TAG = "paimon-expire-snapshots";
  private volatile Map<String, String> summary = Collections.emptyMap();

  public PaimonSnapshotsExpiringProcess(TableRuntime runtime, ExecuteEngine engine) {
    super(runtime, engine);
  }

  @Override
  public String tag() {
    return TAG;
  }

  @Override
  public void run() {
    Map<String, String> result = new LinkedHashMap<>();
    publish(result);
    String stage = "load";
    try {
      PaimonTable table = PaimonSnapshotExpiration.refresh(tableRuntime);
      stage = "config";
      String reason = PaimonSnapshotExpiration.skipReason(table);
      if (reason != null) {
        result.put("outcome", "skipped");
        result.put("skip-reason", reason);
        publish(result);
        return;
      }
      FileStoreTable nativeTable = (FileStoreTable) table.originalTable();
      ExpireConfig config = table.doAs(() -> PaimonSnapshotExpiration.configuration(nativeTable));
      result.putAll(PaimonSnapshotExpiration.summary(config));
      publish(result);
      stage = "expire";
      int expired =
          table.doAs(
              () ->
                  PaimonSnapshotExpiration.newExpireSnapshots(nativeTable).config(config).expire());
      result.put("outcome", expired == 0 ? "noop" : "executed");
      result.put("reported-expired-snapshots", String.valueOf(expired));
      result.put("result-scope", "last-completed-invocation");
      publish(result);
      stage = "state-update";
      tableRuntime.updateState(
          PaimonProcessFactory.LAST_SUCCESS_TIME, ignored -> System.currentTimeMillis());
    } catch (Exception e) {
      result.put("error-stage", stage);
      // Exception messages can contain catalog credentials or signed object-store URLs.
      result.put(
          "error-message",
          "Snapshot expiration failed during "
              + stage
              + " ("
              + e.getClass().getSimpleName()
              + "). See AMS logs for details.");
      publish(result);
      LOG.error(
          "Paimon snapshot expiration failed during {} for {}", stage, getTableIdentifier(), e);
      throw new RuntimeException("Paimon snapshot expiration failed during " + stage, e);
    }
  }

  private void publish(Map<String, String> result) {
    summary = Collections.unmodifiableMap(new LinkedHashMap<>(result));
  }

  @Override
  public Action getAction() {
    return PaimonActions.EXPIRE_SNAPSHOTS;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, String> getSummary() {
    return summary;
  }
}
