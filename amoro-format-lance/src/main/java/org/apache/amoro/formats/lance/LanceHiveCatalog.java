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

package org.apache.amoro.formats.lance;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.aliyun.AliyunProperties;
import org.apache.iceberg.aws.AwsClientProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.lance.Dataset;
import org.lance.ReadOptions;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.hive3.Hive3Namespace;
import org.lance.namespace.hive3.Hive3NamespaceConfig;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.DropTableRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Lance catalog backed by a Hive metastore; metadata is delegated to {@link Hive3Namespace}. */
public class LanceHiveCatalog extends AbstractLanceCatalog {

  private static final String HIVE_METASTORE_URIS = "hive.metastore.uris";

  /** Escape hatch for storage options Amoro does not model, e.g. MinIO's aws_allow_http. */
  private static final String STORAGE_OPTIONS_PREFIX = "lance.storage-options.";

  private final Map<String, String> catalogProperties;

  public LanceHiveCatalog(
      String catalogName, Map<String, String> catalogProperties, TableMetaStore metaStore) {
    super(
        catalogName,
        buildNamespace(catalogProperties, metaStore),
        catalogPrefix(catalogProperties, Hive3NamespaceConfig.CATALOG_NAME));
    this.catalogProperties = new HashMap<>(catalogProperties);
  }

  @Override
  protected AmoroTable<?> openTable(String database, String table) {
    DescribeTableResponse response =
        namespace.describeTable(new DescribeTableRequest().id(tableId(database, table)));
    return new LanceTable(
        TableIdentifier.of(catalogName, database, table),
        openDataset(response.getLocation()),
        response.getProperties());
  }

  /** Purge deletes the table data; otherwise only the metastore entry is deregistered. */
  @Override
  protected void dropInNamespace(String database, String table, boolean purge) {
    if (purge) {
      namespace.dropTable(new DropTableRequest().id(tableId(database, table)));
    } else {
      namespace.deregisterTable(new DeregisterTableRequest().id(tableId(database, table)));
    }
  }

  private Dataset openDataset(String location) {
    ReadOptions readOptions =
        new ReadOptions.Builder()
            .setStorageOptions(storageOptions(catalogProperties, location))
            .build();
    return Dataset.open().uri(location).readOptions(readOptions).build();
  }

  private static LanceNamespace buildNamespace(
      Map<String, String> catalogProperties, TableMetaStore metaStore) {
    validateProperties(catalogProperties);

    String root = catalogProperties.get(CatalogMetaProperties.KEY_WAREHOUSE);
    Preconditions.checkArgument(
        StringUtils.isNotBlank(root), "Warehouse must be set in catalogProperties.");
    Preconditions.checkArgument(
        !"hdfs".equals(scheme(root.trim())), "Unsupported warehouse scheme: " + root);

    String uri = catalogProperties.get(URI_PROPERTY);
    Preconditions.checkArgument(
        StringUtils.isNotBlank(uri), "Hive metastore uri must be set, e.g. thrift://host:9083.");

    Configuration conf = new Configuration(metaStore.getConfiguration());
    conf.set(HIVE_METASTORE_URIS, uri.trim());

    Map<String, String> namespaceProperties = new HashMap<>();
    namespaceProperties.put(Hive3NamespaceConfig.ROOT, root.trim());

    Hive3Namespace namespace = new Hive3Namespace();
    namespace.setHadoopConf(conf);
    namespace.initialize(namespaceProperties, new RootAllocator(Long.MAX_VALUE));
    return namespace;
  }

  /** Maps the catalog credentials onto Lance storage_options of the location's scheme. */
  static Map<String, String> storageOptions(
      Map<String, String> catalogProperties, String location) {
    Map<String, String> options = new HashMap<>();
    String scheme = scheme(location);
    if ("s3".equals(scheme)) {
      CatalogUtil.copyProperty(
          catalogProperties, options, S3FileIOProperties.ACCESS_KEY_ID, "aws_access_key_id");
      CatalogUtil.copyProperty(
          catalogProperties,
          options,
          S3FileIOProperties.SECRET_ACCESS_KEY,
          "aws_secret_access_key");
      CatalogUtil.copyProperty(
          catalogProperties, options, S3FileIOProperties.ENDPOINT, "aws_endpoint");
      CatalogUtil.copyProperty(
          catalogProperties, options, AwsClientProperties.CLIENT_REGION, "aws_region");
    } else if ("oss".equals(scheme)) {
      CatalogUtil.copyProperty(
          catalogProperties, options, AliyunProperties.CLIENT_ACCESS_KEY_ID, "access_key_id");
      CatalogUtil.copyProperty(
          catalogProperties,
          options,
          AliyunProperties.CLIENT_ACCESS_KEY_SECRET,
          "access_key_secret");
      CatalogUtil.copyProperty(
          catalogProperties, options, AliyunProperties.OSS_ENDPOINT, "endpoint");
    }

    // lance.storage-options.* overrides win over the translated defaults.
    for (Map.Entry<String, String> entry : catalogProperties.entrySet()) {
      if (entry.getKey().startsWith(STORAGE_OPTIONS_PREFIX)) {
        options.put(entry.getKey().substring(STORAGE_OPTIONS_PREFIX.length()), entry.getValue());
      }
    }
    return options;
  }

  private static String scheme(String uri) {
    return StringUtils.substringBefore(uri, "://").toLowerCase(Locale.ROOT);
  }
}
