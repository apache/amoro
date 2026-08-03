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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bytedance.olap.iam.Credential;
import bytedance.olap.iam.IAMService;
import bytedance.olap.iam.http.model.AssumeRoleResponse;
import bytedance.olap.iam.http.model.AssumeRoleResponse.Credentials;
import com.volcengine.emr.serverless.Job;
import com.volcengine.emr.serverless.SQLTask;
import com.volcengine.emr.serverless.ServerlessQueryClient;
import org.apache.amoro.client.ClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Executable example of the HMS -> IAM -> EMR Serverless Spark SQL scaffold. */
public class TestLasSparkSqlFlow {

  private static final String CATALOG = "123456789@las";
  private static final String SUBMIT_ROLE = "trn:iam::123456789:role/EmrJobSubmitRole";
  private static final String DATA_ROLE = "trn:iam::123456789:role/LasDataAccessRole";

  @Test
  public void testCompleteSparkSqlFlow() throws Exception {
    LasIntegrationContext integration =
        LasIntegrationContext.initialize(TestLasIntegrationContext.validConfigurations());

    HMSClient hmsSdk = mock(HMSClient.class);
    when(hmsSdk.getCatalogs()).thenReturn(Collections.singletonList(CATALOG));
    when(hmsSdk.getAllDatabases()).thenReturn(Collections.singletonList("analytics"));
    when(hmsSdk.getAllTables("analytics")).thenReturn(Collections.singletonList("orders"));
    Table hmsTable = new Table();
    hmsTable.setDbName("analytics");
    hmsTable.setTableName("orders");
    when(hmsSdk.getTable("analytics", "orders")).thenReturn(hmsTable);
    HMSClientPool hmsPool = directPool(hmsSdk);
    LasHmsClient hms = new LasHmsClient(hmsPool, ignored -> hmsPool);

    Assertions.assertEquals(Collections.singletonList(CATALOG), hms.listCatalogs());
    Assertions.assertEquals(Collections.singletonList("analytics"), hms.listDatabases(CATALOG));
    Assertions.assertEquals(
        Collections.singletonList("orders"), hms.listTables(CATALOG, "analytics"));
    Assertions.assertSame(hmsTable, hms.loadTable(CATALOG, "analytics", "orders"));

    IAMService iamSdk = mock(IAMService.class);
    when(iamSdk.assumeRole(any(Credential.class), anyString(), anyString(), anyInt(), isNull()))
        .thenAnswer(
            invocation ->
                assumeRoleResponse(
                    invocation.getArgument(1),
                    SUBMIT_ROLE.equals(invocation.getArgument(1)) ? "submit" : "data"));
    LasIamClient iam =
        new LasIamClient(integration, iamSdk, new Credential("bootstrap-ak", "bootstrap-sk"));

    ServerlessQueryClient serverlessSdk = mock(ServerlessQueryClient.class);
    Job submitted = mock(Job.class);
    when(submitted.getId()).thenReturn("job-20260803-0001");
    when(serverlessSdk.executeSQL(any(SQLTask.class))).thenReturn(submitted);
    Job running = mock(Job.class);
    when(serverlessSdk.getJob(submitted.getId())).thenReturn(running);
    AtomicReference<Credentials> clientCredentials = new AtomicReference<>();
    ServerlessSparkSqlManager sparkSql =
        new ServerlessSparkSqlManager(
            integration,
            iam,
            credentials -> {
              clientCredentials.set(credentials);
              return serverlessSdk;
            });

    LasTenantContext tenant =
        new LasTenantContext(
            "123456789", CATALOG, "tenant-production-queue", SUBMIT_ROLE, DATA_ROLE);
    String sql = "CALL spark_catalog.system.rewrite_data_files(table => 'analytics.orders')";
    String jobId =
        sparkSql.submit(
            tenant,
            "compact-analytics-orders",
            sql,
            Collections.singletonMap("spark.sql.shuffle.partitions", "200"));

    Assertions.assertEquals(submitted.getId(), jobId);
    Assertions.assertEquals("submit-ak", clientCredentials.get().getAccessKeyId());
    ArgumentCaptor<SQLTask> taskCaptor = ArgumentCaptor.forClass(SQLTask.class);
    verify(serverlessSdk).executeSQL(taskCaptor.capture());
    SQLTask task = taskCaptor.getValue();
    Assertions.assertEquals(sql, task.getQuery());
    Assertions.assertEquals("tenant-production-queue", task.getQueue().orElse(null));
    Assertions.assertFalse(task.isSync());
    Map<String, String> taskConf = task.getConf();
    Assertions.assertEquals(CATALOG, taskConf.get("spark.hive.metastore.catalog.default"));
    Assertions.assertEquals(
        "thrift://hms-service:9083", taskConf.get("spark.hadoop.hive.metastore.uris"));
    Assertions.assertEquals(
        "io.proton.common.object.auth.SimpleCredentialsProvider",
        taskConf.get("spark.hadoop.fs.tos.credentials.provider"));
    Assertions.assertEquals("data-ak", taskConf.get("spark.hadoop.fs.tos.access-key-id"));
    Assertions.assertEquals("data-token", taskConf.get("spark.hadoop.fs.tos.session-token"));
    Assertions.assertEquals("200", taskConf.get("spark.sql.shuffle.partitions"));

    Assertions.assertSame(running, sparkSql.getJob(tenant, jobId));
    sparkSql.cancelJob(tenant, jobId);
    verify(serverlessSdk).cancelJob(jobId);

    // Submit and data credentials are each loaded once; status/cancel reuse the submit-role cache.
    verify(iamSdk, times(2))
        .assumeRole(any(Credential.class), anyString(), anyString(), anyInt(), isNull());
    iam.close();
    verify(iamSdk).close();
  }

  private static AssumeRoleResponse assumeRoleResponse(String roleTrn, String credentialPrefix) {
    Credentials credentials = new Credentials();
    credentials.setAccessKeyId(credentialPrefix + "-ak");
    credentials.setSecretAccessKey(credentialPrefix + "-sk");
    credentials.setSessionToken(credentialPrefix + "-token");
    credentials.setCurrentTime("2026-08-03T13:00:00+00:00");
    credentials.setExpiredTime("2099-08-03T14:00:00+00:00");

    AssumeRoleResponse.ResultBean result = new AssumeRoleResponse.ResultBean();
    result.setCredentials(credentials);
    AssumeRoleResponse.AssumedRoleUser user = new AssumeRoleResponse.AssumedRoleUser();
    user.setTrn(roleTrn);
    result.setAssumedRoleUser(user);

    AssumeRoleResponse response = new AssumeRoleResponse();
    response.setResult(result);
    return response;
  }

  private static HMSClientPool directPool(HMSClient client) {
    return new HMSClientPool() {
      @Override
      public <R> R run(ClientPool.Action<R, HMSClient, TException> action) throws TException {
        return action.run(client);
      }

      @Override
      public <R> R run(ClientPool.Action<R, HMSClient, TException> action, boolean retry)
          throws TException {
        return action.run(client);
      }
    };
  }
}
