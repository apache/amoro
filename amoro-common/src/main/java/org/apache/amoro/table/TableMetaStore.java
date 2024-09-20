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

package org.apache.amoro.table;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Charsets;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.base.Strings;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.hash.Hashing;
import org.apache.amoro.shade.guava32.com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.util.KerberosName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.krb5.KrbException;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/** Stores hadoop config files */
public class TableMetaStore implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(TableMetaStore.class);

  // Share runtime context with same configuration as context is expensive to construct
  private static final ConcurrentHashMap<TableMetaStore, RuntimeContext> RUNTIME_CONTEXT_CACHE =
      new ConcurrentHashMap<>();

  public static final TableMetaStore EMPTY =
      TableMetaStore.builder().withConfiguration(new Configuration()).build();

  public static final String HADOOP_CONF_DIR = "conf.hadoop.dir";
  public static final String HIVE_SITE = "hive-site";
  public static final String HDFS_SITE = "hdfs-site";
  public static final String CORE_SITE = "core-site";
  public static final String KRB5_CONF = "krb.conf";
  public static final String KEYTAB = "krb.keytab";
  public static final String AUTH_METHOD = "auth.method";
  public static final String KEYTAB_LOGIN_USER = "krb.principal";
  public static final String SIMPLE_USER_NAME = "simple.user.name";
  public static final String AUTH_METHOD_AK_SK = "ak/sk";
  public static final String AUTH_METHOD_SIMPLE = "SIMPLE";
  public static final String AUTH_METHOD_KERBEROS = "KERBEROS";

  private static final String KRB_CONF_FILE_NAME = "krb5.conf";
  private static final String KEY_TAB_FILE_NAME = "krb.keytab";
  private static final String META_STORE_SITE_FILE_NAME = "hive-site.xml";
  private static final String HADOOP_USER_PROPERTY = "HADOOP_USER_NAME";
  private static final String KRB5_CONF_PROPERTY = "java.security.krb5.conf";

  private static Field UGI_PRINCIPLE_FIELD;
  private static Field UGI_KEYTAB_FIELD;
  private static boolean UGI_REFLECT;

  static {
    try {
      // We must reset the private static variables in UserGroupInformation when re-login
      UGI_PRINCIPLE_FIELD = UserGroupInformation.class.getDeclaredField("keytabPrincipal");
      UGI_PRINCIPLE_FIELD.setAccessible(true);
      UGI_KEYTAB_FIELD = UserGroupInformation.class.getDeclaredField("keytabFile");
      UGI_KEYTAB_FIELD.setAccessible(true);
      UGI_REFLECT = true;
    } catch (NoSuchFieldException e) {
      // Do not need to reflect if hadoop-common version is 3.1.0+
      UGI_REFLECT = false;
      LOG.info(
          "Hadoop version is 3.1.0+, configuration modification through reflection is not required.");
    }
  }

  /**
   * For Kerberos authentication, krb5.conf and keytab files need to be written in TM, and method
   * files need to be locked in-process exclusive locking
   */
  private static final Object lock = new Object();

  private final byte[] metaStoreSite;
  private final byte[] hdfsSite;
  private final byte[] coreSite;
  private final String authMethod;
  private final String hadoopUsername;
  private final byte[] krbKeyTab;
  private final byte[] krbConf;
  private final String krbPrincipal;
  private final boolean disableAuth;
  private final String accessKey;
  private final String secretKey;

  private transient RuntimeContext runtimeContext;
  private transient String authInformation;

  public static Builder builder() {
    return new Builder();
  }

  private TableMetaStore(
      byte[] metaStoreSite,
      byte[] hdfsSite,
      byte[] coreSite,
      String authMethod,
      String hadoopUsername,
      byte[] krbKeyTab,
      byte[] krbConf,
      String krbPrincipal,
      String accessKey,
      String secretKey,
      boolean disableAuth) {
    Preconditions.checkArgument(
        authMethod == null
            || AUTH_METHOD_SIMPLE.equals(authMethod)
            || AUTH_METHOD_KERBEROS.equals(authMethod)
            || AUTH_METHOD_AK_SK.equals(authMethod),
        "Error auth method:%s",
        authMethod);
    this.metaStoreSite = metaStoreSite;
    this.hdfsSite = hdfsSite;
    this.coreSite = coreSite;
    this.authMethod = authMethod;
    this.hadoopUsername = hadoopUsername;
    this.krbKeyTab = krbKeyTab;
    this.krbConf = krbConf;
    this.krbPrincipal = krbPrincipal;
    this.disableAuth = disableAuth;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  private TableMetaStore(Configuration configuration) {
    this.disableAuth = true;
    this.metaStoreSite = null;
    this.hdfsSite = null;
    this.coreSite = null;
    this.authMethod = null;
    this.hadoopUsername = null;
    this.krbKeyTab = null;
    this.krbConf = null;
    this.krbPrincipal = null;
    this.accessKey = null;
    this.secretKey = null;
    getRuntimeContext().setConfiguration(configuration);
  }

  public byte[] getMetaStoreSite() {
    return metaStoreSite;
  }

  public byte[] getHdfsSite() {
    return hdfsSite;
  }

  public byte[] getCoreSite() {
    return coreSite;
  }

  public byte[] getKrbKeyTab() {
    return krbKeyTab;
  }

  public byte[] getKrbConf() {
    return krbConf;
  }

  public String getKrbPrincipal() {
    return krbPrincipal;
  }

  public String getAuthMethod() {
    return authMethod;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public boolean isKerberosAuthMethod() {
    return AUTH_METHOD_KERBEROS.equalsIgnoreCase(authMethod);
  }

  public String getHadoopUsername() {
    return hadoopUsername;
  }

  public synchronized Configuration getConfiguration() {
    return getRuntimeContext().getConfiguration();
  }

  private synchronized UserGroupInformation getUGI() {
    return getRuntimeContext().getUGI();
  }

  public <T> T doAs(Callable<T> callable) {
    // if disableAuth, use process ugi to execute
    if (disableAuth
        || CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_AK_SK.equalsIgnoreCase(authMethod)) {
      return call(callable);
    }
    return Objects.requireNonNull(getUGI()).doAs((PrivilegedAction<T>) () -> call(callable));
  }

  /**
   * Login with configured catalog user and create a proxy user ugi. Then the operations are
   * performed within the doAs method of this proxy user ugi.
   */
  public <T> T doAsImpersonating(String proxyUser, Callable<T> callable) {
    // if disableAuth, use process ugi to execute
    if (disableAuth) {
      return call(callable);
    }
    // create proxy user ugi and execute
    UserGroupInformation proxyUgi =
        UserGroupInformation.createProxyUser(proxyUser, Objects.requireNonNull(getUGI()));
    LOG.debug(
        "Access through the proxy account {}, proxy ugi {}, original ugi {}.",
        proxyUser,
        proxyUgi,
        getUGI());
    return proxyUgi.doAs((PrivilegedAction<T>) () -> call(callable));
  }

  private <T> T call(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Throwable e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException("Run with ugi request failed.", e);
    }
  }

  public synchronized Optional<URL> getHiveSiteLocation() {
    return getRuntimeContext().getHiveSiteLocation();
  }

  private RuntimeContext getRuntimeContext() {
    if (runtimeContext == null) {
      RUNTIME_CONTEXT_CACHE.putIfAbsent(this, new RuntimeContext());
      runtimeContext = RUNTIME_CONTEXT_CACHE.get(this);
    }
    return runtimeContext;
  }

  private String authInformation() {
    if (authInformation == null) {
      StringBuilder stringBuilder = new StringBuilder();
      if (disableAuth) {
        stringBuilder.append("disable authentication");
      } else if (AUTH_METHOD_KERBEROS.equalsIgnoreCase(authMethod)) {
        stringBuilder.append(authMethod).append("(").append(krbPrincipal).append(")");
      } else if (AUTH_METHOD_SIMPLE.equalsIgnoreCase(authMethod)) {
        stringBuilder.append(authMethod).append("(").append(hadoopUsername).append(")");
      }
      authInformation = stringBuilder.toString();
    }
    return authInformation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableMetaStore that = (TableMetaStore) o;
    return Arrays.equals(metaStoreSite, that.metaStoreSite)
        && Arrays.equals(hdfsSite, that.hdfsSite)
        && Arrays.equals(coreSite, that.coreSite)
        && Objects.equals(authMethod, that.authMethod)
        && Objects.equals(hadoopUsername, that.hadoopUsername)
        && Arrays.equals(krbKeyTab, that.krbKeyTab)
        && Arrays.equals(krbConf, that.krbConf)
        && Objects.equals(krbPrincipal, that.krbPrincipal)
        && Objects.equals(disableAuth, that.disableAuth);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(disableAuth, authMethod, hadoopUsername, krbPrincipal);
    result = 31 * result + Arrays.hashCode(metaStoreSite);
    result = 31 * result + Arrays.hashCode(hdfsSite);
    result = 31 * result + Arrays.hashCode(coreSite);
    result = 31 * result + Arrays.hashCode(krbKeyTab);
    result = 31 * result + Arrays.hashCode(krbConf);
    return result;
  }

  @Override
  public String toString() {
    return authInformation();
  }

  class RuntimeContext {
    private Configuration configuration;
    private UserGroupInformation ugi;
    private Path confCachePath;

    Configuration getConfiguration() {
      if (configuration == null) {
        configuration = buildConfiguration(TableMetaStore.this);
      }
      return configuration;
    }

    private void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
    }

    synchronized Optional<URL> getHiveSiteLocation() {
      try {
        Path confPath = generateKrbConfPath();
        if (ArrayUtils.isEmpty(metaStoreSite)) {
          return Optional.empty();
        }
        Path hiveSitePath = Paths.get(confPath.toAbsolutePath().toString(), "hive-site.xml");
        if (!hiveSitePath.toFile().exists()) {
          hiveSitePath =
              Paths.get(saveConfInPath(confPath, META_STORE_SITE_FILE_NAME, metaStoreSite));
        }
        org.apache.hadoop.fs.Path hadoopPath =
            new org.apache.hadoop.fs.Path(hiveSitePath.toAbsolutePath().toString());
        org.apache.hadoop.fs.Path hadoopPathWithSchema =
            new org.apache.hadoop.fs.Path("file://" + hadoopPath.toUri().toString());
        return Optional.of(hadoopPathWithSchema.toUri().toURL());
      } catch (MalformedURLException e) {
        throw new RuntimeException("Failed to generate hive site location", e);
      }
    }

    public UserGroupInformation getUGI() {
      if (ugi == null) {
        try {
          if (TableMetaStore.AUTH_METHOD_SIMPLE.equals(authMethod)) {
            ugi = UserGroupInformation.createRemoteUser(hadoopUsername);
          } else if (TableMetaStore.AUTH_METHOD_KERBEROS.equals(authMethod)) {
            constructKerberosUgi();
          }
          LOG.info("Completed to build ugi {}", authInformation());
        } catch (IOException | KrbException e) {
          throw new RuntimeException("Fail to init user group information", e);
        }
      } else {
        if (TableMetaStore.AUTH_METHOD_KERBEROS.equals(authMethod)) {
          // re-construct
          if (!ugi.getAuthenticationMethod().toString().equals(authMethod)
              || (!ugi.getUserName().equals(krbPrincipal)
                  && !StringUtils.substringBefore(ugi.getUserName(), "@").equals(krbPrincipal))) {
            try {
              constructKerberosUgi();
              LOG.info("Completed to re-build ugi {}", authInformation());
            } catch (Exception e) {
              throw new RuntimeException("Fail to init user group information", e);
            }
          } else {
            // re-login
            reLoginKerberosUgi();
          }
        }
      }
      return ugi;
    }

    private void constructKerberosUgi() throws IOException, KrbException {
      Path confPath = generateKrbConfPath();
      String krbConfFile = saveConfInPath(confPath, KRB_CONF_FILE_NAME, krbConf);
      String keyTabFile = saveConfInPath(confPath, KEY_TAB_FILE_NAME, krbKeyTab);
      System.clearProperty(HADOOP_USER_PROPERTY);
      System.setProperty(KRB5_CONF_PROPERTY, krbConfFile);
      sun.security.krb5.Config.refresh();
      UserGroupInformation.setConfiguration(getConfiguration());
      KerberosName.resetDefaultRealm();
      this.ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(krbPrincipal, keyTabFile);
    }

    private void reLoginKerberosUgi() {
      synchronized (UserGroupInformation.class) {
        String oldKeytabFile = null;
        String oldPrincipal = null;
        if (UGI_REFLECT) {
          try {
            // use reflection to set private static field of UserGroupInformation for re-login
            // to fix static field reuse bug before hadoop-common version 3.1.0
            oldKeytabFile = (String) UGI_KEYTAB_FIELD.get(null);
            oldPrincipal = (String) UGI_PRINCIPLE_FIELD.get(null);
          } catch (IllegalAccessException e) {
            UGI_REFLECT = false;
            LOG.warn("Get kerberos configuration through reflection failed.", e);
          }
        }

        String oldSystemPrincipal = System.getProperty("sun.security.krb5.principal");
        boolean systemPrincipalChanged = false;
        try {
          if (!UserGroupInformation.isSecurityEnabled()) {
            UserGroupInformation.setConfiguration(getConfiguration());
            LOG.info(
                "Reset authentication method to Kerberos, isSecurityEnabled {},"
                    + " AuthenticationMethod {}, isKeytab {}",
                UserGroupInformation.isSecurityEnabled(),
                ugi.getAuthenticationMethod().toString(),
                ugi.isFromKeytab());
          }

          if (UGI_REFLECT) {
            try {
              UGI_PRINCIPLE_FIELD.set(null, krbPrincipal);
              UGI_KEYTAB_FIELD.set(null, getConfPath(confCachePath, KEY_TAB_FILE_NAME));
            } catch (IllegalAccessException e) {
              UGI_REFLECT = false;
              LOG.warn("Set kerberos configuration through reflection failed", e);
            }
          }

          if (oldSystemPrincipal != null && !oldSystemPrincipal.equals(krbPrincipal)) {
            System.setProperty("sun.security.krb5.principal", krbPrincipal);
            systemPrincipalChanged = true;
          }
          ugi.checkTGTAndReloginFromKeytab();
        } catch (Exception e) {
          throw new RuntimeException("Re-login from keytab failed", e);
        } finally {
          if (UGI_REFLECT) {
            try {
              UGI_PRINCIPLE_FIELD.set(null, oldPrincipal);
              UGI_KEYTAB_FIELD.set(null, oldKeytabFile);
            } catch (IllegalAccessException e) {
              UGI_REFLECT = false;
              LOG.warn("Get kerberos configuration through reflection failed", e);
            }
          }
          if (systemPrincipalChanged) {
            System.setProperty("sun.security.krb5.principal", oldSystemPrincipal);
          }
        }
      }
    }

    private Path generateKrbConfPath() {
      if (confCachePath == null) {
        String path = Paths.get("").toAbsolutePath().toString();
        String confPath =
            String.format(
                "%s/%s/%s",
                path,
                "amoro_krb_conf",
                md5() + "_" + ManagementFactory.getRuntimeMXBean().getName());
        LOG.info("Generated conf path: {}", confPath);
        Path p = Paths.get(confPath);
        if (!p.toFile().exists()) {
          p.toFile().mkdirs();
        }
        this.confCachePath = p;
      }
      return this.confCachePath;
    }

    private String saveConfInPath(Path confPath, String confName, byte[] confValues) {
      String confFile = getConfPath(confPath, confName);
      synchronized (lock) {
        if (!Paths.get(confFile).toFile().exists()) {
          try (FileOutputStream fileOutputStream = new FileOutputStream(confFile)) {
            ByteStreams.copy(new ByteArrayInputStream(confValues), fileOutputStream);
            LOG.info("Saved the configuration file to path {}.", confFile);
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to save conf files in work space", e);
          }
        } else {
          LOG.info("Configuration file already exists in {}.", confFile);
        }
      }
      return confFile;
    }

    private String getConfPath(Path confPath, String confName) {
      return String.format("%s/%s", confPath.toString(), confName);
    }

    private Configuration buildConfiguration(TableMetaStore metaStore) {
      Configuration configuration = new Configuration();
      if (!ArrayUtils.isEmpty(metaStore.getCoreSite())) {
        configuration.addResource(new ByteArrayInputStream(metaStore.getCoreSite()));
      }
      if (!ArrayUtils.isEmpty(metaStore.getHdfsSite())) {
        configuration.addResource(new ByteArrayInputStream(metaStore.getHdfsSite()));
      }
      if (!ArrayUtils.isEmpty(metaStore.getMetaStoreSite())) {
        configuration.addResource(new ByteArrayInputStream(metaStore.getMetaStoreSite()));
      }
      configuration.set(
          CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY, "true");
      // Enforce configuration resolve resources
      configuration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);

      // Avoid check hive version
      configuration.set("hive.metastore.schema.verification", "false");

      // It will encounter error(Required table missing : "DBS" in Catalog "" Schema "") when there
      // is not this param
      configuration.set("datanucleus.schema.autoCreateAll", "true");

      return configuration;
    }

    private String md5() {
      return Hashing.md5()
          .newHasher()
          .putString(
              "tableMetaStore:"
                  + base64(getHdfsSite())
                  + base64(getCoreSite())
                  + base64(getMetaStoreSite())
                  + base64(getKrbConf())
                  + base64(getKrbKeyTab())
                  + getKrbPrincipal(),
              Charsets.UTF_8)
          .hash()
          .toString();
    }

    private String base64(byte[] bytes) {
      return bytes == null ? "" : Base64.getEncoder().encodeToString(bytes);
    }
  }

  @SuppressWarnings("unused")
  public static class Builder {
    private byte[] metaStoreSite;
    private byte[] hdfsSite;
    private byte[] coreSite;
    private String authMethod;
    private String hadoopUsername;
    private byte[] krbKeyTab;
    private byte[] krbConf;
    private String krbPrincipal;
    private String accessKey;
    private String secretKey;
    private boolean disableAuth = true;
    private final Map<String, String> properties = Maps.newHashMap();
    private Configuration configuration;

    public Builder withMetaStoreSitePath(String metaStoreSitePath) {
      this.metaStoreSite = readBytesFromFile(metaStoreSitePath);
      return this;
    }

    public Builder withMetaStoreSite(byte[] metaStoreSiteBytes) {
      this.metaStoreSite = metaStoreSiteBytes;
      return this;
    }

    public Builder withBase64MetaStoreSite(String encodedMetaStoreSite) {
      this.metaStoreSite =
          StringUtils.isBlank(encodedMetaStoreSite)
              ? null
              : Base64.getDecoder().decode(encodedMetaStoreSite);
      return this;
    }

    public Builder withHdfsSitePath(String hdfsSitePath) {
      this.hdfsSite = readBytesFromFile(hdfsSitePath);
      return this;
    }

    public Builder withHdfsSite(byte[] hdfsSiteBytes) {
      this.hdfsSite = hdfsSiteBytes;
      return this;
    }

    public Builder withBase64HdfsSite(String encodedHdfsSite) {
      this.hdfsSite =
          StringUtils.isBlank(encodedHdfsSite) ? null : Base64.getDecoder().decode(encodedHdfsSite);
      return this;
    }

    public Builder withCoreSitePath(String coreSitePath) {
      this.coreSite = readBytesFromFile(coreSitePath);
      return this;
    }

    public Builder withCoreSite(byte[] coreSiteBytes) {
      this.coreSite = coreSiteBytes;
      return this;
    }

    public Builder withBase64CoreSite(String encodedCoreSite) {
      this.coreSite =
          StringUtils.isBlank(encodedCoreSite) ? null : Base64.getDecoder().decode(encodedCoreSite);
      return this;
    }

    public Builder withAkSkAuth(String accessKey, String secretKey) {
      this.disableAuth = false;
      this.authMethod = AUTH_METHOD_AK_SK;
      this.accessKey = accessKey;
      this.secretKey = secretKey;
      return this;
    }

    public Builder withSimpleAuth(String hadoopUsername) {
      this.disableAuth = false;
      this.authMethod = AUTH_METHOD_SIMPLE;
      this.hadoopUsername = hadoopUsername;
      return this;
    }

    public Builder withKrbAuth(String krbKeyTabPath, String krbConfPath, String krbPrincipal) {
      this.disableAuth = false;
      this.authMethod = AUTH_METHOD_KERBEROS;
      this.krbKeyTab = readBytesFromFile(krbKeyTabPath);
      this.krbConf = readBytesFromFile(krbConfPath);
      this.krbPrincipal = krbPrincipal;
      return this;
    }

    public Builder withKrbAuth(byte[] krbKeyTabBytes, byte[] krbConfBytes, String krbPrincipal) {
      this.disableAuth = false;
      this.authMethod = AUTH_METHOD_KERBEROS;
      this.krbKeyTab = krbKeyTabBytes;
      this.krbConf = krbConfBytes;
      this.krbPrincipal = krbPrincipal;
      return this;
    }

    public Builder withBase64KrbAuth(
        String encodedKrbKeytab, String encodedKrbConf, String krbPrincipal) {
      return withKrbAuth(
          Base64.getDecoder().decode(encodedKrbKeytab),
          Base64.getDecoder().decode(encodedKrbConf),
          krbPrincipal);
    }

    public Builder withAuth(
        String authMethod,
        String hadoopUsername,
        byte[] krbKeyTabBytes,
        byte[] krbConfBytes,
        String krbPrincipal) {
      this.disableAuth = false;
      this.authMethod = authMethod == null ? null : authMethod.toUpperCase();
      this.hadoopUsername = hadoopUsername;
      this.krbKeyTab = krbKeyTabBytes;
      this.krbConf = krbConfBytes;
      this.krbPrincipal = krbPrincipal;
      return this;
    }

    public Builder withBase64Auth(
        String authMethod,
        String hadoopUsername,
        String encodedKrbKeytab,
        String encodedKrbConf,
        String krbPrincipal) {
      this.disableAuth = false;
      byte[] keytab = null;
      if (encodedKrbKeytab != null) {
        keytab = Base64.getDecoder().decode(encodedKrbKeytab);
      }
      byte[] krbConf = null;
      if (krbPrincipal != null) {
        krbConf = Base64.getDecoder().decode(encodedKrbConf);
      }
      return withAuth(authMethod, hadoopUsername, keytab, krbConf, krbPrincipal);
    }

    public Builder withProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public Builder withConfiguration(Configuration configuration) {
      this.configuration = configuration;
      return this;
    }

    private byte[] readBytesFromFile(String filePath) {
      try {
        return IOUtils.toByteArray(Files.newInputStream(Paths.get(filePath)));
      } catch (IOException e) {
        throw new UncheckedIOException("Read config failed:" + filePath, e);
      }
    }

    private void readProperties() {
      String krbConfPath = null;
      String keyTabPath = null;
      if (properties.containsKey(HADOOP_CONF_DIR)) {
        String hadoopConfDir = properties.get(HADOOP_CONF_DIR);
        if (properties.containsKey(CORE_SITE)) {
          withCoreSitePath(String.format("%s/%s", hadoopConfDir, properties.get(CORE_SITE)));
        }
        if (properties.containsKey(HDFS_SITE)) {
          withHdfsSitePath(String.format("%s/%s", hadoopConfDir, properties.get(HDFS_SITE)));
        }
        if (properties.containsKey(HIVE_SITE)) {
          withMetaStoreSitePath(String.format("%s/%s", hadoopConfDir, properties.get(HIVE_SITE)));
        }
        if (properties.containsKey(KRB5_CONF)) {
          krbConfPath = String.format("%s/%s", hadoopConfDir, properties.get(KRB5_CONF));
        }
        if (properties.containsKey(KEYTAB)) {
          keyTabPath = String.format("%s/%s", hadoopConfDir, properties.get(KEYTAB));
        }
      }
      if (properties.containsKey(AUTH_METHOD)) {
        String authMethod = properties.get(AUTH_METHOD).toUpperCase();
        if (AUTH_METHOD_SIMPLE.equals(authMethod) && properties.containsKey(SIMPLE_USER_NAME)) {
          withSimpleAuth(properties.get(SIMPLE_USER_NAME));
        } else if (AUTH_METHOD_KERBEROS.equals(authMethod)
            && properties.containsKey(KEYTAB_LOGIN_USER)
            && !Strings.isNullOrEmpty(krbConfPath)
            && !Strings.isNullOrEmpty(keyTabPath)) {
          withKrbAuth(keyTabPath, krbConfPath, properties.get(KEYTAB_LOGIN_USER));
        }
      }
    }

    public TableMetaStore build() {
      if (disableAuth && configuration != null) {
        LOG.info("Build table meta store with local configuration:" + configuration);
        return new TableMetaStore(configuration);
      } else {
        readProperties();
        if (!AUTH_METHOD_AK_SK.equals(authMethod)) {
          Preconditions.checkNotNull(hdfsSite);
          Preconditions.checkNotNull(coreSite);
        }
        if (AUTH_METHOD_SIMPLE.equals(authMethod)) {
          Preconditions.checkNotNull(hadoopUsername);
        } else if (AUTH_METHOD_KERBEROS.equals(authMethod)) {
          Preconditions.checkNotNull(krbConf);
          Preconditions.checkNotNull(krbKeyTab);
          Preconditions.checkNotNull(krbPrincipal);
        } else if (AUTH_METHOD_AK_SK.equals(authMethod)) {
          Preconditions.checkNotNull(accessKey);
          Preconditions.checkNotNull(secretKey);
        } else if (authMethod != null) {
          throw new IllegalArgumentException("Unsupported auth method:" + authMethod);
        }

        LOG.info(
            "Build table meta store with configurations: authMethod:{}, hadoopUsername:{}, krbPrincipal:{}",
            authMethod,
            hadoopUsername,
            krbPrincipal);
        return new TableMetaStore(
            metaStoreSite,
            hdfsSite,
            coreSite,
            authMethod,
            hadoopUsername,
            krbKeyTab,
            krbConf,
            krbPrincipal,
            accessKey,
            secretKey,
            disableAuth);
      }
    }
  }
}
