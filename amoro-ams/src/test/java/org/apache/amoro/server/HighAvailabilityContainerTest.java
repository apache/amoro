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

package org.apache.amoro.server;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.util.KerberizedTestHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public class HighAvailabilityContainerTest extends KerberizedTestHelper {
  @Test
  public void setupKerberosAuth() throws Exception {
    tryWithSecurityEnabled(
        () -> {
          try {
            Configurations conf = new Configurations();
            File keytab = File.createTempFile("amoro", ".keytab");
            String principal = "amoro/_HOST@apache.org";

            conf.set(
                AmoroManagementConf.HA_ZOOKEEPER_AUTH_KEYTAB, keytab.getCanonicalPath().toString());
            conf.set(AmoroManagementConf.HA_ZOOKEEPER_AUTH_PRINCIPAL, principal);
            conf.set(AmoroManagementConf.HA_ZOOKEEPER_AUTH_TYPE, "KERBEROS");

            HighAvailabilityContainer.setupZookeeperAuth(conf);
            Configuration configuration = Configuration.getConfiguration();
            AppConfigurationEntry[] entries =
                configuration.getAppConfigurationEntry("AmoroZooKeeperClient");

            Assertions.assertEquals(
                "com.sun.security.auth.module.Krb5LoginModule", entries[0].getLoginModuleName());
            Map<String, ?> options = entries[0].getOptions();

            String hostname =
                StringUtils.lowerCase(InetAddress.getLocalHost().getCanonicalHostName());
            Assertions.assertEquals("amoro/" + hostname + "@apache.org", options.get("principal"));
            Assertions.assertTrue(Boolean.parseBoolean(options.get("useKeyTab").toString()));

            conf.set(AmoroManagementConf.HA_ZOOKEEPER_AUTH_KEYTAB, keytab.getName());
            IOException e =
                assertThrows(
                    IOException.class, () -> HighAvailabilityContainer.setupZookeeperAuth(conf));
            Assertions.assertTrue(e.getMessage().contains("does not exist"));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
