package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.server.mapper.ApiTokensMapper;
import com.netease.arctic.ams.server.model.ApiTokens;
import com.netease.arctic.ams.server.service.IJDBCService;
import org.apache.ibatis.session.SqlSession;

public class ApiTokenService extends IJDBCService {

  public String getSecretByKey(String key) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      ApiTokensMapper  apiTokensMapper = getMapper(sqlSession, ApiTokensMapper.class);
      String secret = apiTokensMapper.getSecretBykey(key);
      if (secret != null) {
        return secret;
      }
    }

    return null;
  }

  public void insertApiToken(ApiTokens  apiToken) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      ApiTokensMapper  apiTokensMapper = getMapper(sqlSession, ApiTokensMapper.class);
      apiTokensMapper.insert(apiToken);
    }
  }
}
