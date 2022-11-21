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

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.mapper.TableMetadataMapper;
import com.netease.arctic.ams.server.mapper.TableTransactionMetaMapper;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.IJDBCService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ArcticTransactionService extends IJDBCService {

  private static final ConcurrentHashMap<String, ReentrantLock> TABLE_LOCK_MAP = new ConcurrentHashMap<>();

  public long allocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature) {
    ReentrantLock lock = TABLE_LOCK_MAP.putIfAbsent(tableIdentifier.toString(), new ReentrantLock());
    if (lock == null) {
      lock = TABLE_LOCK_MAP.get(tableIdentifier.toString());
    }
    try {
      if (!lock.tryLock(5, TimeUnit.SECONDS)) {
        throw new RuntimeException("get lock timeout");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("get lock failed", e);
    }
    try (SqlSession sqlSession = getSqlSession(false)) {
      try {
        com.netease.arctic.table.TableIdentifier identifier =
            new com.netease.arctic.table.TableIdentifier(tableIdentifier);
        TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
        TableMetadata tableMetadata = tableMetadataMapper.loadTableMeta(identifier);
        TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);
        Preconditions.checkNotNull(tableMetadata, "lost table " + identifier);
        Long currentTxId = tableMetadata.getCurrentTxId();
        Long finalTxId = currentTxId + 1;
        if (!StringUtils.isEmpty(transactionSignature)) {
          Long txId = mapper.getTxIdBySign(tableIdentifier, transactionSignature);
          if (txId != null) {
            sqlSession.commit(true);
            return txId;
          }
          mapper.insertTransaction(finalTxId, transactionSignature, tableIdentifier);
        }

        tableMetadataMapper.updateTableTxId(identifier, finalTxId);
        sqlSession.commit();
        return finalTxId;
      } catch (Exception e) {
        sqlSession.rollback();
      }
    } finally {
      lock.unlock();
    }

    throw new RuntimeException(String.format("table %s allocateTransactionId error", tableIdentifier));
  }

  public void delete(TableIdentifier tableIdentifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);
      mapper.deleteTableTx(tableIdentifier);
    }
  }

  public void expire(TableIdentifier identifier, long time) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);
      mapper.expire(identifier, time);
    }
  }
}
