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
 *
 * Modified by Datazip Inc. in 2026
 */

package org.apache.amoro.aws;

import org.apache.iceberg.aws.AwsClientProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This is needed because the Iceberg Glue client does not read S3-specific credential properties
 * (s3.access-key-id / s3.secret-access-key). Without this provider, the Glue client falls back to
 * the default AWS credential chain, which may not have valid credentials.
 */
public class StaticAwsCredentialsProvider implements AwsCredentialsProvider {

  public static final String ACCESS_KEY_ID = "access-key-id";
  public static final String SECRET_ACCESS_KEY = "secret-access-key";

  private final AwsCredentials credentials;

  private StaticAwsCredentialsProvider(String accessKeyId, String secretAccessKey) {
    this.credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
  }

  public static AwsCredentialsProvider create(Map<String, String> properties) {
    String accessKeyId = properties.get(ACCESS_KEY_ID);
    String secretAccessKey = properties.get(SECRET_ACCESS_KEY);
    if (accessKeyId == null || secretAccessKey == null) {
      throw new IllegalArgumentException(
          "Both 'access-key-id' and 'secret-access-key' must be provided in "
              + "client.credentials-provider properties");
    }
    return new StaticAwsCredentialsProvider(accessKeyId, secretAccessKey);
  }

  public static Map<String, String> applyGlueCredentials(Map<String, String> properties) {
    if (properties == null) {
      return null;
    }
    String accessKey = properties.get(S3FileIOProperties.ACCESS_KEY_ID);
    String secretKey = properties.get(S3FileIOProperties.SECRET_ACCESS_KEY);
    if (accessKey == null || secretKey == null) {
      return properties;
    }
    Map<String, String> newProperties = new HashMap<>(properties);
    newProperties.put(
        AwsClientProperties.CLIENT_CREDENTIALS_PROVIDER,
        StaticAwsCredentialsProvider.class.getName());
    newProperties.put("client.credentials-provider." + ACCESS_KEY_ID, accessKey);
    newProperties.put("client.credentials-provider." + SECRET_ACCESS_KEY, secretKey);
    return newProperties;
  }

  @Override
  public AwsCredentials resolveCredentials() {
    return credentials;
  }
}
