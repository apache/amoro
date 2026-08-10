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

package org.apache.amoro.formats.lance;

import org.apache.hadoop.conf.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Converts Hadoop TOS configuration and credentials into Lance storage options. */
class LanceStorageOptionsProvider {

  static final String TOS_ENDPOINT = "fs.tos.endpoint";
  static final String TOS_REGION = "fs.tos.region";
  static final String VOLC_REGION = "fs.volc.openapi.region";
  static final String TOS_ACCESS_KEY = "fs.tos.access-key-id";
  static final String TOS_SECRET_KEY = "fs.tos.secret-access-key";
  static final String TOS_SESSION_TOKEN = "fs.tos.session-token";
  static final String TOS_CREDENTIAL_PROVIDER = "fs.tos.credentials.provider";
  static final String TOS_CREDENTIAL_TTL = "fs.tos.credential.sts.token.time-to-live";

  private static final long DEFAULT_CREDENTIAL_TTL_SECONDS = Duration.ofHours(1).getSeconds();
  private static final long MAX_ADVERTISED_LIFETIME_MILLIS = Duration.ofMinutes(15).toMillis();

  private final Configuration configuration;
  private final CredentialLoader credentialLoader;

  LanceStorageOptionsProvider(Configuration configuration) {
    this(configuration, new ProtonCredentialLoader(configuration));
  }

  LanceStorageOptionsProvider(Configuration configuration, CredentialLoader credentialLoader) {
    this.configuration = configuration;
    this.credentialLoader = credentialLoader;
  }

  Map<String, String> storageOptions(String location) {
    if (location == null || !"tos".equalsIgnoreCase(URI.create(location).getScheme())) {
      return Collections.emptyMap();
    }

    Map<String, String> options = new HashMap<>();
    putIfNotBlank(options, "endpoint", configuration.getTrimmed(TOS_ENDPOINT));
    putIfNotBlank(
        options,
        "region",
        firstNonBlank(configuration.getTrimmed(TOS_REGION), configuration.getTrimmed(VOLC_REGION)));

    TemporaryCredential credential = credential(location);
    if (credential != null) {
      putIfNotBlank(options, "access_key_id", credential.accessKeyId);
      putIfNotBlank(options, "secret_access_key", credential.secretAccessKey);
      putIfNotBlank(options, "security_token", credential.sessionToken);
      putRefreshOptions(options);
    }
    return options;
  }

  String datasetLocation(String location) {
    // The ByteDance Lance runtime has a native tos:// object-store implementation backed by
    // Proton. Keeping the HMS location intact selects that implementation; rewriting it to s3://
    // would bypass Proton and route requests through the generic S3 client.
    return location;
  }

  private TemporaryCredential credential(String location) {
    if (configuration.getTrimmed(TOS_CREDENTIAL_PROVIDER) != null) {
      return credentialLoader.load(bucket(location));
    }

    String accessKey = configuration.getTrimmed(TOS_ACCESS_KEY);
    String secretKey = configuration.getTrimmed(TOS_SECRET_KEY);
    if (accessKey == null || secretKey == null) {
      return null;
    }
    return new TemporaryCredential(
        accessKey, secretKey, configuration.getTrimmed(TOS_SESSION_TOKEN));
  }

  private void putRefreshOptions(Map<String, String> options) {
    long configuredTtlSeconds =
        configuration.getLong(TOS_CREDENTIAL_TTL, DEFAULT_CREDENTIAL_TTL_SECONDS);
    long advertisedLifetime =
        Math.max(
            1_000L,
            Math.min(
                Duration.ofSeconds(configuredTtlSeconds).toMillis() / 2,
                MAX_ADVERTISED_LIFETIME_MILLIS));
    long refreshOffset = Math.max(1_000L, Math.min(60_000L, advertisedLifetime / 5));
    options.put(
        "expires_at_millis", String.valueOf(System.currentTimeMillis() + advertisedLifetime));
    options.put("refresh_offset_millis", String.valueOf(refreshOffset));
  }

  private static String bucket(String location) {
    URI uri = URI.create(location);
    return uri.getHost() == null ? uri.getAuthority() : uri.getHost();
  }

  private static String firstNonBlank(String first, String second) {
    return first == null || first.isEmpty() ? second : first;
  }

  private static void putIfNotBlank(Map<String, String> options, String key, String value) {
    if (value != null && !value.isEmpty()) {
      options.put(key, value);
    }
  }

  interface CredentialLoader {
    TemporaryCredential load(String bucket);
  }

  static class TemporaryCredential {
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;

    TemporaryCredential(String accessKeyId, String secretAccessKey, String sessionToken) {
      this.accessKeyId = accessKeyId;
      this.secretAccessKey = secretAccessKey;
      this.sessionToken = sessionToken;
    }
  }

  /** Uses Proton through reflection so the community Lance module has no Proton dependency. */
  private static class ProtonCredentialLoader implements CredentialLoader {
    private static final String CONF_CLASS = "io.proton.common.conf.Conf";
    private static final String PROVIDER_FACTORY_CLASS =
        "io.proton.common.object.auth.ProviderFactory";

    private final Configuration configuration;
    private final ConcurrentMap<String, Object> providers = new ConcurrentHashMap<>();

    private ProtonCredentialLoader(Configuration configuration) {
      this.configuration = configuration;
    }

    @Override
    public TemporaryCredential load(String bucket) {
      try {
        Object provider = providers.computeIfAbsent(bucket, this::createProvider);
        Object credential = provider.getClass().getMethod("expirableCredential").invoke(provider);
        return new TemporaryCredential(
            invokeString(credential, "accessKeyId"),
            invokeString(credential, "accessKeySecret"),
            invokeString(credential, "sessionToken"));
      } catch (InvocationTargetException e) {
        throw credentialFailure(bucket, e.getCause());
      } catch (ReflectiveOperationException | RuntimeException e) {
        throw credentialFailure(bucket, e);
      }
    }

    private Object createProvider(String bucket) {
      try {
        Class<?> confClass = Class.forName(CONF_CLASS);
        Object protonConf =
            confClass.getMethod("copyOf", Iterable.class).invoke(null, configuration);
        Class<?> factoryClass = Class.forName(PROVIDER_FACTORY_CLASS);
        Method createProvider =
            factoryClass.getMethod("createProvider", confClass, String.class, String.class);
        return createProvider.invoke(null, protonConf, bucket, "tos");
      } catch (InvocationTargetException e) {
        throw credentialFailure(bucket, e.getCause());
      } catch (ReflectiveOperationException e) {
        throw credentialFailure(bucket, e);
      }
    }

    private static String invokeString(Object target, String method)
        throws ReflectiveOperationException {
      Object value = target.getClass().getMethod(method).invoke(target);
      return value == null ? null : value.toString();
    }

    private static IllegalStateException credentialFailure(String bucket, Throwable cause) {
      return new IllegalStateException(
          "Failed to obtain temporary TOS credentials for bucket " + bucket, cause);
    }
  }
}
