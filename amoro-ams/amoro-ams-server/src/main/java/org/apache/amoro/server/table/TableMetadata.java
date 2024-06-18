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

package org.apache.amoro.server.table;

import static org.apache.amoro.table.PrimaryKeySpec.PRIMARY_KEY_COLUMN_JOIN_DELIMITER;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.properties.MetaTableProperties;
import org.apache.amoro.server.dashboard.utils.PropertiesUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableMetaStore;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TableMetadata implements Serializable {

  private TableMetadata() {}

  public TableMetadata(
      ServerTableIdentifier identifier, TableMeta tableMeta, CatalogMeta catalogMeta) {
    this.tableIdentifier = identifier;
    Map<String, String> properties = Maps.newHashMap(tableMeta.getProperties());
    Preconditions.checkNotNull(tableMeta.getFormat(), "lack require field: table format");
    if (tableMeta.getLocations() != null
        && tableMeta.getLocations().containsKey(MetaTableProperties.LOCATION_KEY_TABLE)) {
      this.tableLocation = tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_TABLE);
    }
    if (tableMeta.getLocations() != null
        && tableMeta.getLocations().containsKey(MetaTableProperties.LOCATION_KEY_BASE)) {
      this.baseLocation = tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_BASE);
    }
    if (tableMeta.getLocations() != null
        && tableMeta.getLocations().containsKey(MetaTableProperties.LOCATION_KEY_CHANGE)) {
      this.changeLocation = tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_CHANGE);
    }
    if (StringUtils.isBlank(this.tableLocation) || StringUtils.isBlank(this.baseLocation)) {
      throw new IllegalArgumentException("table location is required");
    }

    if (tableMeta.getKeySpec() == null
        || CollectionUtils.isEmpty(tableMeta.getKeySpec().getFields())) {
      this.primaryKey = PrimaryKeySpec.noPrimaryKey().description();
    } else {
      this.primaryKey =
          String.join(PRIMARY_KEY_COLUMN_JOIN_DELIMITER, tableMeta.getKeySpec().getFields());
    }
    this.metaStoreSite =
        catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE);
    this.hdfsSite =
        catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE);
    this.coreSite =
        catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE);
    this.authMethod = catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE);
    if (this.authMethod != null) {
      this.authMethod = this.authMethod.toUpperCase(Locale.ROOT);
    }
    this.hadoopUsername =
        catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME);
    this.krbKeytab =
        catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB);
    this.krbConf = catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5);
    this.krbPrincipal =
        catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL);
    this.properties = properties;
  }

  public TableMeta buildTableMeta() {
    TableMeta meta = new TableMeta();
    meta.setTableIdentifier(tableIdentifier.getIdentifier());
    Map<String, String> locations = new HashMap<>();
    PropertiesUtil.putNotNullProperties(
        locations, MetaTableProperties.LOCATION_KEY_TABLE, tableLocation);
    PropertiesUtil.putNotNullProperties(
        locations, MetaTableProperties.LOCATION_KEY_CHANGE, changeLocation);
    PropertiesUtil.putNotNullProperties(
        locations, MetaTableProperties.LOCATION_KEY_BASE, baseLocation);
    meta.setLocations(locations);

    Map<String, String> newProperties = new HashMap<>(properties);
    meta.setProperties(newProperties);

    if (StringUtils.isNotBlank(primaryKey)) {
      org.apache.amoro.api.PrimaryKeySpec keySpec = new org.apache.amoro.api.PrimaryKeySpec();
      List<String> fields =
          Arrays.stream(primaryKey.split(PRIMARY_KEY_COLUMN_JOIN_DELIMITER))
              .collect(Collectors.toList());
      keySpec.setFields(fields);
      meta.setKeySpec(keySpec);
    }
    meta.setFormat(this.getFormat().name());
    return meta;
  }

  private ServerTableIdentifier tableIdentifier;

  private String tableLocation;

  private String baseLocation;

  private String changeLocation;

  private String primaryKey;

  private String metaStoreSite;

  private String hdfsSite;

  private String coreSite;

  private String authMethod;

  private String hadoopUsername;

  private String krbKeytab;

  private String krbConf;

  private String krbPrincipal;

  private Map<String, String> properties;

  private long metaVersion;

  private volatile TableMetaStore metaStore;

  public TableFormat getFormat() {
    return this.tableIdentifier.getFormat();
  }

  public String getTableLocation() {
    return tableLocation;
  }

  public void setTableLocation(String tableLocation) {
    this.tableLocation = tableLocation;
  }

  public String getBaseLocation() {
    return baseLocation;
  }

  public void setBaseLocation(String baseLocation) {
    this.baseLocation = baseLocation;
  }

  public String getChangeLocation() {
    return changeLocation;
  }

  public void setChangeLocation(String changeLocation) {
    this.changeLocation = changeLocation;
  }

  public String getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(String primaryKey) {
    this.primaryKey = primaryKey;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public TableMetaStore getMetaStore() {
    if (metaStore == null) {
      synchronized (this) {
        if (metaStore == null) {
          this.metaStore =
              TableMetaStore.builder()
                  .withBase64MetaStoreSite(metaStoreSite)
                  .withBase64CoreSite(coreSite)
                  .withBase64HdfsSite(hdfsSite)
                  .withBase64Auth(authMethod, hadoopUsername, krbKeytab, krbConf, krbPrincipal)
                  .build();
        }
      }
    }

    return metaStore;
  }

  @VisibleForTesting
  public void setMetaStore(TableMetaStore metaStore) {
    synchronized (this) {
      this.metaStore = metaStore;
    }
  }

  public String getMetaStoreSite() {
    return metaStoreSite;
  }

  public void setMetaStoreSite(String metaStoreSite) {
    this.metaStoreSite = metaStoreSite;
  }

  public String getHdfsSite() {
    return hdfsSite;
  }

  public void setHdfsSite(String hdfsSite) {
    this.hdfsSite = hdfsSite;
  }

  public String getCoreSite() {
    return coreSite;
  }

  public void setCoreSite(String coreSite) {
    this.coreSite = coreSite;
  }

  public String getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  public String getHadoopUsername() {
    return hadoopUsername;
  }

  public void setHadoopUsername(String hadoopUsername) {
    this.hadoopUsername = hadoopUsername;
  }

  public String getKrbKeytab() {
    return krbKeytab;
  }

  public void setKrbKeytab(String krbKeytab) {
    this.krbKeytab = krbKeytab;
  }

  public String getKrbConf() {
    return krbConf;
  }

  public void setKrbConf(String krbConf) {
    this.krbConf = krbConf;
  }

  public String getKrbPrincipal() {
    return krbPrincipal;
  }

  public void setKrbPrincipal(String krbPrincipal) {
    this.krbPrincipal = krbPrincipal;
  }

  public long getMetaVersion() {
    return metaVersion;
  }

  public void setMetaVersion(long metaVersion) {
    this.metaVersion = metaVersion;
  }
}
