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

package com.netease.arctic.ams.server.controller;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.config.ConfigFileProperties;
import com.netease.arctic.ams.server.controller.response.ErrorResponse;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.model.CatalogRegisterInfo;
import com.netease.arctic.ams.server.model.CatalogSettingInfo;
import com.netease.arctic.ams.server.model.CatalogSettingInfo.ConfigFileItem;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.PlatformFileInfoService;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/25 14:58
 * @Description:
 */
public class CatalogController extends RestBaseController {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);
  private static final IMetaService iMetaService = ServiceContainer.getMetaService();
  private static CatalogMetadataService catalogMetadataService = ServiceContainer.getCatalogMetadataService();
  private static PlatformFileInfoService platformFileInfoService = ServiceContainer.getPlatformFileInfoService();

  private static final String AUTH_CONFIG_KEY_TYPE = "auth_config.type";
  private static final String AUTH_CONFIG_TYPE_VALUE_SIMPLE = "simple";
  private static final String AUTH_CONFIG_TYPE_VALUE_KERBEROS = "kerberos";
  private static final String AUTH_CONFIG_KEY_KEYTAB = "auth_config.keytab";
  private static final String AUTH_CONFIG_KEY_PRINCIPAL = "auth_config.principal";
  private static final String AUTH_CONFIG_HADOOP_USERNAME = "auth_config.hadoop_username";
  private static final String AUTH_CONFIG_KEY_KRB5 = "auth_config.krb5";

  private static final String CONFIG_TYPE_STORAGE = "storage-config";
  private static final String CONFIG_TYPE_AUTH = "auth-config";

  /**
   * get catalog Type list
   *
   * @param ctx
   */
  public static void getCatalogTypeList(Context ctx) {
    ctx.json(OkResponse.of(Arrays.asList(CatalogMetaProperties.CATALOG_TYPE_HIVE,
            CatalogMetaProperties.CATALOG_TYPE_HADOOP)));
  }

  /**
   * convert server authconfig to metaAuthConfig
   *
   * @param serverAuthConfig
   * @return
   */
  private static Map<String, String> authConvertFromServerToMeta(Map<String, String> serverAuthConfig) {
    Map<String, String> metaAuthConfig = new HashMap();
    String authType = serverAuthConfig.get(AUTH_CONFIG_KEY_TYPE).toLowerCase();
    metaAuthConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE, authType);
    if (authType.equals(AUTH_CONFIG_TYPE_VALUE_SIMPLE)) {
      metaAuthConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
              serverAuthConfig.get(AUTH_CONFIG_HADOOP_USERNAME));
    } else if (authType.equals(AUTH_CONFIG_TYPE_VALUE_KERBEROS)) {
      String keytabFileId = serverAuthConfig.get(AUTH_CONFIG_KEY_KEYTAB);
      String keytabB64 = platformFileInfoService.getFileContentB64ById(Integer.valueOf(keytabFileId));
      metaAuthConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB, keytabB64);

      String krbFileId = serverAuthConfig.get(AUTH_CONFIG_KEY_KEYTAB);
      String krbB64 = platformFileInfoService.getFileContentB64ById(Integer.valueOf(krbFileId));
      metaAuthConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5, krbB64);

      metaAuthConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL,
              serverAuthConfig.get(AUTH_CONFIG_KEY_PRINCIPAL));
    }
    return metaAuthConfig;
  }

  /**
   * convert meta authconfig to server authconfigDTO
   *
   * @param metaAuthConfig
   * @return
   */
  private static Map<String, Object> authConvertFromMetaToServer(String catalogName,
                                                                 Map<String, String> metaAuthConfig) {
    Map<String, Object> serverAuthConfig = new HashMap<String, Object>();
    serverAuthConfig.put(AUTH_CONFIG_KEY_TYPE,
            metaAuthConfig.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE));
    serverAuthConfig.put(AUTH_CONFIG_HADOOP_USERNAME,
            metaAuthConfig.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME));

    serverAuthConfig.put(AUTH_CONFIG_KEY_PRINCIPAL,
            metaAuthConfig.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL));

    serverAuthConfig.put(AUTH_CONFIG_KEY_KEYTAB, new ConfigFileItem("keytab",
            constructCatalogConfigFileUrl(catalogName, CONFIG_TYPE_AUTH,
                    CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB.replace("\\.", "-"))));

    serverAuthConfig.put(AUTH_CONFIG_KEY_KRB5, new ConfigFileItem("krb5.conf",
            constructCatalogConfigFileUrl(catalogName, CONFIG_TYPE_AUTH,
                    CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5.replace("\\.", "-"))));
    return serverAuthConfig;
  }

  private static Map<String, Object> storageConvertFromMetaToServer(String catalogName, Map<String, String> config) {

    String catalogConfPrefix = ConfigFileProperties.CATALOG_STORAGE_CONFIG + ".";
    Map<String, Object> storageConfig = new HashMap<>();
    // todo: need to consider some catalog in config.yaml without xxx-site.id configuration.
    Map<String, String> coreSiteItem = new HashMap<>();
    storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_CORE_SITE, new ConfigFileItem(
            ConfigFileProperties.CATALOG_CORE_SITE + ".xml",
            constructCatalogConfigFileUrl(catalogName, CONFIG_TYPE_STORAGE,
                    CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE.replace("\\.", "-"))));

    storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_HDFS_SITE, new ConfigFileItem(
            ConfigFileProperties.CATALOG_HDFS_SITE + ".xml",
            constructCatalogConfigFileUrl(catalogName, CONFIG_TYPE_STORAGE,
                    CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE.replace("\\.", "-"))));

    storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_HIVE_SITE, new ConfigFileItem(
            ConfigFileProperties.CATALOG_HIVE_SITE + ".xml",
            constructCatalogConfigFileUrl(catalogName, CONFIG_TYPE_STORAGE,
                    CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE.replace("\\.", "-"))));

    storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_STORAGE_TYPE,
            config.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE));
    return storageConfig;

  }

  /**
   * constrct catalogmeta through catalog registerinfo
   *
   * @param info
   * @return
   */
  private static CatalogMeta constructCatalogMeta(CatalogRegisterInfo info) {

    CatalogMeta catalogMeta = new CatalogMeta();
    catalogMeta.setCatalogName(info.getName());
    catalogMeta.setCatalogType(info.getType());
    catalogMeta.setCatalogProperties(info.getProperties());
    catalogMeta.setAuthConfigs(authConvertFromServerToMeta(info.getAuthConfig()));

    // change fileId to base64Code
    Map<String, String> metaStorageConfig = new HashMap<String, String>();
    String catalogConfPrefix = ConfigFileProperties.CATALOG_STORAGE_CONFIG + ".";

    metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
            info.getAuthConfig().get(catalogConfPrefix + ConfigFileProperties.CATALOG_STORAGE_TYPE));

    String hdfsSiteFileId = info.getAuthConfig()
            .get(catalogConfPrefix + ConfigFileProperties.CATALOG_HDFS_SITE);
    String hdfsSite = platformFileInfoService.getFileContentB64ById(Integer.valueOf(hdfsSiteFileId));

    metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, hdfsSite);
    //metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE_ID, hdfsSiteFileId);

    String coreSite = platformFileInfoService.getFileContentB64ById(Integer.valueOf(info.getAuthConfig()
            .get(catalogConfPrefix + ConfigFileProperties.CATALOG_CORE_SITE)));
    String coreSiteFileId = info.getAuthConfig()
            .get(catalogConfPrefix + ConfigFileProperties.CATALOG_CORE_SITE);
    metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, coreSite);
    //metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE_ID, coreSiteFileId);


    String hiveSite = platformFileInfoService.getFileContentB64ById(Integer.valueOf(info.getAuthConfig()
            .get(catalogConfPrefix + ConfigFileProperties.CATALOG_HIVE_SITE)));
    String hiveSiteFileId = info.getAuthConfig()
            .get(catalogConfPrefix + ConfigFileProperties.CATALOG_HIVE_SITE);
    metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE, hiveSite);
    // metaStorageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE_ID, hiveSiteFileId);

    catalogMeta.setStorageConfigs(metaStorageConfig);
    return catalogMeta;
  }

  /**
   * register catalog to ams
   *
   * @param ctx
   */
  public static void createCatalog(Context ctx) {
    CatalogRegisterInfo info = ctx.bodyAsClass(CatalogRegisterInfo.class);
    if (info.getAuthConfig() == null ||
            info.getStorageConfig() == null ||
            info.getProperties() == null) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Some configuration is null", null));
    }
    if (catalogMetadataService.catalogExist(info.getName())) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "duplicate catalog name!", null));
      return;
    }

    CatalogMeta catalogMeta = constructCatalogMeta(info);
    catalogMetadataService.addCatalog(catalogMeta);
    ctx.json(OkResponse.of(""));
  }

  /**
   * get detail of some catalog
   *
   * @param ctx
   */
  public static void getCatalogDetail(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    CatalogMeta catalogMeta = catalogMetadataService.getCatalog(catalogName);
    CatalogSettingInfo info = new CatalogSettingInfo();
    String catalogConfPrefix = ConfigFileProperties.CATALOG_STORAGE_CONFIG + ".";

    if (catalogMetadataService.catalogExist(catalogName)) {
      info.setName(catalogMeta.getCatalogName());
      info.setType(catalogMeta.getCatalogType());
      info.setAuthConfig(authConvertFromMetaToServer(catalogName, catalogMeta.getAuthConfigs()));
      Map<String, Object> storageConfig = new HashMap<>();
      storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_CORE_SITE,
              catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE_ID));
      storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_HDFS_SITE,
              catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE_ID));
      storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_HIVE_SITE,
              catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE_ID));
      storageConfig.put(catalogConfPrefix + ConfigFileProperties.CATALOG_STORAGE_TYPE,
              catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE));
      info.setStorageConfig(storageConfig);
      info.setProperties(catalogMeta.getCatalogProperties());
      ctx.json(OkResponse.of(info));
      return;
    }
    ctx.json(OkResponse.of(null));
  }


  /**
   * get detail of some catalog
   * 1、first check whether there are some tables in catalog.
   *
   * @param ctx
   */
  public static void updateCatalog(Context ctx) {
    CatalogRegisterInfo info = ctx.bodyAsClass(CatalogRegisterInfo.class);
    if (info.getAuthConfig() == null ||
            info.getStorageConfig() == null) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Some configuration is null", null));
    }
    if (!catalogMetadataService.catalogExist(info.getName())) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "catalog doesn't exist!", null));
      return;
    }
    // check only some item can be modified!
    CatalogMeta catalogMeta = constructCatalogMeta(info);
    catalogMetadataService.updateCatalog(catalogMeta);
    ctx.json(OkResponse.of(null));
  }

  /**
   * check whether we could delete the catalog
   *
   * @param ctx
   */
  public static void catalogDeleteCheck(Context ctx) {
    // check
    Integer tblCount = 0;
    if (StringUtils.isNotEmpty(ctx.pathParam("catalogName"))) {
      tblCount = iMetaService.getTableCountInCatalog(ctx.pathParam("catalogName"));
    } else {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "catalog is empty!", null));
      return;
    }
    ctx.json(OkResponse.of(tblCount > 0 ? false : true));
  }

  /**
   * delete some catalog and infos associate with the catalog
   *
   * @param ctx
   */
  public static void deleteCatalog(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    // need check
    if (StringUtils.isEmpty(catalogName)) {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "catalog is empty!", null));
      return;
    }
    Integer tblCount = iMetaService.getTableCountInCatalog(catalogName);
    if (tblCount == 0) {
      catalogMetadataService.deleteCatalog(catalogName);
      ctx.json(OkResponse.of("OK"));
      return;

    } else {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Some tables in catalog!", null));
    }
  }

  /**
   * construct a url
   *
   * @return
   */
  public static String constructCatalogConfigFileUrl(String catalogName, String type, String key) {
    return String.format("/ams/v1/catalogs/%s/config/%s/%s", catalogName,
            type, key.replaceAll("\\.", "-"));
  }

  /**
   * get file content of authconfig/storageconfig config file
   * get("/catalogs/{catalogName}/config/{type}/{key}
   *
   * @param ctx
   */
  public static void getCatalogConfFileContent(Context ctx) {
    String catalogName = ctx.pathParam("catalogName");
    String confType = ctx.pathParam("type");
    String configKey = ctx.pathParam("key");

    if (StringUtils.isEmpty(catalogName) || StringUtils.isEmpty(confType) || StringUtils.isEmpty(configKey)) {
      ctx.json(new ErrorResponse("catalogName or auth type or configkey is null!"));
      return;
    }

    // get file content from catlaog.
    CatalogMeta catalogMeta = catalogMetadataService.getCatalog(catalogName);
    if (CONFIG_TYPE_STORAGE.equalsIgnoreCase(confType)) {
      Map<String, String> storageConfig = catalogMeta.getStorageConfigs();
      String key = configKey.replaceAll("-", "\\.");
      ctx.result(new String(Base64.getDecoder().decode(storageConfig.get(key))));
      return;
    } else if (CONFIG_TYPE_AUTH.equalsIgnoreCase(confType)) {
      Map<String, String> storageConfig = catalogMeta.getAuthConfigs();
      String key = configKey.replaceAll("-", "\\.");
      ctx.result(new String(Base64.getDecoder().decode(storageConfig.get(key))));
      return;
    } else {
      ctx.json(new ErrorResponse("Invalid request for " + confType));
      return;
    }
  }
}
