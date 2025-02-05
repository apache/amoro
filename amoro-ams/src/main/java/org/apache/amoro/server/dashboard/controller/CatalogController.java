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

package org.apache.amoro.server.dashboard.controller;

import static org.apache.amoro.TableFormat.HUDI;
import static org.apache.amoro.TableFormat.ICEBERG;
import static org.apache.amoro.TableFormat.MIXED_HIVE;
import static org.apache.amoro.TableFormat.MIXED_ICEBERG;
import static org.apache.amoro.TableFormat.PAIMON;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_ACCESS_KEY;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_SECRET_KEY;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_AK_SK;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_KERBEROS;
import static org.apache.amoro.properties.CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_CUSTOM;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_GLUE;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;
import static org.apache.amoro.properties.CatalogMetaProperties.FILE_CONTENT_BASE64_SUFFIX;
import static org.apache.amoro.properties.CatalogMetaProperties.KEY_WAREHOUSE;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_OSS_ENDPOINT;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_REGION;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_S3_ENDPOINT;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_OSS;
import static org.apache.amoro.properties.CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_S3;
import static org.apache.amoro.properties.CatalogMetaProperties.TABLE_FORMATS;

import io.javalin.http.Context;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.PlatformFileManager;
import org.apache.amoro.server.dashboard.model.CatalogRegisterInfo;
import org.apache.amoro.server.dashboard.model.CatalogSettingInfo;
import org.apache.amoro.server.dashboard.model.CatalogSettingInfo.ConfigFileItem;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.utils.DesensitizationUtil;
import org.apache.amoro.server.dashboard.utils.PropertiesUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aliyun.AliyunProperties;
import org.apache.iceberg.aws.AwsClientProperties;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.aws.s3.S3FileIOProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** The controller that handles catalog requests. */
public class CatalogController {
  private static final String CONFIG_TYPE_STORAGE = "storage-config";
  private static final String CONFIG_TYPE_AUTH = "auth-config";
  // <configuration></configuration>  encoded with base64
  private static final String EMPTY_XML_BASE64 = "PGNvbmZpZ3VyYXRpb24+PC9jb25maWd1cmF0aW9uPg==";
  private static final Map<String, List<String>> CATALOG_REQUIRED_PROPERTIES;
  private static final Set<CatalogDescriptor> VALIDATE_CATALOGS;

  static {
    CATALOG_REQUIRED_PROPERTIES = Maps.newHashMap();
    CATALOG_REQUIRED_PROPERTIES.put(CATALOG_TYPE_AMS, Lists.newArrayList(KEY_WAREHOUSE));
    CATALOG_REQUIRED_PROPERTIES.put(
        CATALOG_TYPE_HADOOP, Lists.newArrayList(CatalogProperties.WAREHOUSE_LOCATION));
    CATALOG_REQUIRED_PROPERTIES.put(
        CATALOG_TYPE_GLUE, Lists.newArrayList(CatalogProperties.WAREHOUSE_LOCATION));
    CATALOG_REQUIRED_PROPERTIES.put(
        CATALOG_TYPE_CUSTOM, Lists.newArrayList(CatalogProperties.CATALOG_IMPL));
  }

  static {
    VALIDATE_CATALOGS = Sets.newHashSet();
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_S3, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_S3, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_OSS, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_OSS, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_AMS, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HIVE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HIVE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HIVE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, MIXED_HIVE));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HIVE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, PAIMON));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HIVE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, HUDI));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(
            CATALOG_TYPE_HADOOP, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HADOOP, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HADOOP, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, PAIMON));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HADOOP, STORAGE_CONFIGS_VALUE_TYPE_S3, PAIMON));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_GLUE, STORAGE_CONFIGS_VALUE_TYPE_S3, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_GLUE, STORAGE_CONFIGS_VALUE_TYPE_S3, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_CUSTOM, STORAGE_CONFIGS_VALUE_TYPE_S3, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_CUSTOM, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_HADOOP, STORAGE_CONFIGS_VALUE_TYPE_OSS, PAIMON));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_GLUE, STORAGE_CONFIGS_VALUE_TYPE_OSS, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_GLUE, STORAGE_CONFIGS_VALUE_TYPE_OSS, MIXED_ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(CATALOG_TYPE_CUSTOM, STORAGE_CONFIGS_VALUE_TYPE_OSS, ICEBERG));
    VALIDATE_CATALOGS.add(
        CatalogDescriptor.of(
            CATALOG_TYPE_CUSTOM, STORAGE_CONFIGS_VALUE_TYPE_HADOOP, MIXED_ICEBERG));
  }

  private final PlatformFileManager platformFileInfoService;
  private final CatalogManager catalogService;

  public CatalogController(
      CatalogManager catalogService, PlatformFileManager platformFileInfoService) {
    this.catalogService = catalogService;
    this.platformFileInfoService = platformFileInfoService;
  }

  private static Set<String> getHiddenCatalogTableProperties() {
    return Sets.newHashSet(TableProperties.SELF_OPTIMIZING_GROUP);
  }

  private static Set<String> getHiddenCatalogProperties(CatalogRegisterInfo info) {
    return getHiddenCatalogProperties(
        info.getType(), info.getAuthConfig(), info.getStorageConfig());
  }

  private static Set<String> getHiddenCatalogProperties(CatalogSettingInfo info) {
    return getHiddenCatalogProperties(
        info.getType(), info.getAuthConfig(), info.getStorageConfig());
  }

  private static Set<String> getHiddenCatalogProperties(
      String type, Map<String, ?> authConfig, Map<String, ?> storageConfig) {
    Set<String> hiddenProperties = Sets.newHashSet(TABLE_FORMATS);
    if (AUTH_CONFIGS_VALUE_TYPE_AK_SK.equalsIgnoreCase(
        String.valueOf(authConfig.get(AUTH_CONFIGS_KEY_TYPE)))) {
      hiddenProperties.add(S3FileIOProperties.ACCESS_KEY_ID);
      hiddenProperties.add(S3FileIOProperties.SECRET_ACCESS_KEY);
      hiddenProperties.add(AliyunProperties.CLIENT_ACCESS_KEY_ID);
      hiddenProperties.add(AliyunProperties.CLIENT_ACCESS_KEY_SECRET);
    }
    if (STORAGE_CONFIGS_VALUE_TYPE_S3.equals(storageConfig.get(STORAGE_CONFIGS_KEY_TYPE))) {
      hiddenProperties.add(AwsClientProperties.CLIENT_REGION);
      hiddenProperties.add(S3FileIOProperties.ENDPOINT);
    }
    if (STORAGE_CONFIGS_VALUE_TYPE_OSS.equals(storageConfig.get(STORAGE_CONFIGS_KEY_TYPE))) {
      hiddenProperties.add(AliyunProperties.OSS_ENDPOINT);
    }
    return hiddenProperties;
  }

  /** Get catalog Type list */
  public void getCatalogTypeList(Context ctx) {

    List<ImmutableMap<String, String>> catalogTypes = new ArrayList<>();
    String valueKey = "value";
    String displayKey = "display";
    catalogTypes.add(ImmutableMap.of(valueKey, CATALOG_TYPE_AMS, displayKey, "Amoro Metastore"));
    catalogTypes.add(ImmutableMap.of(valueKey, CATALOG_TYPE_HIVE, displayKey, "Hive Metastore"));
    catalogTypes.add(ImmutableMap.of(valueKey, CATALOG_TYPE_HADOOP, displayKey, "Filesystem"));
    catalogTypes.add(ImmutableMap.of(valueKey, CATALOG_TYPE_GLUE, displayKey, "Glue"));
    catalogTypes.add(ImmutableMap.of(valueKey, CATALOG_TYPE_CUSTOM, displayKey, "Custom"));
    ctx.json(OkResponse.of(catalogTypes));
  }

  private void fillAuthConfigs2CatalogMeta(
      CatalogMeta catalogMeta,
      Map<String, String> serverAuthConfig,
      CatalogMeta oldCatalogMeta,
      String storageType) {
    Map<String, String> metaAuthConfig = new HashMap<>();
    String authType =
        serverAuthConfig
            .getOrDefault(AUTH_CONFIGS_KEY_TYPE, AUTH_CONFIGS_VALUE_TYPE_SIMPLE)
            .toLowerCase();
    metaAuthConfig.put(AUTH_CONFIGS_KEY_TYPE, authType);
    Map<String, String> oldAuthConfig = new HashMap<>();
    if (oldCatalogMeta != null) {
      oldAuthConfig = oldCatalogMeta.getAuthConfigs();
    }

    switch (authType) {
      case AUTH_CONFIGS_VALUE_TYPE_SIMPLE:
        metaAuthConfig.put(
            AUTH_CONFIGS_KEY_HADOOP_USERNAME,
            serverAuthConfig.get(AUTH_CONFIGS_KEY_HADOOP_USERNAME));
        break;
      case AUTH_CONFIGS_VALUE_TYPE_KERBEROS:
        String krbB64 = getFileContent(serverAuthConfig, AUTH_CONFIGS_KEY_KRB5, oldAuthConfig);
        metaAuthConfig.put(AUTH_CONFIGS_KEY_KRB5, krbB64);

        String keytabB64 = getFileContent(serverAuthConfig, AUTH_CONFIGS_KEY_KEYTAB, oldAuthConfig);
        metaAuthConfig.put(AUTH_CONFIGS_KEY_KEYTAB, keytabB64);

        metaAuthConfig.put(
            AUTH_CONFIGS_KEY_PRINCIPAL, serverAuthConfig.get(AUTH_CONFIGS_KEY_PRINCIPAL));
        break;
      case AUTH_CONFIGS_VALUE_TYPE_AK_SK:
        metaAuthConfig.put(
            AUTH_CONFIGS_KEY_ACCESS_KEY, serverAuthConfig.get(AUTH_CONFIGS_KEY_ACCESS_KEY));
        metaAuthConfig.put(
            AUTH_CONFIGS_KEY_SECRET_KEY, serverAuthConfig.get(AUTH_CONFIGS_KEY_SECRET_KEY));

        CatalogUtil.copyProperty(
            serverAuthConfig,
            catalogMeta.getCatalogProperties(),
            AUTH_CONFIGS_KEY_ACCESS_KEY,
            getStorageAccessKey(storageType));
        CatalogUtil.copyProperty(
            serverAuthConfig,
            catalogMeta.getCatalogProperties(),
            AUTH_CONFIGS_KEY_SECRET_KEY,
            getStorageSecretKey(storageType));
        break;
    }
    catalogMeta.setAuthConfigs(metaAuthConfig);
  }

  private Map<String, Object> extractAuthConfigsFromCatalogMeta(
      String catalogName, CatalogMeta catalogMeta, String storageType) {
    Map<String, Object> serverAuthConfig = new HashMap<>();
    Map<String, String> metaAuthConfig = catalogMeta.getAuthConfigs();
    String authType =
        metaAuthConfig.getOrDefault(AUTH_CONFIGS_KEY_TYPE, AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    serverAuthConfig.put(AUTH_CONFIGS_KEY_TYPE, authType.toUpperCase());
    switch (authType) {
      case AUTH_CONFIGS_VALUE_TYPE_SIMPLE:
        serverAuthConfig.put(
            AUTH_CONFIGS_KEY_HADOOP_USERNAME, metaAuthConfig.get(AUTH_CONFIGS_KEY_HADOOP_USERNAME));
        break;
      case AUTH_CONFIGS_VALUE_TYPE_KERBEROS:
        serverAuthConfig.put(
            AUTH_CONFIGS_KEY_PRINCIPAL, metaAuthConfig.get(AUTH_CONFIGS_KEY_PRINCIPAL));

        serverAuthConfig.put(
            AUTH_CONFIGS_KEY_KEYTAB,
            new ConfigFileItem(
                catalogName + ".keytab",
                constructCatalogConfigFileUrl(
                    catalogName, CONFIG_TYPE_AUTH, AUTH_CONFIGS_KEY_KEYTAB.replace("\\.", "-"))));

        serverAuthConfig.put(
            AUTH_CONFIGS_KEY_KRB5,
            new ConfigFileItem(
                "krb5.conf",
                constructCatalogConfigFileUrl(
                    catalogName, CONFIG_TYPE_AUTH, AUTH_CONFIGS_KEY_KRB5.replace("\\.", "-"))));
        break;
      case AUTH_CONFIGS_VALUE_TYPE_AK_SK:
        CatalogUtil.copyProperty(
            catalogMeta.getCatalogProperties(),
            serverAuthConfig,
            getStorageAccessKey(storageType),
            AUTH_CONFIGS_KEY_ACCESS_KEY);
        CatalogUtil.copyProperty(
            catalogMeta.getCatalogProperties(),
            serverAuthConfig,
            getStorageSecretKey(storageType),
            AUTH_CONFIGS_KEY_SECRET_KEY);
        break;
    }

    return serverAuthConfig;
  }

  private String getStorageAccessKey(String storageType) {
    if (STORAGE_CONFIGS_VALUE_TYPE_OSS.equals(storageType)) {
      return AliyunProperties.CLIENT_ACCESS_KEY_ID;
    }
    // default s3
    return S3FileIOProperties.ACCESS_KEY_ID;
  }

  private String getStorageSecretKey(String storageType) {
    if (STORAGE_CONFIGS_VALUE_TYPE_OSS.equals(storageType)) {
      return AliyunProperties.CLIENT_ACCESS_KEY_SECRET;
    }
    // default s3
    return S3FileIOProperties.SECRET_ACCESS_KEY;
  }

  private Map<String, Object> extractStorageConfigsFromCatalogMeta(
      String catalogName, CatalogMeta catalogMeta, String storageType) {
    Map<String, Object> storageConfig = new HashMap<>();
    storageConfig.put(STORAGE_CONFIGS_KEY_TYPE, storageType);
    if (STORAGE_CONFIGS_VALUE_TYPE_HADOOP.equals(storageType)) {
      storageConfig.put(
          STORAGE_CONFIGS_KEY_CORE_SITE,
          new ConfigFileItem(
              AmoroManagementConf.CATALOG_CORE_SITE + ".xml",
              constructCatalogConfigFileUrl(
                  catalogName,
                  CONFIG_TYPE_STORAGE,
                  STORAGE_CONFIGS_KEY_CORE_SITE.replace("\\.", "-"))));

      storageConfig.put(
          STORAGE_CONFIGS_KEY_HDFS_SITE,
          new ConfigFileItem(
              AmoroManagementConf.CATALOG_HDFS_SITE + ".xml",
              constructCatalogConfigFileUrl(
                  catalogName,
                  CONFIG_TYPE_STORAGE,
                  STORAGE_CONFIGS_KEY_HDFS_SITE.replace("\\.", "-"))));

      storageConfig.put(
          STORAGE_CONFIGS_KEY_HIVE_SITE,
          new ConfigFileItem(
              AmoroManagementConf.CATALOG_HIVE_SITE + ".xml",
              constructCatalogConfigFileUrl(
                  catalogName,
                  CONFIG_TYPE_STORAGE,
                  STORAGE_CONFIGS_KEY_HIVE_SITE.replace("\\.", "-"))));
    } else if (STORAGE_CONFIGS_VALUE_TYPE_S3.equals(storageType)) {
      CatalogUtil.copyProperty(
          catalogMeta.getCatalogProperties(),
          storageConfig,
          AwsClientProperties.CLIENT_REGION,
          STORAGE_CONFIGS_KEY_REGION);
      CatalogUtil.copyProperty(
          catalogMeta.getCatalogProperties(),
          storageConfig,
          S3FileIOProperties.ENDPOINT,
          STORAGE_CONFIGS_KEY_S3_ENDPOINT);
    } else if (STORAGE_CONFIGS_VALUE_TYPE_OSS.equals(storageType)) {
      CatalogUtil.copyProperty(
          catalogMeta.getCatalogProperties(),
          storageConfig,
          AliyunProperties.OSS_ENDPOINT,
          STORAGE_CONFIGS_KEY_OSS_ENDPOINT);
    }

    return storageConfig;
  }

  /** Construct catalog meta through catalog register info. */
  private CatalogMeta constructCatalogMeta(CatalogRegisterInfo info, CatalogMeta oldCatalogMeta) {
    CatalogMeta catalogMeta = new CatalogMeta();
    catalogMeta.setCatalogName(info.getName());
    catalogMeta.setCatalogType(info.getType());
    catalogMeta.setCatalogProperties(
        PropertiesUtil.unionCatalogProperties(info.getTableProperties(), info.getProperties()));
    // fill catalog impl when catalog type is glue
    if (CatalogMetaProperties.CATALOG_TYPE_GLUE.equals(info.getType())) {
      catalogMeta.putToCatalogProperties(
          CatalogProperties.CATALOG_IMPL, GlueCatalog.class.getName());
    }
    catalogMeta.putToCatalogProperties(
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.SELF_OPTIMIZING_GROUP,
        info.getOptimizerGroup());
    String tableFormats;
    try {
      // validate table format
      tableFormats =
          info.getTableFormatList().stream()
              .map(item -> TableFormat.valueOf(item).name())
              .collect(Collectors.joining(","));
    } catch (Exception e) {
      throw new RuntimeException(
          "Invalid table format list, " + String.join(",", info.getTableFormatList()));
    }
    catalogMeta.getCatalogProperties().put(CatalogMetaProperties.TABLE_FORMATS, tableFormats);
    String storageType =
        info.getStorageConfig()
            .getOrDefault(STORAGE_CONFIGS_KEY_TYPE, STORAGE_CONFIGS_VALUE_TYPE_HADOOP);
    fillAuthConfigs2CatalogMeta(catalogMeta, info.getAuthConfig(), oldCatalogMeta, storageType);
    // change fileId to base64Code
    Map<String, String> metaStorageConfig = new HashMap<>();
    metaStorageConfig.put(STORAGE_CONFIGS_KEY_TYPE, storageType);
    if (storageType.equals(STORAGE_CONFIGS_VALUE_TYPE_HADOOP)) {
      List<String> metaKeyList =
          Arrays.asList(
              STORAGE_CONFIGS_KEY_HDFS_SITE,
              STORAGE_CONFIGS_KEY_CORE_SITE,
              STORAGE_CONFIGS_KEY_HIVE_SITE);

      // when update catalog, fileId won't be post when file doesn't been changed!
      int idx;
      for (idx = 0; idx < metaKeyList.size(); idx++) {
        String file =
            getFileContent(
                info.getStorageConfig(),
                metaKeyList.get(idx),
                oldCatalogMeta == null ? null : oldCatalogMeta.getStorageConfigs());
        metaStorageConfig.put(
            metaKeyList.get(idx), StringUtils.isEmpty(file) ? EMPTY_XML_BASE64 : file);
      }
    } else if (storageType.equals(STORAGE_CONFIGS_VALUE_TYPE_S3)) {
      CatalogUtil.copyProperty(
          info.getStorageConfig(),
          catalogMeta.getCatalogProperties(),
          STORAGE_CONFIGS_KEY_REGION,
          AwsClientProperties.CLIENT_REGION);
      CatalogUtil.copyProperty(
          info.getStorageConfig(),
          catalogMeta.getCatalogProperties(),
          STORAGE_CONFIGS_KEY_S3_ENDPOINT,
          S3FileIOProperties.ENDPOINT);
    } else if (storageType.equals(STORAGE_CONFIGS_VALUE_TYPE_OSS)) {
      CatalogUtil.copyProperty(
          info.getStorageConfig(),
          catalogMeta.getCatalogProperties(),
          STORAGE_CONFIGS_KEY_OSS_ENDPOINT,
          AliyunProperties.OSS_ENDPOINT);
    } else {
      throw new RuntimeException("Invalid storage type " + storageType);
    }

    catalogMeta.setStorageConfigs(metaStorageConfig);
    return catalogMeta;
  }

  private String getFileContent(
      Map<String, String> config, String fileKey, Map<String, String> oldConfig) {
    String contentBase64Key = fileKey + FILE_CONTENT_BASE64_SUFFIX;
    String contentBase64 = config.get(contentBase64Key);
    if (!StringUtils.isEmpty(contentBase64)) {
      return contentBase64;
    }
    String fileId = config.get(fileKey);
    if (!StringUtils.isEmpty(fileId)) {
      return platformFileInfoService.getFileContentB64ById(Integer.valueOf(fileId));
    }
    if (oldConfig != null) {
      return oldConfig.get(fileKey);
    }

    return null;
  }

  private void checkHiddenProperties(CatalogRegisterInfo info) {
    getHiddenCatalogTableProperties().stream()
        .filter(info.getTableProperties()::containsKey)
        .findAny()
        .ifPresent(
            hiddenCatalogTableProperty -> {
              throw new IllegalArgumentException(
                  String.format(
                      "Table property %s is not allowed to set", hiddenCatalogTableProperty));
            });
    getHiddenCatalogProperties(info).stream()
        .filter(info.getProperties()::containsKey)
        .findAny()
        .ifPresent(
            hiddenCatalogProperty -> {
              throw new IllegalArgumentException(
                  String.format(
                      "Catalog property %s is not allowed to set", hiddenCatalogProperty));
            });
  }

  private void removeHiddenProperties(CatalogSettingInfo info) {
    getHiddenCatalogTableProperties().forEach(info.getTableProperties()::remove);
    getHiddenCatalogProperties(info).forEach(info.getProperties()::remove);
  }

  private void maskSensitiveData(CatalogSettingInfo info) {
    if (info.getAuthConfig().containsKey(AUTH_CONFIGS_KEY_SECRET_KEY)) {
      info.getAuthConfig()
          .put(
              AUTH_CONFIGS_KEY_SECRET_KEY,
              DesensitizationUtil.desensitize(
                  info.getAuthConfig().get(AUTH_CONFIGS_KEY_SECRET_KEY)));
    }
  }

  private void unMaskSensitiveData(CatalogRegisterInfo newInfo, CatalogMeta oldCatalogMeta) {
    if (newInfo.getAuthConfig().containsKey(AUTH_CONFIGS_KEY_SECRET_KEY)) {
      Object secretKey = newInfo.getAuthConfig().get(AUTH_CONFIGS_KEY_SECRET_KEY);
      if (DesensitizationUtil.isDesensitized(secretKey)) {
        Preconditions.checkArgument(
            oldCatalogMeta.getCatalogProperties().containsKey(S3FileIOProperties.SECRET_ACCESS_KEY),
            "Secret key is not set before，must provide a valid secret key");
        newInfo
            .getAuthConfig()
            .put(
                AUTH_CONFIGS_KEY_SECRET_KEY,
                oldCatalogMeta.getCatalogProperties().get(S3FileIOProperties.SECRET_ACCESS_KEY));
      }
    }
  }

  /** Register catalog to ams. */
  public void createCatalog(Context ctx) {
    CatalogRegisterInfo info = ctx.bodyAsClass(CatalogRegisterInfo.class);
    validateCatalogRegisterInfo(info);
    if (catalogService.catalogExist(info.getName())) {
      throw new RuntimeException("Duplicate catalog name!");
    }
    CatalogMeta catalogMeta = constructCatalogMeta(info, null);
    catalogService.createCatalog(catalogMeta);
    ctx.json(OkResponse.of(""));
  }

  private void validateCatalogRegisterInfo(CatalogRegisterInfo info) {
    Preconditions.checkNotNull(info.getAuthConfig(), "Catalog auth config must not be null");
    Preconditions.checkNotNull(info.getStorageConfig(), "Catalog storage config must not be null");
    Preconditions.checkNotNull(info.getProperties(), "Catalog properties must not be null");
    Preconditions.checkNotNull(
        info.getTableProperties(), "Catalog table properties must not be null");
    Preconditions.checkArgument(
        info.getTableFormatList() != null && !info.getTableFormatList().isEmpty(),
        "Catalog table format list must not be empty");

    CatalogDescriptor.of(info).validate();
    checkHiddenProperties(info);

    List<String> requiredProperties = CATALOG_REQUIRED_PROPERTIES.get(info.getType());
    if (requiredProperties != null && !requiredProperties.isEmpty()) {
      for (String propertyName : requiredProperties) {
        Preconditions.checkArgument(
            info.getProperties().containsKey(propertyName),
            String.format("Catalog type:%s require property:%s.", info.getType(), propertyName));
      }
    }
  }

  /** Get detail of some catalog. */
  public void getCatalogDetail(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    CatalogMeta catalogMeta = catalogService.getCatalogMeta(catalogName);
    CatalogSettingInfo info = new CatalogSettingInfo();

    if (catalogService.catalogExist(catalogName)) {
      info.setName(catalogMeta.getCatalogName());
      // We create ams catalog with type hadoop in v0.3, we should be compatible with it.
      if (CATALOG_TYPE_HADOOP.equals(catalogMeta.getCatalogType())
          && !catalogMeta.getCatalogProperties().containsKey(TABLE_FORMATS)) {
        info.setType(CATALOG_TYPE_AMS);
      } else {
        info.setType(catalogMeta.getCatalogType());
      }
      String storageType = CatalogUtil.getCompatibleStorageType(catalogMeta.getStorageConfigs());
      info.setAuthConfig(extractAuthConfigsFromCatalogMeta(catalogName, catalogMeta, storageType));
      info.setStorageConfig(
          extractStorageConfigsFromCatalogMeta(catalogName, catalogMeta, storageType));
      // we put the table format single
      String tableFormat =
          catalogMeta.getCatalogProperties().get(CatalogMetaProperties.TABLE_FORMATS);
      if (StringUtils.isEmpty(tableFormat)) {
        if (catalogMeta.getCatalogType().equals(CATALOG_TYPE_HIVE)) {
          tableFormat = TableFormat.MIXED_HIVE.name();
        } else {
          tableFormat = TableFormat.MIXED_ICEBERG.name();
        }
      }
      info.setTableFormatList(Arrays.asList(tableFormat.split(",")));
      info.setProperties(
          PropertiesUtil.extractCatalogMetaProperties(catalogMeta.getCatalogProperties()));
      info.setTableProperties(
          PropertiesUtil.extractTableProperties(catalogMeta.getCatalogProperties()));
      info.setOptimizerGroup(
          info.getTableProperties()
              .getOrDefault(
                  TableProperties.SELF_OPTIMIZING_GROUP,
                  TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT));
      removeHiddenProperties(info);
      maskSensitiveData(info);
      ctx.json(OkResponse.of(info));
      return;
    }
    ctx.json(OkResponse.of(null));
  }

  /** Get detail of some catalog 1、first check whether there are some tables in catalog. */
  public void updateCatalog(Context ctx) {
    CatalogRegisterInfo info = ctx.bodyAsClass(CatalogRegisterInfo.class);
    validateCatalogRegisterInfo(info);
    CatalogMeta optCatalog = catalogService.getCatalogMeta(info.getName());
    Preconditions.checkNotNull(optCatalog, "Catalog not exist!");

    unMaskSensitiveData(info, optCatalog);
    // check only some item can be modified!
    CatalogMeta catalogMeta = constructCatalogMeta(info, optCatalog);
    catalogService.updateCatalog(catalogMeta);
    ctx.json(OkResponse.ok());
  }

  /** Check whether we could delete the catalog */
  public void catalogDeleteCheck(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(ctx.pathParam("catalogName")), "Catalog name is empty!");
    ServerCatalog serverCatalog = catalogService.getServerCatalog(catalogName);
    if (serverCatalog instanceof InternalCatalog) {
      ctx.json(OkResponse.of(serverCatalog.listTables().size() == 0));
    } else {
      ctx.json(OkResponse.of(true));
    }
  }

  /** Delete some catalog and information associate with the catalog */
  public void deleteCatalog(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(ctx.pathParam("catalogName")), "Catalog name is empty!");
    catalogService.dropCatalog(catalogName);
    ctx.json(OkResponse.of("OK"));
  }

  /** Construct a url */
  private String constructCatalogConfigFileUrl(String catalogName, String type, String key) {
    return String.format(
        "/api/ams/v1/catalogs/%s/config/%s/%s", catalogName, type, key.replaceAll("\\.", "-"));
  }

  /** Get the config file content uri("/catalogs/{catalogName}/config/{type}/{key} */
  public void getCatalogConfFileContent(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    String confType = ctx.pathParam("type");
    String configKey = ctx.pathParam("key");
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(catalogName)
            && StringUtils.isNotEmpty(confType)
            && StringUtils.isNotEmpty(configKey),
        "Catalog name or auth type or config key is null!");

    CatalogMeta catalogMeta = catalogService.getCatalogMeta(catalogName);
    if (CONFIG_TYPE_STORAGE.equalsIgnoreCase(confType)) {
      Map<String, String> storageConfig = catalogMeta.getStorageConfigs();
      String key = configKey.replaceAll("-", "\\.");
      ctx.result(Base64.getDecoder().decode(storageConfig.get(key)));
    } else if (CONFIG_TYPE_AUTH.equalsIgnoreCase(confType)) {
      Map<String, String> storageConfig = catalogMeta.getAuthConfigs();
      String key = configKey.replaceAll("-", "\\.");
      ctx.result(Base64.getDecoder().decode(storageConfig.get(key)));
    } else {
      throw new RuntimeException("Invalid request for " + confType);
    }
  }

  private static class CatalogDescriptor {
    private final String catalogType;
    private final String storageType;
    private final TableFormat tableFormat;

    public CatalogDescriptor(String catalogType, String storageType, TableFormat tableFormat) {
      this.catalogType = catalogType;
      this.storageType = storageType;
      this.tableFormat = tableFormat;
    }

    public static CatalogDescriptor of(
        String catalogType, String storageType, TableFormat tableFormat) {
      return new CatalogDescriptor(catalogType, storageType, tableFormat);
    }

    public static CatalogDescriptor of(CatalogRegisterInfo info) {
      // only support one table format now
      String tableFormat = info.getTableFormatList().get(0);
      String storageType = info.getStorageConfig().get(STORAGE_CONFIGS_KEY_TYPE);
      String catalogType = info.getType();
      return of(catalogType, storageType, TableFormat.valueOf(tableFormat));
    }

    public void validate() {
      Preconditions.checkArgument(VALIDATE_CATALOGS.contains(this), "Not support " + this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CatalogDescriptor that = (CatalogDescriptor) o;
      return Objects.equal(catalogType, that.catalogType)
          && Objects.equal(storageType, that.storageType)
          && tableFormat == that.tableFormat;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(catalogType, storageType, tableFormat);
    }

    @Override
    public String toString() {
      return String.format(
          "Metastore [%s], Storage Type [%s], Table Format [%s]",
          catalogType, storageType, tableFormat);
    }
  }
}
