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

package org.apache.amoro.flink.lookup;

import static org.apache.amoro.flink.util.LookupUtil.convertLookupOptions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KVTableFactory implements TableFactory<RowData>, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(KVTableFactory.class);
  private static final long serialVersionUID = 8090117643055858494L;
  public static final KVTableFactory INSTANCE = new KVTableFactory();

  public KVTable<RowData> create(
      RowDataStateFactory rowDataStateFactory,
      List<String> primaryKeys,
      List<String> joinKeys,
      Schema projectSchema,
      Configuration config,
      Predicate<RowData> rowDataPredicate) {
    Set<String> joinKeySet = new HashSet<>(joinKeys);
    Set<String> primaryKeySet = new HashSet<>(primaryKeys);
    // keep the primary keys order with projected schema fields.
    primaryKeys =
        projectSchema.asStruct().fields().stream()
            .map(Types.NestedField::name)
            .filter(primaryKeySet::contains)
            .collect(Collectors.toList());

    if (primaryKeySet.equals(joinKeySet)) {
      LOG.info(
          "create unique index table, unique keys are {}, lookup keys are {}.",
          primaryKeys.toArray(),
          joinKeys.toArray());
      return new UniqueIndexTable(
          rowDataStateFactory,
          primaryKeys,
          projectSchema,
          convertLookupOptions(config),
          rowDataPredicate);
    } else {
      LOG.info(
          "create secondary index table, unique keys are {}, lookup keys are {}.",
          primaryKeys.toArray(),
          joinKeys.toArray());
      return new SecondaryIndexTable(
          rowDataStateFactory,
          primaryKeys,
          joinKeys,
          projectSchema,
          convertLookupOptions(config),
          rowDataPredicate);
    }
  }
}
