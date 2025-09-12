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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.dashboard.APITokenManager;
import org.apache.amoro.server.dashboard.model.ApiTokens;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.utils.ParamSignatureCalculator;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The controller that handles api token requests. */
public class ApiTokenController {
  private static final Logger LOG = LoggerFactory.getLogger(ApiTokenController.class);
  private final APITokenManager apiTokenManager;
  private static final String MASK_STRING = "******";

  public ApiTokenController(APITokenManager apiTokenManager) {
    this.apiTokenManager = apiTokenManager;
  }

  /** Get all api tokens. As secret shown once when created, the returned secret is masked. */
  public void getApiTokens(Context ctx) {
    List<ApiTokens> apiTokens = apiTokenManager.getApiTokens();
    // Mask the secret field for each ApiTokens object
    apiTokens.forEach(apiToken -> apiToken.setSecret(MASK_STRING));
    ctx.json(OkResponse.of(apiTokens));
  }

  /** Create an api token. */
  public void createApiToken(Context ctx) {
    ApiTokens apiTokens = createApiToken();
    apiTokenManager.insertApiToken(apiTokens);
    LOG.info(
        "Create apiKey:{}, secret: {}, apply time: {}",
        apiTokens.getApikey(),
        MASK_STRING,
        apiTokens.getApplyTime());
    ctx.json(OkResponse.of(apiTokens));
  }

  /** Delete the api token for given api key. */
  public void deleteApiToken(Context ctx) {
    String apiKey = ctx.queryParam("apiKey");
    apiTokenManager.deleteApiTokenByKey(apiKey);
    LOG.info("Delete apiKey:{}", apiKey);
    ctx.json(OkResponse.of("Success to delete apiKey"));
  }

  /** Check if the given apiKey exists. */
  private boolean checkApiKeyExists(String apiKey) {
    String secret = apiTokenManager.getSecretByKey(apiKey);
    return apiKey != null && secret != null;
  }

  /**
   * Calculate signature for request. Used only for clients to verify their signature calculation.
   */
  public void calculateSignature(Context ctx) {
    String apiKey = ctx.queryParam("apiKey");
    String secret = ctx.queryParam("secret");
    Map<String, List<String>> params = ctx.queryParamMap();
    params.remove("secret");

    Preconditions.checkArgument(checkApiKeyExists(apiKey), "API key does not exist");
    String signature = generateSignature(apiKey, params, secret);
    ctx.json(OkResponse.of(signature));
  }

  /** Get encrypt string for request query params. Used for client to calculate signature. */
  public void getEncryptStringFromQueryParam(Context ctx) {
    String apiKey = ctx.queryParam("apiKey");
    Map<String, List<String>> params = ctx.queryParamMap();

    Preconditions.checkArgument(checkApiKeyExists(apiKey), "API key does not exist");
    String encryptString = generateEncryptString(params);
    ctx.json(OkResponse.of(encryptString));
  }

  /** check api token for requestUrl. */
  public void checkApiToken(Context ctx) {
    String requestUrl = ctx.url();
    String apiKey = ctx.queryParam("apiKey");
    String signature = ctx.queryParam("signature");
    Map<String, List<String>> params = ctx.queryParamMap();

    try {
      if (apiKey == null || signature == null) {
        throw new SignatureCheckException("API key or signature is missing");
      }

      String secret = apiTokenManager.getSecretByKey(apiKey);
      String signCal = generateSignature(apiKey, params, secret);
      LOG.debug(
          "Calculated signature for url:{}, apiKey:{}, calculated signature:{}, signature in request: {}",
          requestUrl,
          apiKey,
          signCal,
          signature);

      if (!signature.equals(signCal)) {
        throw new SignatureCheckException(
            String.format(
                "Check signature for url:%s failed,"
                    + " calculated signature:%s, signature in request:%s",
                requestUrl, signCal, signature));
      }
    } catch (Exception e) {
      throw new SignatureCheckException("Check url signature failed", e);
    }
  }

  private ApiTokens createApiToken() {
    String apiKey = UUID.randomUUID().toString().replace("-", "");
    String secret = UUID.randomUUID().toString().replace("-", "");
    return new ApiTokens(apiKey, secret);
  }

  private String generateSignature(String apiKey, Map<String, List<String>> params, String secret) {
    Preconditions.checkArgument(apiKey != null && secret != null, "Invalid API key");

    String encryptString = generateEncryptString(params);
    String plainText = String.format("%s%s%s", apiKey, encryptString, secret);
    String signCal = ParamSignatureCalculator.getMD5(plainText);
    LOG.debug(
        "Calculated signature for plain text:{}, calculated signature:{}", plainText, signCal);
    return signCal;
  }

  private String generateEncryptString(Map<String, List<String>> params) {
    params.remove("apiKey");
    params.remove("signature");
    String paramString = ParamSignatureCalculator.generateParamStringWithValueList(params);

    if (StringUtils.isBlank(paramString)) {
      return ParamSignatureCalculator.SIMPLE_DATE_FORMAT.format(new Date());
    } else {
      return paramString;
    }
  }
}
