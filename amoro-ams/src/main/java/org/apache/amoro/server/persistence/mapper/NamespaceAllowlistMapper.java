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

package org.apache.amoro.server.persistence.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Persistence mapper for namespaces that are allowed to synchronize external catalogs. */
public interface NamespaceAllowlistMapper {
  String TABLE_NAME = "namespace_allowlist";

  @Select("SELECT namespace FROM " + TABLE_NAME + " ORDER BY namespace")
  List<String> listNamespaces();

  @Select("SELECT namespace FROM " + TABLE_NAME + " WHERE namespace = #{namespace}")
  String getNamespace(@Param("namespace") String namespace);

  @Insert("INSERT INTO " + TABLE_NAME + " (namespace) VALUES (#{namespace})")
  void insertNamespace(@Param("namespace") String namespace);

  @Delete("DELETE FROM " + TABLE_NAME + " WHERE namespace = #{namespace}")
  int deleteNamespace(@Param("namespace") String namespace);
}
