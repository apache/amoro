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

import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplitManager;
import io.trino.spi.connector.ConnectorSplitSource;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.DynamicFilter;
import org.apache.amoro.trino.keyed.KeyedConnectorSplitManager;
import org.apache.amoro.trino.keyed.KeyedTableHandle;
import org.apache.amoro.trino.unkeyed.IcebergSplitManager;

import javax.inject.Inject;

/**
 * {@link MixedFormatConnectorSplitManager} is a Union {@link ConnectorSplitManager} contain {@link
 * KeyedConnectorSplitManager} and {@link IcebergSplitManager}. This is final {@link
 * ConnectorSplitManager} provided to Trino
 */
public class MixedFormatConnectorSplitManager implements ConnectorSplitManager {

  private final KeyedConnectorSplitManager keyedConnectorSplitManager;

  private final IcebergSplitManager icebergSplitManager;

  @Inject
  public MixedFormatConnectorSplitManager(
      KeyedConnectorSplitManager keyedConnectorSplitManager,
      IcebergSplitManager icebergSplitManager) {
    this.keyedConnectorSplitManager = keyedConnectorSplitManager;
    this.icebergSplitManager = icebergSplitManager;
  }

  @Override
  public ConnectorSplitSource getSplits(
      ConnectorTransactionHandle transaction,
      ConnectorSession session,
      ConnectorTableHandle table,
      DynamicFilter dynamicFilter,
      Constraint constraint) {
    if (table instanceof KeyedTableHandle) {
      return keyedConnectorSplitManager.getSplits(
          transaction, session, table, dynamicFilter, constraint);
    } else {
      return icebergSplitManager.getSplits(transaction, session, table, dynamicFilter, constraint);
    }
  }
}
