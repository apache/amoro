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

package org.apache.amoro.server.permission;

import org.apache.amoro.server.Environments;
import org.casbin.jcasbin.main.Enforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PermissionManager {

  public static final Logger LOG = LoggerFactory.getLogger(UserInfoManager.class);

  private final Enforcer enforcer;

  public PermissionManager() {
    String modelPath = Environments.getConfigPath() + "/rbac_model.conf";
    String policyPath = Environments.getConfigPath() + "/policy.csv";
    File modelFile = new File(modelPath);
    File policyFile = new File(policyPath);
    if (!modelFile.exists() || !policyFile.exists()) {
      enforcer = new Enforcer();
      LOG.warn("model or policy file not exist, please check your config");
      return;
    }
    enforcer = new Enforcer(modelPath, policyPath);
  }

  public boolean accessible(String user, String url, String method) {
    if (!enforcer.enforce(user, url, method)) {
      return false;
    }
    return true;
  }
}
