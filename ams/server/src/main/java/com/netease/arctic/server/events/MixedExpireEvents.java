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

package com.netease.arctic.server.events;

import com.netease.arctic.ams.api.events.EventType;
import com.netease.arctic.ams.api.events.ExpireEvent;
import com.netease.arctic.ams.api.events.ExpireOperation;
import com.netease.arctic.ams.api.events.ExpireResult;
import com.netease.arctic.ams.api.events.ImmutableExpireEvent;
import com.netease.arctic.ams.api.events.iceberg.ExpireSnapshotsResult;
import com.netease.arctic.ams.api.events.mixed.ExpireMixedSnapshotsResult;
import com.netease.arctic.ams.api.events.mixed.ImmutableExpireMixedSnapshotsResult;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.FileNameRules;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

public class MixedExpireEvents {

  public static ExpireEvent combine(ExpireEvent changeEvent, ExpireEvent baseEvent) {
    if (changeEvent == null) {
      return baseEvent;
    }

    if (!changeEvent.operation().equals(baseEvent.operation())) {
      throw new UnsupportedOperationException(
          "Cannot combine expire events with different operation");
    }

    return ImmutableExpireEvent.builder()
        .catalog(baseEvent.catalog())
        .database(baseEvent.database())
        .table(baseEvent.table())
        .processId(Math.min(changeEvent.processId(), baseEvent.processId()))
        .timestampMillis(Math.min(changeEvent.timestampMillis(), baseEvent.timestampMillis()))
        .format(baseEvent.format())
        .operation(baseEvent.operation())
        .expireResult(
            combineExpireResult(
                baseEvent.operation(), changeEvent.expireResult(), baseEvent.expireResult()))
        .type(EventType.EXPIRE_REPORT)
        .build();
  }

  public static ExpireEvent wrap(ExpireEvent nativeEvent) {
    if (nativeEvent == null) {
      return null;
    }

    ExpireResult mixedResult =
        combineExpireResult(nativeEvent.operation(), null, nativeEvent.expireResult());

    return ImmutableExpireEvent.builder()
        .catalog(nativeEvent.catalog())
        .database(nativeEvent.database())
        .table(nativeEvent.table())
        .processId(nativeEvent.processId())
        .timestampMillis(nativeEvent.timestampMillis())
        .format(nativeEvent.format())
        .operation(nativeEvent.operation())
        .expireResult(mixedResult)
        .type(EventType.EXPIRE_REPORT)
        .build();
  }

  private static ExpireResult combineExpireResult(
      ExpireOperation expectOperation, ExpireResult changeResult, ExpireResult baseResult) {
    if (expectOperation == ExpireOperation.EXPIRE_SNAPSHOTS) {
      return combineExpireSnapshots(
          (ExpireSnapshotsResult) changeResult, (ExpireSnapshotsResult) baseResult);
    } else {
      // TODO: combine other operations
      throw new UnsupportedOperationException(
          "Operation of expire result is not as expect: " + expectOperation);
    }
  }

  private static ExpireMixedSnapshotsResult combineExpireSnapshots(
      ExpireSnapshotsResult changeResult, ExpireSnapshotsResult baseResult) {

    ImmutableExpireMixedSnapshotsResult.Builder builder =
        ImmutableExpireMixedSnapshotsResult.builder();

    if (baseResult == null) {
      return builder.build();
    }

    Sets.union(
            Sets.union(baseResult.deletedDataFiles(), baseResult.deletedPositionDeleteFiles()),
            baseResult.deletedEqualityDeleteFiles())
        .forEach(
            file -> {
              DataFileType dataFileType = FileNameRules.parseFileTypeForChange(file);
              if (dataFileType == DataFileType.BASE_FILE) {
                builder.addDeletedBaseFiles(file);
              } else if (dataFileType == DataFileType.POS_DELETE_FILE) {
                builder.addDeletedPositionDeleteFiles(file);
              } else if (dataFileType == DataFileType.ICEBERG_EQ_DELETE_FILE) {
                builder.addDeletedIcebergEqualityDeleteFiles(file);
              }
            });

    if (changeResult == null) {
      builder
          .totalDuration(baseResult.totalDuration())
          .deletedMetadataFiles(baseResult.deletedMetadataFiles())
          .deletedStatisticsFiles(baseResult.deletedStatisticsFiles());
    } else {
      Sets.union(
              Sets.union(
                  changeResult.deletedDataFiles(), changeResult.deletedPositionDeleteFiles()),
              changeResult.deletedEqualityDeleteFiles())
          .forEach(
              file -> {
                DataFileType dataFileType = FileNameRules.parseFileTypeForChange(file);
                if (dataFileType == DataFileType.INSERT_FILE) {
                  builder.addDeletedInsertFiles(file);
                } else if (dataFileType == DataFileType.EQ_DELETE_FILE) {
                  builder.addDeletedIcebergEqualityDeleteFiles(file);
                } else if (dataFileType == DataFileType.ICEBERG_EQ_DELETE_FILE) {
                  builder.addDeletedIcebergEqualityDeleteFiles(file);
                }
              });
      builder
          .totalDuration(changeResult.totalDuration().plus(baseResult.totalDuration()))
          .addAllDeletedMetadataFiles(changeResult.deletedMetadataFiles())
          .addAllDeletedMetadataFiles(baseResult.deletedMetadataFiles())
          .addAllDeletedStatisticsFiles(changeResult.deletedStatisticsFiles())
          .addAllDeletedStatisticsFiles(baseResult.deletedStatisticsFiles());
    }

    return builder.build();
  }
}
