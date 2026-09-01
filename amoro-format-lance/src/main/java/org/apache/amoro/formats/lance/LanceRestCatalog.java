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

import org.apache.amoro.AlreadyExistsException;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.Preconditions;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.NamespaceAlreadyExistsException;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST catalog implementation for Lance.
 *
 * <p>This catalog connects to a Lance Namespace REST service via {@code
 * LanceNamespace.connect("rest", ...)} and maps the REST namespace hierarchy to Amoro's
 * database/table model.
 */
public class LanceRestCatalog extends AbstractLanceCatalog {

  private static final String URI_PROPERTY = "uri";

  /**
   * Optional catalog name prepended to the REST namespace id, mapping Amoro's database/table onto
   * the service's schema/table levels (catalog/schema/table hierarchy). Empty when the service has
   * no catalog level.
   */
  private static final String CATALOG_PROPERTY = "catalog-name";

  private final List<String> catalogPrefix;

  public LanceRestCatalog(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, buildNamespace(catalogProperties));
    this.catalogPrefix = catalogPrefix(catalogProperties);
  }

  private static List<String> catalogPrefix(Map<String, String> catalogProperties) {
    String catalog = catalogProperties.get(CATALOG_PROPERTY);
    return catalog == null || catalog.isEmpty()
        ? Collections.emptyList()
        : Collections.singletonList(catalog);
  }

  private static LanceNamespace buildNamespace(Map<String, String> namespaceProperties) {
    Preconditions.checkArgument(
        namespaceProperties != null && !namespaceProperties.isEmpty(),
        "Catalog properties must be set.");
    Preconditions.checkArgument(
        namespaceProperties.containsKey(URI_PROPERTY),
        "URI must be set in namespaceProperties for REST metastore type.");
    Map<String, String> restProperties = new HashMap<>(namespaceProperties);
    return LanceNamespace.connect("rest", restProperties, new RootAllocator(Long.MAX_VALUE));
  }

  @Override
  protected List<String> tableId(String database, String table) {
    return concat(catalogPrefix, database, table);
  }

  @Override
  protected List<String> tableIdForListTables(String database) {
    return concat(catalogPrefix, database);
  }

  private static List<String> concat(List<String> prefix, String... parts) {
    List<String> id = new ArrayList<>(prefix);
    Collections.addAll(id, parts);
    return id;
  }

  @Override
  public List<String> listDatabases() {
    ListNamespacesRequest request = new ListNamespacesRequest().id(catalogPrefix);
    ListNamespacesResponse response = namespace.listNamespaces(request);
    if (response == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(response.getNamespaces());
  }

  @Override
  public boolean databaseExists(String database) {
    try {
      NamespaceExistsRequest request =
          new NamespaceExistsRequest().id(concat(catalogPrefix, database));
      namespace.namespaceExists(request);
      return true;
    } catch (NamespaceNotFoundException e) {
      return false;
    }
  }

  @Override
  public void createDatabase(String database) {
    try {
      namespace.createNamespace(new CreateNamespaceRequest().id(concat(catalogPrefix, database)));
    } catch (NamespaceAlreadyExistsException e) {
      throw new AlreadyExistsException("Database: " + database + " already exists", e);
    }
  }

  @Override
  public void dropDatabase(String database) {
    validateDatabase(database);
    namespace.dropNamespace(new DropNamespaceRequest().id(concat(catalogPrefix, database)));
  }
}
