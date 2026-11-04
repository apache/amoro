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

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.Preconditions;
import org.lance.namespace.LanceNamespace;

import java.util.HashMap;
import java.util.Map;

/**
 * REST catalog implementation for Lance.
 *
 * <p>This catalog connects to a Lance Namespace REST service via {@code
 * LanceNamespace.connect("rest", ...)} and maps the REST namespace hierarchy to Amoro's
 * database/table model.
 */
public class LanceRestCatalog extends AbstractLanceCatalog {

  public LanceRestCatalog(String catalogName, Map<String, String> catalogProperties) {
    super(
        catalogName,
        buildNamespace(catalogProperties),
        // The REST service may or may not expose a catalog level.
        catalogPrefix(catalogProperties, null));
  }

  private static LanceNamespace buildNamespace(Map<String, String> namespaceProperties) {
    validateProperties(namespaceProperties);
    Preconditions.checkArgument(
        namespaceProperties.containsKey(URI_PROPERTY),
        "URI must be set in namespaceProperties for REST metastore type.");
    Map<String, String> restProperties = new HashMap<>(namespaceProperties);
    return LanceNamespace.connect("rest", restProperties, new RootAllocator(Long.MAX_VALUE));
  }
}
