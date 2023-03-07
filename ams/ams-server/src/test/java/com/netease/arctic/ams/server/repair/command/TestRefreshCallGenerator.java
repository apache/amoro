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

import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.catalog.TableTestBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class TestRefreshCallGenerator extends TableTestBase {

  public TestRefreshCallGenerator() {
    super(TableFormat.MIXED_ICEBERG, true, true);
  }

  @Test
  public void test() throws TException, CallCommand.FullTableNameException {
    RefreshCallGenerator generator = new RefreshCallGenerator(getCatalogUrl());
    Assert.assertEquals("OK", generator.generate(TableTestHelpers.TEST_TABLE_ID.toString()).call(new Context()));
  }
}
