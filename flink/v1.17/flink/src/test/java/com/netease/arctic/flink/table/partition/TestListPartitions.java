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

package com.netease.arctic.flink.table.partition;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.flink.InternalCatalogBuilder;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.partition.PartitionUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestListPartitions {

  public static final Logger LOG = LoggerFactory.getLogger(TestListPartitions.class);

  @Test
  public void listPartitions() {

    ArcticCatalog catalog = new InternalCatalogBuilder()
        .metastoreUrl("thrift://k8s01:1260/arctic_catalog")
        .build();

    ArcticTable arcticTable = catalog.loadTable(TableIdentifier.of("arctic_catalog", "ods", "pt_tb_fun"));

    PartitionUtil.getTablePartition(arcticTable).forEach(partitionBaseInfo -> {
      LOG.info("{} , {} ", partitionBaseInfo.getPartition(), partitionBaseInfo.getFileCount());
    });
  }
}
