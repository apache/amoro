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

package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.ams.server.repair.TestRepairEnv;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

public class TestShowCallGenerator extends TestRepairEnv {
  @Test
  public void test() throws TException, CallCommand.FullTableNameException {
    ShowCallGenerator generator = new ShowCallGenerator(TEST_AMS.getServerUrl());
    Context context = new Context();
    Assert.assertEquals(TEST_CATALOG_NAME, generator.generate(ShowCall.Namespaces.CATALOGS).call(context));
    context.setCatalog(TEST_CATALOG_NAME);
    Assert.assertEquals(TEST_DATABASE_NAME, generator.generate(ShowCall.Namespaces.DATABASES).call(context));
    context.setDb(TEST_DATABASE_NAME);
    Assert.assertEquals(
        catalog.listTables(TEST_DATABASE_NAME)
            .stream()
            .map(e -> String.format("%s %s", e.getDatabase(), e.getTableName()))
            .collect(Collectors.joining("\\n")),
        generator.generate(ShowCall.Namespaces.TABLES).call(context));
  }
}
