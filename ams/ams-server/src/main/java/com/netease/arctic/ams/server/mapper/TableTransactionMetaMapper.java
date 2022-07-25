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

package com.netease.arctic.ams.server.mapper;

import com.netease.arctic.ams.api.TableIdentifier;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TableTransactionMetaMapper {
  String TABLE_NAME = "table_transaction_meta";

  @Insert("insert into " + TABLE_NAME + "(transaction_id, signature, table_identifier) values(#{transactionId}," +
      "#{signature}, #{tableIdentifier, typeHandler=com.netease.arctic.ams.server.mybatis" +
      ".TableIdentifier2StringConverter})")
  void insertTransaction(
      @Param("transactionId") Long transactionId,
      @Param("signature") String signature,
      @Param("tableIdentifier") TableIdentifier tableIdentifier);

  @Select("select transaction_id from " + TABLE_NAME + " where table_identifier = #{tableIdentifier, " +
      "typeHandler=com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter} and signature = " +
      "#{sign}")
  Long getTxIdBySign(@Param("tableIdentifier") TableIdentifier tableIdentifier,
      @Param("sign") String sign);

  @Select("select max(transaction_id) from " + TABLE_NAME + " where table_identifier = #{tableIdentifier, " +
      "typeHandler=com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter}")
  Long getCurrentTxId(@Param("tableIdentifier") TableIdentifier tableIdentifier);

  @Delete("delete from " + TABLE_NAME + " where table_identifier = #{tableIdentifier, " +
      "typeHandler=com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter}")
  void deleteTableTx(@Param("tableIdentifier") TableIdentifier tableIdentifier);

  @Delete("delete from " + TABLE_NAME + " where table_identifier = #{tableIdentifier,typeHandler=com.netease.arctic" +
      ".ams.server.mybatis.TableIdentifier2StringConverter} and commit_time < #{expiredTime, typeHandler=com.netease" +
      ".arctic.ams.server.mybatis.Long2TsConvertor} and transaction_id < " +
      "( select id from (select max(transaction_id) as id from " + TABLE_NAME + " where table_identifier = " +
      "#{tableIdentifier, typeHandler=com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter}) t)")
  void expire(@Param("tableIdentifier") TableIdentifier tableIdentifier, @Param("expireTime") Long expireTime);
}
