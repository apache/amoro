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

package com.netease.arctic.flink;

import com.netease.arctic.flink.catalog.TestCatalog;
import com.netease.arctic.flink.read.ArcticSourceTest;
import com.netease.arctic.flink.read.FlinkSourceTest;
import com.netease.arctic.flink.read.FlinkSplitPlannerTest;
import com.netease.arctic.flink.read.hybrid.assigner.ShuffleSplitAssignerTest;
import com.netease.arctic.flink.read.hybrid.enumerator.ArcticSourceEnumStateSerializerTest;
import com.netease.arctic.flink.read.hybrid.reader.RowDataReaderFunctionTest;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplitSerializerTest;
import com.netease.arctic.flink.shuffle.RoundRobinShuffleRulePolicyTest;
import com.netease.arctic.flink.table.TestKeyed;
import com.netease.arctic.flink.table.TestUnkeyed;
import com.netease.arctic.flink.table.TestUnkeyedOverwrite;
import com.netease.arctic.flink.write.AdaptHiveWriterTest;
import com.netease.arctic.flink.write.ArcticFileCommitterTest;
import com.netease.arctic.flink.write.ArcticFileWriterTest;
import com.netease.arctic.flink.write.FlinkSinkTest;
import com.netease.arctic.hive.HiveTableTestBase;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.iceberg.flink.MiniClusterResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestCatalog.class,
//    ShuffleSplitAssignerTest.class,
//    ArcticSourceEnumStateSerializerTest.class,
//    RowDataReaderFunctionTest.class,
//    ArcticSplitSerializerTest.class,
//    ArcticSourceTest.class,
    FlinkSourceTest.class,
//    FlinkSplitPlannerTest.class,
    RoundRobinShuffleRulePolicyTest.class,
    TestKeyed.class,
    TestUnkeyed.class,
    TestUnkeyedOverwrite.class,
    AdaptHiveWriterTest.class,
    ArcticFileCommitterTest.class,
    ArcticFileWriterTest.class,
    FlinkSinkTest.class
})
public class FlinkTestMain {

  @ClassRule
  public static final MiniClusterWithClientResource MINI_CLUSTER_RESOURCE =
      MiniClusterResource.createWithClassloaderCheckDisabled();

  @BeforeClass
  public static void setup() throws Exception {
    System.out.println("================== begin flink test ==================");
    HiveTableTestBase.startMetastore();
  }

  @AfterClass
  public static void cleanDown() {
    System.out.println("================== end flink test ===================");
    HiveTableTestBase.stopMetastore();
  }
}
