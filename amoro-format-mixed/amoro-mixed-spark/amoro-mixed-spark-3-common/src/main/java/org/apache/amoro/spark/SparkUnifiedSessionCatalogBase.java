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
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.spark.sql.catalyst.analysis.NoSuchProcedureException;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.iceberg.catalog.Procedure;
import org.apache.spark.sql.connector.iceberg.catalog.ProcedureCatalog;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.Map;
import java.util.ServiceLoader;

public abstract class SparkUnifiedSessionCatalogBase<T extends TableCatalog & SupportsNamespaces>
    extends SessionCatalogBase<T> implements ProcedureCatalog {

  protected final Map<TableFormat, SparkTableFormat> tableFormats = Maps.newConcurrentMap();

  protected abstract SparkUnifiedCatalogBase createUnifiedCatalog(
      String name, CaseInsensitiveStringMap options);

  @Override
  protected TableCatalog buildTargetCatalog(String name, CaseInsensitiveStringMap options) {
    SparkUnifiedCatalogBase sparkUnifiedCatalog = createUnifiedCatalog(name, options);
    sparkUnifiedCatalog.initialize(name, options);
    ServiceLoader<SparkTableFormat> sparkTableFormats = ServiceLoader.load(SparkTableFormat.class);
    for (SparkTableFormat format : sparkTableFormats) {
      tableFormats.put(format.format(), format);
    }
    return sparkUnifiedCatalog;
  }

  @Override
  protected boolean isManagedTable(Table table) {
    return tableFormats.values().stream().anyMatch(f -> f.isFormatOf(table));
  }

  @Override
  protected boolean isManagedProvider(String provider) {
    if (provider == null) {
      return false;
    }
    try {
      TableFormat.valueOf(provider.toUpperCase());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  protected boolean isManagedSubTable(Identifier ident) {
    if (ident.namespace().length == 2) {
      for (SparkTableFormat sparkTableFormat : tableFormats.values()) {
        if (sparkTableFormat.isSubTableName(ident.name())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Procedure loadProcedure(Identifier ident) throws NoSuchProcedureException {
    SparkUnifiedCatalogBase catalog = (SparkUnifiedCatalogBase) getTargetCatalog();
    return catalog.loadProcedure(ident);
  }
}
