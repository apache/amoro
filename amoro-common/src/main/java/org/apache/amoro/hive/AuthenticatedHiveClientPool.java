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

package org.apache.amoro.hive;

import org.apache.amoro.client.ClientPoolImpl;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.DynConstructors;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended implementation of {@link ClientPoolImpl} with {@link TableMetaStore} to support
 * authenticated hive cluster.
 */
public class AuthenticatedHiveClientPool extends ClientPoolImpl<HMSClient, TException> {
  private final TableMetaStore metaStore;

  private final HiveConf hiveConf;
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatedHiveClientPool.class);

  private static final DynConstructors.Ctor<HiveMetaStoreClient> CLIENT_CTOR =
      DynConstructors.builder()
          .impl(HiveMetaStoreClient.class, HiveConf.class)
          .impl(HiveMetaStoreClient.class, Configuration.class)
          .build();

  @VisibleForTesting
  public static HMSClient createHiveMetaStoreClient(HiveConf hiveConf) {
    try {
      try {
        HiveMetaStoreClient client = CLIENT_CTOR.newInstance(hiveConf);
        return new HMSClientImpl(client);
      } catch (RuntimeException e) {
        // any MetaException would be wrapped into RuntimeException during reflection, so
        // let's double-check type
        // here
        if (e.getCause() instanceof MetaException) {
          throw (MetaException) e.getCause();
        }
        throw e;
      }
    } catch (MetaException e) {
      throw new RuntimeException("Failed to connect to Hive Metastore", e);
    } catch (Throwable t) {
      LOG.error("Failed to call createHiveMetaStoreClient", t);
      if (t.getMessage().contains("Another instance of Derby may have already booted")) {
        throw new RuntimeException(
            "Failed to start an embedded metastore because embedded "
                + "Derby supports only one client at a time. To fix this, use a metastore that supports "
                + "multiple clients.",
            t);
      }

      throw new RuntimeException("Failed to connect to Hive Metastore", t);
    }
  }

  public AuthenticatedHiveClientPool(TableMetaStore tableMetaStore, int poolSize) {
    super(poolSize, TTransportException.class, true);
    this.hiveConf =
        new HiveConf(tableMetaStore.getConfiguration(), AuthenticatedHiveClientPool.class);
    this.hiveConf.addResource(tableMetaStore.getConfiguration());
    this.hiveConf.addResource(tableMetaStore.getHiveSiteLocation().orElse(null));
    this.metaStore = tableMetaStore;
  }

  @Override
  protected HMSClient newClient() {
    return metaStore.doAs(() -> createHiveMetaStoreClient(hiveConf));
  }

  @Override
  protected HMSClient reconnect(HMSClient client) {
    try {
      return metaStore.doAs(
          () -> {
            try {
              client.close();
              client.reconnect();
            } catch (MetaException e) {
              throw new RuntimeException("Failed to reconnect to Hive Metastore", e);
            }
            return client;
          });
    } catch (Exception e) {
      LOG.error("hive metastore client reconnected failed", e);
      throw e;
    }
  }

  @Override
  protected boolean isConnectionException(Exception e) {
    return super.isConnectionException(e)
        || (e != null
            && e instanceof MetaException
            && e.getMessage()
                .contains("Got exception: org.apache.thrift.transport.TTransportException"));
  }

  @Override
  protected void close(HMSClient client) {
    client.close();
  }
}
