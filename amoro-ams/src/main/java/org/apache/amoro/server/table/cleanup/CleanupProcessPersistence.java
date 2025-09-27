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

package org.apache.amoro.server.table.cleanup;

import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableCleanupMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CleanupProcessPersistence extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(CleanupProcessPersistence.class);

  /**
   * Upsert lastCleanupEndTime field by table_id and cleanup_operation If record exists, update it,
   * otherwise insert a new record
   */
  public void upsertLastCleanupEndTimeByTableIdAndCleanupOperation(
      TableCleanupProcessMeta cleanupProcessMeta) {
    doAsTransaction(
        () ->
            doAs(
                TableCleanupMapper.class,
                mapper -> {
                  // First try to update
                  int updatedRows =
                      mapper.updateLastCleanupEndTimeByTableIdAndCleanupOperation(
                          cleanupProcessMeta.getTableId(),
                          cleanupProcessMeta.getCleanupOperation(),
                          cleanupProcessMeta.getLastCleanupEndTime());

                  // If no rows were updated, insert a new record
                  if (updatedRows == 0) {
                    mapper.insertTableCleanupProcess(cleanupProcessMeta);
                  }
                }));
  }

  /**
   * Check if cleanup process records exist for multiple table ids and one cleanupOperation, return
   * the list of tableId and lastCleanupEndTime that have record
   *
   * @param tableIds List of table IDs to check
   * @param cleanupOperation Operation to filter by
   */
  public Map<Long, Long> selectTableCleanupProcess(
      List<Long> tableIds, CleanupOperation cleanupOperation) {

    List<TableCleanupProcessMeta> metas =
        getAs(
            TableCleanupMapper.class,
            mapper -> mapper.selectTableIdAndLastCleanupEndTime(tableIds, cleanupOperation));
    if (metas.isEmpty()) {
      LOG.debug(
          "No cleanup process records found for {} tables with cleanupOperation: {}",
          tableIds.size(),
          cleanupOperation);
      return Maps.newHashMap();
    } else {
      LOG.debug(
          "Found cleanup process records for {} out of {} tables with cleanupOperation: {}",
          metas.size(),
          tableIds.size(),
          cleanupOperation);
      return metas.stream()
          .collect(
              Collectors.toMap(
                  TableCleanupProcessMeta::getTableId,
                  TableCleanupProcessMeta::getLastCleanupEndTime,
                  (existing, replacement) -> replacement));
    }
  }

  public void deleteTableCleanupProcesses(Long tableId) {
    doAsTransaction(
        () ->
            doAs(
                TableCleanupMapper.class,
                mapper -> {
                  mapper.deleteTableCleanupProcesses(tableId);
                }));
  }
}
