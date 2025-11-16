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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.util.KerberizedTestHelper;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

public class KinitAuxiliaryServiceTest extends KerberizedTestHelper {
  @Test
  public void testNonSecuredKinitService() throws IOException {
    KinitAuxiliaryService kintService = new KinitAuxiliaryService(new Configurations());
    kintService.start();
    assert (!UserGroupInformation.getCurrentUser().hasKerberosCredentials());
    kintService.stop();
  }

  @Test
  public void testSecuredKinitService() throws Exception {
    tryWithSecurityEnabled(
        new Runnable() {
          @Override
          public void run() {
            try {
              Configurations conf = new Configurations();
              conf.set(AmoroManagementConf.SERVER_KEYTAB, testKeytab);
              conf.set(AmoroManagementConf.SERVER_PRINCIPAL, testPrincipal);
              conf.set(AmoroManagementConf.SERVER_KINIT_INTERVAL, Duration.ZERO);
              KinitAuxiliaryService kintService = new KinitAuxiliaryService(conf);
              kintService.start();
              assert (UserGroupInformation.getCurrentUser().hasKerberosCredentials());
              assert (UserGroupInformation.getCurrentUser().isFromKeytab());
              kintService.stop();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        });
  }
}
