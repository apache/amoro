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
import org.apache.amoro.utils.ThreadUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KinitAuxiliaryService {

  private static final Logger LOG = LoggerFactory.getLogger(KinitAuxiliaryService.class);

  private ScheduledExecutorService executor;
  private String keytab;
  private String principal;
  private Long kinitInterval;
  private int kinitMaxAttempts;
  private volatile int kinitAttempts;
  private Runnable kinitTask;

  public KinitAuxiliaryService(Configurations conf) throws IOException {
    if (UserGroupInformation.isSecurityEnabled()) {
      keytab = conf.get(AmoroManagementConf.SERVER_KEYTAB);
      principal = conf.get(AmoroManagementConf.SERVER_PRINCIPAL);
      kinitInterval = conf.get(AmoroManagementConf.SERVER_KINIT_INTERVAL).toMillis();
      kinitMaxAttempts = conf.get(AmoroManagementConf.SERVER_KINIT_MAX_ATTEMPTS);

      if (StringUtils.isNoneBlank(keytab, principal)) {
        principal = SecurityUtil.getServerPrincipal(principal, "0.0.0.0");
        UserGroupInformation.loginUserFromKeytab(principal, keytab);
        Optional<String> krb5Conf =
            Optional.ofNullable(System.getProperty("java.security.krb5.conf"))
                .map(Optional::of)
                .orElse(Optional.ofNullable(System.getenv("KRB5_CONFIG")));
        String[] commands = {"kinit", "-kt", keytab, principal};
        ProcessBuilder kinitProc = new ProcessBuilder(commands).inheritIO();
        krb5Conf.ifPresent(krb5Config -> kinitProc.environment().put("KRB5_CONFIG", krb5Config));
        executor = ThreadUtil.newDaemonSingleThreadScheduledExecutor("KinitAuxiliaryService", true);
        kinitTask =
            new Runnable() {
              @Override
              public void run() {
                boolean needToAttempt = true;
                try {
                  Process process = kinitProc.start();
                  if (process.waitFor() == 0) {
                    needToAttempt = false;
                    LOG.info("Successfully {}", String.join(" ", commands));
                    kinitAttempts = 0;
                    executor.schedule(this, kinitInterval, TimeUnit.MILLISECONDS);
                  }
                } catch (InterruptedException e) {
                  needToAttempt = false;
                } catch (IOException e) {
                  LOG.error("Error {}", String.join(" ", commands), e);
                } finally {
                  if (needToAttempt) {
                    if (kinitAttempts >= kinitMaxAttempts) {
                      LOG.error("Failed to kinit with {} attempts, will exit...", kinitAttempts);
                      System.exit(-1);
                    }
                    kinitAttempts += 1;
                    executor.submit(this);
                  }
                }
              }
            };
      }
    }
  }

  public void start() {
    if (executor != null && kinitTask != null) {
      executor.submit(kinitTask);
    }
  }

  public void stop() {
    if (executor != null) {
      executor.shutdown();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        // do nothing
      }
    }
  }
}
