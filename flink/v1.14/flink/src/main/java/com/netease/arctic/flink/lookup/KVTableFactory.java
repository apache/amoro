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

package com.netease.arctic.flink.lookup;

import com.netease.arctic.flink.lookup.filter.RowDataPredicate;
import org.apache.flink.configuration.Configuration;
import org.apache.iceberg.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.netease.arctic.flink.util.LookupUtil.convertLookupOptions;

public class KVTableFactory implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(KVTableFactory.class);
  private static final long serialVersionUID = 8090117643055858494L;
  public static final KVTableFactory INSTANCE = new KVTableFactory();

  public KVTable create(
      StateFactory stateFactory,
      List<String> primaryKeys,
      List<String> joinKeys,
      Schema projectSchema,
      Configuration config,
      RowDataPredicate rowDataPredicate) {
    Set<String> joinKeySet = new HashSet<>(joinKeys);
    Set<String> primaryKeySet = new HashSet<>(primaryKeys);
    if (joinKeySet.size() > primaryKeySet.size() && joinKeySet.containsAll(primaryKeySet)) {
      LOG.info("create unique index table, join keys contain all primary keys, unique keys are {}, join keys are {}.",
          primaryKeys.toArray(), joinKeys.toArray());
      return
          new UniqueIndexTable(stateFactory, joinKeys, projectSchema,
              convertLookupOptions(config),
              rowDataPredicate);
    }
    if (new HashSet<>(primaryKeys).equals(new HashSet<>(joinKeys))) {
      LOG.info("create unique index table, unique keys are {}, join keys are {}.",
          primaryKeys.toArray(), joinKeys.toArray());
      return
          new UniqueIndexTable(stateFactory, primaryKeys, projectSchema,
              convertLookupOptions(config), rowDataPredicate);
    } else {
      LOG.info("create secondary index table, unique keys are {}, join keys are {}.",
          primaryKeys.toArray(), joinKeys.toArray());
      return
          new SecondaryIndexTable(stateFactory, primaryKeys, joinKeys, projectSchema,
              convertLookupOptions(config),
              rowDataPredicate);
    }
  }
}
