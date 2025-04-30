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

import com.google.common.collect.Maps;
import org.apache.amoro.server.Environments;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;

public class UserInfoManager {

  private final Map<String, String> users = Maps.newHashMap();

  public UserInfoManager() {
    String configPath = Environments.getConfigPath() + "/users.csv";
    this.loadUserInfoFileToMap(configPath);
  }

  public boolean isValidate(String username, String password) {
    if (users.containsKey(username)) {
      return users.get(username).equals(password);
    }
    return false;
  }

  private void loadUserInfoFileToMap(String filePath) {
    try {
      FileUtils.readLines(new File(filePath), "UTF-8")
          .forEach(
              line -> {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                  String username = parts[0].trim();
                  String password = parts[1].trim();
                  users.put(username, password);
                }
              });
    } catch (Exception e) {
      throw new RuntimeException("load userInfo file error", e);
    }
  }
}
