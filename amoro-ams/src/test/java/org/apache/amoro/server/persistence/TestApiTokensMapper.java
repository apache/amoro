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

package org.apache.amoro.server.persistence;

import org.apache.amoro.server.AMSManagerTestBase;
import org.apache.amoro.server.dashboard.model.ApiTokens;
import org.apache.amoro.server.persistence.mapper.ApiTokensMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestApiTokensMapper extends AMSManagerTestBase {

  @Test
  public void getApiTokensLoadsSecretColumn() {
    // The @Results mapping declares the secret column; the SELECT list must fetch it,
    // otherwise the token list endpoint fails for every request.
    ApiTokens token = new ApiTokens("mapper-test-apikey", "mapper-test-secret");
    token.setApplyTime(System.currentTimeMillis());
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(ApiTokensMapper.class).insert(token);
    }

    List<ApiTokens> tokens;
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      tokens = session.getMapper(ApiTokensMapper.class).getApiTokens();
    }

    Assertions.assertEquals(
        "mapper-test-secret",
        tokens.stream()
            .filter(t -> "mapper-test-apikey".equals(t.getApikey()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("inserted token not found"))
            .getSecret());
  }
}
