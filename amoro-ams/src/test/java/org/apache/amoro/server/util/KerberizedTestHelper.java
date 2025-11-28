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

package org.apache.amoro.server.util;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.minikdc.MiniKdc;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public abstract class KerberizedTestHelper {

  private static final String clientPrincipalUser = "client";
  private static File baseDir;
  private static Properties kdcConf;
  private static final String hostName = "localhost";
  private static MiniKdc kdc;
  protected static String krb5ConfPath;
  private static File keytabFile;
  protected static String testKeytab;
  protected static String testPrincipal;
  protected static String testSpnegoPrincipal;

  @BeforeAll
  public static void beforeAll() throws Exception {
    baseDir = Files.createTempDirectory("kyuubi-kdc").toFile();
    kdcConf = MiniKdc.createConf();
    kdcConf.setProperty(MiniKdc.INSTANCE, KerberizedTestHelper.class.getSimpleName());
    kdcConf.setProperty(MiniKdc.ORG_NAME, "KerberizedTestHelper");
    kdcConf.setProperty(MiniKdc.ORG_DOMAIN, "COM");
    kdcConf.setProperty(MiniKdc.KDC_BIND_ADDRESS, hostName);
    kdcConf.setProperty(MiniKdc.KDC_PORT, "0");
    kdcConf.remove(MiniKdc.DEBUG);

    // Start MiniKdc
    kdc = new MiniKdc(kdcConf, baseDir);
    kdc.start();
    krb5ConfPath = kdc.getKrb5conf().getAbsolutePath();

    keytabFile = new File(baseDir, "kyuubi-test.keytab");
    testKeytab = keytabFile.getAbsolutePath();
    String tempTestPrincipal = clientPrincipalUser + "/" + hostName;
    String tempSpnegoPrincipal = "HTTP/" + hostName;
    kdc.createPrincipal(keytabFile, tempTestPrincipal, tempSpnegoPrincipal);
    rewriteKrb5Conf();
    testPrincipal = tempTestPrincipal + "@" + kdc.getRealm();
    testSpnegoPrincipal = tempSpnegoPrincipal + "@" + kdc.getRealm();
    System.out.println("KerberizedTest Principal: " + testPrincipal);
    System.out.println("KerberizedTest SPNEGO Principal: " + testSpnegoPrincipal);
    System.out.println("KerberizedTest Keytab: " + testKeytab);
  }

  /** In this method we rewrite krb5.conf to make kdc and client use the same enctypes */
  private static void rewriteKrb5Conf() throws IOException {
    File krb5conf = kdc.getKrb5conf();
    StringBuilder rewriteKrb5Conf = new StringBuilder();
    boolean rewritten = false;
    String addedConfig =
        addedKrb5Config("default_tkt_enctypes", "aes128-cts-hmac-sha1-96")
            + addedKrb5Config("default_tgs_enctypes", "aes128-cts-hmac-sha1-96")
            + addedKrb5Config("dns_lookup_realm", "true");

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(krb5conf), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains("libdefaults")) {
          rewritten = true;
          rewriteKrb5Conf.append(line).append(addedConfig).append(System.lineSeparator());
        } else if (line.contains(hostName)) {
          rewriteKrb5Conf
              .append(line)
              .append(System.lineSeparator())
              .append(line.replace(hostName, "tcp/" + hostName))
              .append(System.lineSeparator());
        } else if (!line.trim().startsWith("#")) {
          rewriteKrb5Conf.append(line).append(System.lineSeparator());
        }
      }
    }

    String krb5confStr;
    if (!rewritten) {
      krb5confStr =
          "[libdefaults]"
              + addedConfig
              + System.lineSeparator()
              + System.lineSeparator()
              + rewriteKrb5Conf;
    } else {
      krb5confStr = rewriteKrb5Conf.toString();
    }

    // Overwrite krb5.conf
    try (BufferedWriter writer =
        Files.newBufferedWriter(krb5conf.toPath(), StandardCharsets.UTF_8)) {
      writer.write(krb5confStr);
    }
    System.out.println("krb5.conf file content: " + krb5confStr);
  }

  private static String addedKrb5Config(String key, String value) {
    return System.lineSeparator() + "    " + key + "=" + value;
  }

  @AfterAll
  public static void afterAll() {
    if (kdc != null) {
      kdc.stop();
    }
    if (baseDir != null) {
      FileUtils.deleteQuietly(baseDir);
    }
  }

  // Usage: tryWithSecurityEnabled(() -> { /* test code */ });
  public static void tryWithSecurityEnabled(Runnable block) throws Exception {
    Configuration conf = new Configuration();
    Assertions.assertFalse(UserGroupInformation.isSecurityEnabled());
    UserGroupInformation currentUser = UserGroupInformation.getCurrentUser();
    String authType = "hadoop.security.authentication";
    try {
      conf.set(authType, "KERBEROS");
      conf.set("hadoop.security.auth_to_local", "DEFAULT RULE:[1:$1] RULE:[2:$1]");
      System.setProperty("java.security.krb5.conf", krb5ConfPath);
      UserGroupInformation.setConfiguration(conf);
      Assertions.assertTrue(UserGroupInformation.isSecurityEnabled());
      block.run();
    } finally {
      conf.unset(authType);
      System.clearProperty("java.security.krb5.conf");
      UserGroupInformation.setLoginUser(currentUser);
      UserGroupInformation.setConfiguration(conf);
      Assertions.assertFalse(UserGroupInformation.isSecurityEnabled());
    }
  }
}
