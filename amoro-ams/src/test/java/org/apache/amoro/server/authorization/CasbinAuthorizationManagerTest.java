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

package org.apache.amoro.server.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CasbinAuthorizationManagerTest {

  @Test
  public void testResolvePrivilegesForBuiltInRoles() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);

    CasbinAuthorizationManager manager = new CasbinAuthorizationManager(conf);

    Set<String> viewerPrivileges = manager.resolvePrivileges(Set.of(Role.VIEWER));
    assertTrue(viewerPrivileges.contains(Privilege.VIEW_TABLE.name()));
    assertFalse(viewerPrivileges.contains(Privilege.VIEW_SYSTEM.name()));

    Set<String> adminPrivileges = manager.resolvePrivileges(Set.of(Role.SERVICE_ADMIN));
    assertTrue(adminPrivileges.contains(Privilege.VIEW_SYSTEM.name()));
    assertTrue(adminPrivileges.contains(Privilege.EXECUTE_SQL.name()));
  }
}
