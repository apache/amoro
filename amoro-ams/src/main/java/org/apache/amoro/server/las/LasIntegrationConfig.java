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

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;

import java.time.Duration;

/** Configuration used to connect AMS to services in the LAS/EMR network. */
public final class LasIntegrationConfig {

  private static final String PREFIX = "las.integration.";

  public static final ConfigOption<Boolean> ENABLED =
      ConfigOptions.key(PREFIX + "enabled")
          .booleanType()
          .defaultValue(false)
          .withDescription("Whether the LAS/EMR integration is enabled.");

  public static final ConfigOption<String> HMS_URI =
      ConfigOptions.key(PREFIX + "hms-uri")
          .stringType()
          .noDefaultValue()
          .withDescription("HMS3 Thrift URI used as the metadata source of truth.");

  public static final ConfigOption<Duration> CATALOG_SYNC_INTERVAL =
      ConfigOptions.key(PREFIX + "catalog-sync-interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(5))
          .withDescription("Interval for synchronizing allowlisted HMS catalogs into AMS.");

  public static final ConfigOption<String> TOS_ENDPOINT =
      ConfigOptions.key(PREFIX + "tos-endpoint")
          .stringType()
          .noDefaultValue()
          .withDescription("TOS endpoint used by Proton file system clients.");

  public static final ConfigOption<String> IAM_ENDPOINT =
      ConfigOptions.key(PREFIX + "iam-endpoint")
          .stringType()
          .noDefaultValue()
          .withDescription("IAM endpoint used to obtain short-lived workload credentials.");

  public static final ConfigOption<String> IAM_BOOTSTRAP_ACCESS_KEY =
      ConfigOptions.key(PREFIX + "iam.bootstrap-access-key")
          .stringType()
          .noDefaultValue()
          .withDescription("Access key of the AMS workload identity used to call AssumeRole.");

  public static final ConfigOption<String> IAM_BOOTSTRAP_SECRET_KEY =
      ConfigOptions.key(PREFIX + "iam.bootstrap-secret-key")
          .stringType()
          .noDefaultValue()
          .withDescription("Secret key of the AMS workload identity used to call AssumeRole.");

  public static final ConfigOption<String> IAM_BOOTSTRAP_SESSION_TOKEN =
      ConfigOptions.key(PREFIX + "iam.bootstrap-session-token")
          .stringType()
          .defaultValue("")
          .withDescription("Optional session token of the AMS workload identity.");

  public static final ConfigOption<String> IAM_ROLE_SESSION_NAME =
      ConfigOptions.key(PREFIX + "iam.role-session-name")
          .stringType()
          .defaultValue("AmoroAssumeRoleSession")
          .withDescription("IAM role session name used by AMS.");

  public static final ConfigOption<Duration> IAM_ASSUME_ROLE_TTL =
      ConfigOptions.key(PREFIX + "iam.assume-role-ttl")
          .durationType()
          .defaultValue(Duration.ofHours(1))
          .withDescription("Lifetime of credentials returned by IAM AssumeRole.");

  public static final ConfigOption<Integer> IAM_CREDENTIAL_CACHE_SIZE =
      ConfigOptions.key(PREFIX + "iam.credential-cache-size")
          .intType()
          .defaultValue(1000)
          .withDescription("Maximum number of role credentials cached by AMS.");

  public static final ConfigOption<String> IAM_DATA_ROLE_NAME =
      ConfigOptions.key(PREFIX + "iam.data-role-name")
          .stringType()
          .defaultValue("ServiceRoleForLAS")
          .withDescription("Tenant IAM role name used by Proton to access TOS data.");

  public static final ConfigOption<String> EMR_SERVERLESS_ENDPOINT =
      ConfigOptions.key(PREFIX + "emr-serverless-endpoint")
          .stringType()
          .noDefaultValue()
          .withDescription("EMR Serverless endpoint used to submit maintenance executors.");

  public static final ConfigOption<String> EMR_SERVERLESS_SERVICE =
      ConfigOptions.key(PREFIX + "emr-serverless-service")
          .stringType()
          .defaultValue("emr_serverless")
          .withDescription("Service name used by the EMR Serverless client.");

  public static final ConfigOption<String> REGION =
      ConfigOptions.key(PREFIX + "region")
          .stringType()
          .defaultValue("cn-beijing")
          .withDescription("Cloud region containing the LAS/EMR services.");

  public static final ConfigOption<Duration> CONNECT_TIMEOUT =
      ConfigOptions.key(PREFIX + "connect-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(30))
          .withDescription("Connection timeout for LAS/EMR service clients.");

  public static final ConfigOption<Duration> READ_TIMEOUT =
      ConfigOptions.key(PREFIX + "read-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(30))
          .withDescription("Socket read timeout for LAS/EMR service clients.");

  public static final ConfigOption<Boolean> CROSS_VPC_ENABLED =
      ConfigOptions.key(PREFIX + "cross-vpc.enabled")
          .booleanType()
          .defaultValue(false)
          .withDescription("Whether EMR Serverless cross-VPC access is enabled.");

  public static final ConfigOption<String> CROSS_VPC_ACCOUNT_ID =
      ConfigOptions.key(PREFIX + "cross-vpc.account-id")
          .stringType()
          .noDefaultValue()
          .withDescription("Account ID owning the VPC used by EMR Serverless.");

  public static final ConfigOption<String> CROSS_VPC_VPC_ID =
      ConfigOptions.key(PREFIX + "cross-vpc.vpc-id")
          .stringType()
          .noDefaultValue()
          .withDescription("VPC ID used by EMR Serverless.");

  public static final ConfigOption<String> CROSS_VPC_SUBNET_IDS =
      ConfigOptions.key(PREFIX + "cross-vpc.subnet-ids")
          .stringType()
          .noDefaultValue()
          .withDescription("Comma-separated subnet IDs used by EMR Serverless.");

  public static final ConfigOption<String> CROSS_VPC_SECURITY_GROUP_ID =
      ConfigOptions.key(PREFIX + "cross-vpc.security-group-id")
          .stringType()
          .noDefaultValue()
          .withDescription("Security group ID used by EMR Serverless.");

  private LasIntegrationConfig() {}
}
