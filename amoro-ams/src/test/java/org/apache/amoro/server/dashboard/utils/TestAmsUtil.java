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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.Constants;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAmsUtil {

  @Test
  public void testGetAMSThriftAddressWithoutHa() {
    Configurations conf = new Configurations();
    conf.setBoolean(AmoroManagementConf.HA_ENABLE, false);
    conf.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    conf.set(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    conf.set(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);

    Assertions.assertEquals(
        "thrift://127.0.0.1:1260",
        AmsUtil.getAMSThriftAddress(conf, Constants.THRIFT_TABLE_SERVICE_NAME));
    Assertions.assertEquals(
        "thrift://127.0.0.1:1261",
        AmsUtil.getAMSThriftAddress(conf, Constants.THRIFT_OPTIMIZING_SERVICE_NAME));
  }

  @Test
  public void testGetAMSThriftAddressWithZkHa() {
    Configurations conf = new Configurations();
    conf.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    conf.setString(AmoroManagementConf.HA_TYPE, AmoroManagementConf.HA_TYPE_ZK);
    conf.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    conf.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");

    Assertions.assertEquals(
        "zookeeper://127.0.0.1:2181/test-cluster",
        AmsUtil.getAMSThriftAddress(conf, Constants.THRIFT_TABLE_SERVICE_NAME));
  }

  @Test
  public void testGetAMSThriftAddressWithDatabaseHa() {
    // HA enabled but backed by database: the thrift address must fall back to the direct
    // host:port form instead of the ZooKeeper address.
    Configurations conf = new Configurations();
    conf.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    conf.setString(AmoroManagementConf.HA_TYPE, AmoroManagementConf.HA_TYPE_DATABASE);
    conf.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    conf.set(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    conf.set(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);

    Assertions.assertEquals(
        "thrift://127.0.0.1:1260",
        AmsUtil.getAMSThriftAddress(conf, Constants.THRIFT_TABLE_SERVICE_NAME));
    Assertions.assertEquals(
        "thrift://127.0.0.1:1261",
        AmsUtil.getAMSThriftAddress(conf, Constants.THRIFT_OPTIMIZING_SERVICE_NAME));
  }
}
