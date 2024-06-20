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

package org.apache.amoro.trino;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorPageSource;
import io.trino.spi.connector.ConnectorPageSourceProvider;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.DynamicFilter;
import org.apache.amoro.trino.keyed.KeyedPageSourceProvider;
import org.apache.amoro.trino.keyed.KeyedTableHandle;
import org.apache.amoro.trino.unkeyed.IcebergPageSourceProvider;

import javax.inject.Inject;

import java.util.List;

/**
 * {@link MixedFormatPageSourceProvider} is a Union {@link ConnectorPageSourceProvider} contain
 * {@link KeyedPageSourceProvider} and {@link IcebergPageSourceProvider}. This is final {@link
 * ConnectorPageSourceProvider} provided to Trino
 */
public class MixedFormatPageSourceProvider implements ConnectorPageSourceProvider {

  private final KeyedPageSourceProvider keyedPageSourceProvider;

  private final IcebergPageSourceProvider icebergPageSourceProvider;

  @Inject
  public MixedFormatPageSourceProvider(
      KeyedPageSourceProvider keyedPageSourceProvider,
      IcebergPageSourceProvider icebergPageSourceProvider) {
    this.keyedPageSourceProvider = keyedPageSourceProvider;
    this.icebergPageSourceProvider = icebergPageSourceProvider;
  }

  @Override
  public ConnectorPageSource createPageSource(
      ConnectorTransactionHandle transaction,
      ConnectorSession session,
      ConnectorSplit split,
      ConnectorTableHandle table,
      List<ColumnHandle> columns,
      DynamicFilter dynamicFilter) {
    if (table instanceof KeyedTableHandle) {
      return keyedPageSourceProvider.createPageSource(
          transaction, session, split, table, columns, dynamicFilter);
    } else {
      return icebergPageSourceProvider.createPageSource(
          transaction, session, split, table, columns, dynamicFilter);
    }
  }
}
