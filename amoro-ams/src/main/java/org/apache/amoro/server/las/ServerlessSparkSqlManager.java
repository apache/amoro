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

import bytedance.olap.iam.IamException;
import bytedance.olap.iam.http.model.AssumeRoleResponse.Credentials;
import com.volcengine.emr.serverless.Job;
import com.volcengine.emr.serverless.SQLTask;
import com.volcengine.emr.serverless.ServerlessClientOption;
import com.volcengine.emr.serverless.ServerlessQueryClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/** Submits asynchronous Spark SQL jobs to a tenant queue with tenant-scoped credentials. */
public final class ServerlessSparkSqlManager {

  private static final String SPARK_HADOOP_PREFIX = "spark.hadoop.";

  private final LasIntegrationContext context;
  private final LasIamClient iamClient;
  private final ServerlessClientFactory clientFactory;

  public ServerlessSparkSqlManager(LasIntegrationContext context, LasIamClient iamClient) {
    this(context, iamClient, credentials -> newServerlessClient(context, credentials));
  }

  ServerlessSparkSqlManager(
      LasIntegrationContext context,
      LasIamClient iamClient,
      ServerlessClientFactory clientFactory) {
    this.context = context;
    this.iamClient = iamClient;
    this.clientFactory = clientFactory;
  }

  public String submit(
      LasTenantContext tenant,
      String taskName,
      String sparkSql,
      Map<String, String> customSparkConf)
      throws IamException {
    Credentials submitCredentials = iamClient.assumeRole(tenant.submitRoleTrn());
    Credentials dataCredentials = iamClient.assumeRole(tenant.dataRoleTrn());
    SQLTask task = buildTask(tenant, taskName, sparkSql, customSparkConf, dataCredentials);
    return clientFactory.create(submitCredentials).executeSQL(task).getId();
  }

  public Job getJob(LasTenantContext tenant, String jobId) throws IamException {
    return submissionClient(tenant).getJob(required("jobId", jobId));
  }

  public void cancelJob(LasTenantContext tenant, String jobId) throws IamException {
    submissionClient(tenant).cancelJob(required("jobId", jobId));
  }

  SQLTask buildTask(
      LasTenantContext tenant,
      String taskName,
      String sparkSql,
      Map<String, String> customSparkConf,
      Credentials dataCredentials) {
    required("taskName", taskName);
    required("sparkSql", sparkSql);

    Map<String, String> managedConf = managedSparkConf(tenant, dataCredentials);
    Map<String, String> taskConf = new LinkedHashMap<>();
    if (customSparkConf != null) {
      customSparkConf.forEach(
          (key, value) -> {
            if (managedConf.containsKey(key)) {
              throw new IllegalArgumentException("Custom Spark conf cannot override " + key);
            }
            taskConf.put(key, value);
          });
    }
    taskConf.putAll(managedConf);

    return SQLTask.builder(sparkSql)
        .name(taskName)
        .queue(tenant.queueName())
        .addConf(taskConf)
        .sync(false)
        .build();
  }

  private ServerlessQueryClient submissionClient(LasTenantContext tenant) throws IamException {
    return clientFactory.create(iamClient.assumeRole(tenant.submitRoleTrn()));
  }

  private Map<String, String> managedSparkConf(
      LasTenantContext tenant, Credentials dataCredentials) {
    Map<String, String> conf = new LinkedHashMap<>();
    conf.put("spark.hadoop.hive.metastore.uris", context.hmsUri().toString());
    conf.put("spark.hadoop.metastore.catalog.default", tenant.catalogName());
    conf.put("spark.hive.metastore.catalog.default", tenant.catalogName());

    Configuration tos = context.newTosConfiguration(dataCredentials);
    tos.forEach(entry -> conf.put(SPARK_HADOOP_PREFIX + entry.getKey(), entry.getValue()));

    if (context.crossVpcEnabled()) {
      conf.put("serverless.cross.vpc.access.enabled", "true");
      conf.put("serverless.cross.vpc.accountId", context.crossVpcAccountId());
      conf.put("serverless.cross.vpc.vpc.id", context.crossVpcVpcId());
      conf.put("serverless.cross.vpc.subnet.ids", context.crossVpcSubnetIds());
      conf.put("serverless.cross.vpc.security.group.id", context.crossVpcSecurityGroupId());
    }
    return conf;
  }

  private static ServerlessQueryClient newServerlessClient(
      LasIntegrationContext context, Credentials credentials) {
    ServerlessClientOption options =
        ServerlessClientOption.builder(
                credentials.getAccessKeyId(),
                credentials.getSecretAccessKey(),
                credentials.getSessionToken())
            .endpoint(context.emrServerlessEndpoint().toString())
            .service(context.emrServerlessService())
            .region(context.region())
            .connectionTimeoutMs(Math.toIntExact(context.connectTimeout().toMillis()))
            .socketTimeoutMs(Math.toIntExact(context.readTimeout().toMillis()))
            .build();
    return new ServerlessQueryClient(options);
  }

  private static String required(String name, String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException(name + " is required");
    }
    return value;
  }

  @FunctionalInterface
  interface ServerlessClientFactory {
    ServerlessQueryClient create(Credentials credentials);
  }
}
