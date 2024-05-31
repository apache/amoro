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

import org.apache.spark.sql.catalyst.analysis.NoSuchProcedureException;
import org.apache.spark.sql.connector.catalog.FunctionCatalog;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.iceberg.catalog.Procedure;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.ServiceLoader;

/** @Auth: hzwangtao6 @Time: 2024/5/24 14:04 @Description: */
public class SparkUnifiedSessionCatalog<
        T extends TableCatalog & SupportsNamespaces & FunctionCatalog>
    extends SparkUnifiedSessionCatalogBase<T> {

  @Override
  protected TableCatalog buildTargetCatalog(String name, CaseInsensitiveStringMap options) {
    SparkUnifiedCatalog sparkUnifiedCatalog = new SparkUnifiedCatalog();
    sparkUnifiedCatalog.initialize(name, options);
    ServiceLoader<SparkTableFormat> sparkTableFormats = ServiceLoader.load(SparkTableFormat.class);
    for (SparkTableFormat format : sparkTableFormats) {
      tableFormats.put(format.format(), format);
    }
    return sparkUnifiedCatalog;
  }

  @Override
  public Procedure loadProcedure(Identifier ident) throws NoSuchProcedureException {
    SparkUnifiedCatalog catalog = (SparkUnifiedCatalog) getTargetCatalog();
    return catalog.loadProcedure(ident);
  }
}
