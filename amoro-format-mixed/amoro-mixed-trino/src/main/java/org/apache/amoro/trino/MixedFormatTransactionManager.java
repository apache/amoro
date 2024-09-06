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

import static java.util.Objects.requireNonNull;
import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkArgument;
import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkState;

import io.trino.spi.classloader.ThreadContextClassLoader;
import io.trino.spi.connector.ConnectorTransactionHandle;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** This is used to guarantee one transaction to one {@link MixedFormatConnectorMetadata} */
public class MixedFormatTransactionManager {
  private final MixedFormatMetadataFactory metadataFactory;
  private final ClassLoader classLoader;
  private final ConcurrentMap<ConnectorTransactionHandle, MemoizedMetadata> transactions =
      new ConcurrentHashMap<>();

  @Inject
  public MixedFormatTransactionManager(MixedFormatMetadataFactory metadataFactory) {
    this(metadataFactory, Thread.currentThread().getContextClassLoader());
  }

  public MixedFormatTransactionManager(
      MixedFormatMetadataFactory metadataFactory, ClassLoader classLoader) {
    this.metadataFactory = requireNonNull(metadataFactory, "metadataFactory is null");
    this.classLoader = requireNonNull(classLoader, "classLoader is null");
  }

  public void begin(ConnectorTransactionHandle transactionHandle) {
    MemoizedMetadata previousValue =
        transactions.putIfAbsent(transactionHandle, new MemoizedMetadata());
    checkState(previousValue == null);
  }

  public MixedFormatConnectorMetadata get(ConnectorTransactionHandle transactionHandle) {
    return transactions.get(transactionHandle).get();
  }

  public void commit(ConnectorTransactionHandle transaction) {
    MemoizedMetadata transactionalMetadata = transactions.remove(transaction);
    checkArgument(transactionalMetadata != null, "no such transaction: %s", transaction);
  }

  public void rollback(ConnectorTransactionHandle transaction) {
    MemoizedMetadata transactionalMetadata = transactions.remove(transaction);
    checkArgument(transactionalMetadata != null, "no such transaction: %s", transaction);
    transactionalMetadata
        .optionalGet()
        .ifPresent(
            metadata -> {
              try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(classLoader)) {
                metadata.rollback();
              }
            });
  }

  private class MemoizedMetadata {
    @GuardedBy("this")
    private MixedFormatConnectorMetadata metadata;

    public synchronized Optional<MixedFormatConnectorMetadata> optionalGet() {
      return Optional.ofNullable(metadata);
    }

    public synchronized MixedFormatConnectorMetadata get() {
      if (metadata == null) {
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(classLoader)) {
          metadata = metadataFactory.create();
        }
      }
      return metadata;
    }
  }
}
