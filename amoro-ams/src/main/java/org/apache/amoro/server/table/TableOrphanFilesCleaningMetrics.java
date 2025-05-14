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

package org.apache.amoro.server.table;

import static org.apache.amoro.metrics.MetricDefine.defineCounter;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.server.metrics.MetricRegistry;

/** Table Orphan Files Cleaning metrics. */
public class TableOrphanFilesCleaningMetrics extends AbstractTableMetrics {
  private final Counter orphanDataFilesCount = new Counter();
  private final Counter expectedOrphanDataFilesCount = new Counter();

  private final Counter orphanMetadataFilesCount = new Counter();
  private final Counter expectedOrphanMetadataFilesCount = new Counter();

  public TableOrphanFilesCleaningMetrics(ServerTableIdentifier identifier) {
    super(identifier);
  }

  public static final MetricDefine TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_content_file_cleaning_count")
          .withDescription("Count of orphan content files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_ORPHAN_METADATA_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_metadata_file_cleaning_count")
          .withDescription("Count of orphan metadata files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_content_file_cleaning_count")
          .withDescription(
              "Expected count of orphan content files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_EXPECTED_ORPHAN_METADATA_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_metadata_file_cleaning_count")
          .withDescription(
              "Expected count of orphan metadata files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  @Override
  public void registerMetrics(MetricRegistry registry) {
    if (globalRegistry == null) {
      registerMetric(registry, TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT, orphanDataFilesCount);
      registerMetric(registry, TABLE_ORPHAN_METADATA_FILE_CLEANING_COUNT, orphanMetadataFilesCount);
      registerMetric(
          registry,
          TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT,
          expectedOrphanDataFilesCount);
      registerMetric(
          registry,
          TABLE_EXPECTED_ORPHAN_METADATA_FILE_CLEANING_COUNT,
          expectedOrphanMetadataFilesCount);
      globalRegistry = registry;
    }
  }

  public void completeOrphanDataFiles(int expected, int cleaned) {
    expectedOrphanMetadataFilesCount.inc(expected);
    orphanDataFilesCount.inc(cleaned);
  }

  public void completeOrphanMetadataFiles(int expected, int cleaned) {
    expectedOrphanMetadataFilesCount.inc(expected);
    orphanMetadataFilesCount.inc(cleaned);
  }
}
