/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.maintainer.command;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;
import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_DB_NAME;

public class TestShowCall extends CallCommandTestBase {

  @Test
  public void testShowDatabases() throws TException {
    Context context = new Context();
    Assert.assertThrows(
        RuntimeException.class,
        () -> callFactory.generateShowCall(ShowCall.Namespaces.DATABASES).call(context));

    context.setCatalog(TEST_CATALOG_NAME);
    Assert.assertEquals(
        PK_TABLE_ID.getDatabase(),
        callFactory.generateShowCall(ShowCall.Namespaces.DATABASES).call(context));

    AMS.handler().createDatabase(TEST_CATALOG_NAME, "repair_db");
    Assert.assertEquals(
        PK_TABLE_ID.getDatabase() + "\nrepair_db",
        callFactory.generateShowCall(ShowCall.Namespaces.DATABASES).call(context));
  }

  @Test
  public void testShowTables() {
    Context context = new Context();
    Assert.assertThrows(
        RuntimeException.class,
        () -> callFactory.generateShowCall(ShowCall.Namespaces.TABLES).call(context));

    context.setCatalog(TEST_CATALOG_NAME);
    Assert.assertThrows(
        RuntimeException.class,
        () -> callFactory.generateShowCall(ShowCall.Namespaces.TABLES).call(context));

    context.setDb(TEST_DB_NAME);
    Assert.assertEquals(
        "test_table\n" +
            "test_pk_table\n" +
            "test_no_partition_table",
        callFactory.generateShowCall(ShowCall.Namespaces.TABLES).call(context));
  }
}
