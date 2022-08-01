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
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class ArcticTransactionService extends IJDBCService {

  Long expireTime = 24 * 60 * 60 * 1000L;

  public long tryAllocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature, Long txId) {
    SqlSession sqlSession = getSqlSession(true);
    TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);

    while (true) {
      try {
        mapper.insertTransaction(txId, transactionSignature, tableIdentifier);
        return txId;
      } catch (Exception e) {
        txId++;
      }

    }
  }

  public long allocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature, int retry) {
    SqlSession sqlSession = getSqlSession(false);
    try {
      com.netease.arctic.table.TableIdentifier identifier =
          new com.netease.arctic.table.TableIdentifier(tableIdentifier);
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      TableMetadata tableMetadata = tableMetadataMapper.loadTableMetaInLock(identifier);
      Preconditions.checkNotNull(tableMetadata, "lost table " + identifier);
      long currentTxId = tableMetadata.getCurrentTxId() == null ? 0 : tableMetadata.getCurrentTxId();
      long finalTxId = currentTxId + 1;
      if (!StringUtils.isEmpty(transactionSignature)) {
        TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);
        Long txId = mapper.getTxIdBySign(tableIdentifier, transactionSignature);
        if (txId != null) {
          sqlSession.commit(true);
          return txId;
        }
        try {
          mapper.insertTransaction(finalTxId, transactionSignature, tableIdentifier);
        } catch (Exception e) {
          finalTxId = tryAllocateTransactionId(tableIdentifier, transactionSignature, finalTxId + 1);
        }
      }

      tableMetadataMapper.updateTableTxId(identifier, finalTxId);
      sqlSession.commit();
      return finalTxId;
    } catch (PersistenceException e) {
      sqlSession.rollback();
      if (retry <= 0) {
        throw e;
      }
      return allocateTransactionId(tableIdentifier, transactionSignature, retry - 1);
    } finally {
      sqlSession.close();
    }
  }

  public void expire() {
    try (SqlSession sqlSession = getSqlSession(false)) {
      TableTransactionMetaMapper mapper = getMapper(sqlSession, TableTransactionMetaMapper.class);
      Long expireTime = System.currentTimeMillis() - this.expireTime;
      mapper.expire(expireTime);
    }
  }
}
