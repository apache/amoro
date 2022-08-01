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

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.server.mapper.CatalogMetadataMapper;
import com.netease.arctic.ams.server.service.IJDBCService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class CatalogMetadataService extends IJDBCService {

  public List<CatalogMeta> getCatalogs() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      CatalogMetadataMapper catalogMetadataMapper =
          getMapper(sqlSession, CatalogMetadataMapper.class);
      return catalogMetadataMapper.getCatalogs();
    }
  }

  public CatalogMeta getCatalog(String catalogName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      CatalogMetadataMapper catalogMetadataMapper =
          getMapper(sqlSession, CatalogMetadataMapper.class);
      List<CatalogMeta> tmpMetadataList = catalogMetadataMapper.getCatalog(catalogName);
      if (CollectionUtils.isNotEmpty(tmpMetadataList)) {
        return tmpMetadataList.get(0);
      } else {
        return new CatalogMeta();
      }
    }
  }

  public void addCatalog(List<CatalogMeta> catalogMeta) {
    try (SqlSession sqlSession = getSqlSession(true)) {

      CatalogMetadataMapper catalogMetadataMapper =
              getMapper(sqlSession, CatalogMetadataMapper.class);
      for (CatalogMeta c : catalogMeta) {
        if (getCatalog(c.catalogName) != null) {
          catalogMetadataMapper.insertCatalog(c);
        }
      }
    }
  }
}
