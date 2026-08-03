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

import org.apache.commons.lang3.StringUtils;

/** Tenant-scoped identity and resource information required by one LAS operation. */
public final class LasTenantContext {

  private final String accountId;
  private final String catalogName;
  private final String queueName;
  private final String submitRoleTrn;
  private final String dataRoleTrn;

  public LasTenantContext(
      String accountId,
      String catalogName,
      String queueName,
      String submitRoleTrn,
      String dataRoleTrn) {
    this.accountId = required("accountId", accountId);
    this.catalogName = required("catalogName", catalogName);
    this.queueName = required("queueName", queueName);
    this.submitRoleTrn = requiredRole("submitRoleTrn", submitRoleTrn);
    this.dataRoleTrn = requiredRole("dataRoleTrn", dataRoleTrn);
  }

  public String accountId() {
    return accountId;
  }

  public String catalogName() {
    return catalogName;
  }

  public String queueName() {
    return queueName;
  }

  public String submitRoleTrn() {
    return submitRoleTrn;
  }

  public String dataRoleTrn() {
    return dataRoleTrn;
  }

  private static String required(String name, String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException(name + " is required");
    }
    return value;
  }

  private static String requiredRole(String name, String value) {
    String role = required(name, value);
    if (!role.startsWith("trn:iam:") || !role.contains(":role/")) {
      throw new IllegalArgumentException(name + " must be an IAM role TRN");
    }
    return role;
  }
}
