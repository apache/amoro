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

package org.apache.amoro.spark;

import org.apache.amoro.TableFormat;
import org.apache.spark.sql.catalyst.analysis.NoSuchFunctionException;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.connector.catalog.FunctionCatalog;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.functions.UnboundFunction;
import org.apache.spark.sql.connector.iceberg.catalog.ProcedureCatalog;

public class SparkUnifiedCatalog extends SparkUnifiedCatalogBase
    implements TableCatalog, SupportsNamespaces, ProcedureCatalog, FunctionCatalog {
  /**
   * List the functions in a namespace from the catalog.
   *
   * <p>If there are no functions in the namespace, implementations should return an empty array.
   *
   * @param namespace a multi-part namespace
   * @return an array of Identifiers for functions
   * @throws NoSuchNamespaceException If the namespace does not exist (optional).
   */
  @Override
  public Identifier[] listFunctions(String[] namespace) throws NoSuchNamespaceException {
    TableCatalog tableCatalog = tableCatalog(TableFormat.ICEBERG);
    if (tableCatalog instanceof FunctionCatalog) {
      return ((FunctionCatalog) tableCatalog).listFunctions(namespace);
    }
    throw new NoSuchNamespaceException(namespace);
  }

  /**
   * Load a function by {@link Identifier identifier} from the catalog.
   *
   * @param ident a function identifier
   * @return an unbound function instance
   * @throws NoSuchFunctionException If the function doesn't exist
   */
  @Override
  public UnboundFunction loadFunction(Identifier ident) throws NoSuchFunctionException {

    TableCatalog tableCatalog = tableCatalog(TableFormat.ICEBERG);
    if (tableCatalog instanceof FunctionCatalog) {
      return ((FunctionCatalog) tableCatalog).loadFunction(ident);
    }
    throw new NoSuchFunctionException(ident);
  }
}
