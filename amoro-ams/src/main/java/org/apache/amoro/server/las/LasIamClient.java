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

package org.apache.amoro.server.las;

import bytedance.olap.iam.Credential;
import bytedance.olap.iam.IAMService;
import bytedance.olap.iam.IamException;
import bytedance.olap.iam.ServiceInfo;
import bytedance.olap.iam.cache.AssumeRoleCredentialCache;
import bytedance.olap.iam.http.ClientConfiguration;
import bytedance.olap.iam.http.model.AssumeRoleResponse.Credentials;

import java.io.IOException;

/** IAM SDK owner that caches tenant role credentials and closes the underlying HTTP client. */
public final class LasIamClient implements AutoCloseable {

  private final IAMService iamService;
  private final AssumeRoleCredentialCache credentialCache;
  private final String roleSessionName;

  public LasIamClient(LasIntegrationContext context) {
    this(context, newIamService(context), context.bootstrapCredential());
  }

  LasIamClient(LasIntegrationContext context, IAMService iamService, Credential credential) {
    this.iamService = iamService;
    this.roleSessionName = context.iamRoleSessionName();
    this.credentialCache =
        new AssumeRoleCredentialCache(
            iamService,
            credential,
            context.iamAssumeRoleTtl().getSeconds(),
            context.iamCredentialCacheSize());
  }

  public Credentials assumeRole(String roleTrn) throws IamException {
    return credentialCache.get(roleTrn, roleSessionName);
  }

  @Override
  public void close() throws IOException {
    iamService.close();
  }

  private static IAMService newIamService(LasIntegrationContext context) {
    ClientConfiguration configuration = new ClientConfiguration();
    configuration.setConnectionTimeout(Math.toIntExact(context.connectTimeout().toMillis()));
    configuration.setSocketTimeout(Math.toIntExact(context.readTimeout().toMillis()));
    String endpointHost = context.iamEndpoint().getAuthority();
    ServiceInfo serviceInfo = new ServiceInfo(context.iamEndpoint().getScheme(), endpointHost);
    return new IAMService(serviceInfo, configuration, context.region());
  }
}
