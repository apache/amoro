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

public class TestUseCallGenerator extends TestRepairEnv {

  @Test
  public void test() throws TException, CallCommand.FullTableNameException {
    UseCallGenerator generator = new UseCallGenerator(TEST_AMS.getServerUrl());
    Context context = new Context();
    generator.generate(TEST_CATALOG_NAME).call(context);
    Assert.assertEquals(TEST_CATALOG_NAME, context.getCatalog());
    generator.generate(TEST_DATABASE_NAME).call(context);
    Assert.assertEquals(TEST_DATABASE_NAME, context.getDb());

    Context context1 = new Context();
    generator.generate(String.format("%s.%s", TEST_CATALOG_NAME, TEST_DATABASE_NAME)).call(context1);
    Assert.assertEquals(TEST_CATALOG_NAME, context1.getCatalog());
    Assert.assertEquals(TEST_DATABASE_NAME, context1.getDb());
  }
}
