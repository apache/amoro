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

import useStore from '@/store'

export type UserPrivilege =
  | 'VIEW_SYSTEM'
  | 'VIEW_CATALOG'
  | 'VIEW_TABLE'
  | 'VIEW_OPTIMIZER'
  | 'MANAGE_CATALOG'
  | 'MANAGE_TABLE'
  | 'MANAGE_OPTIMIZER'
  | 'EXECUTE_SQL'
  | 'MANAGE_PLATFORM'

export function getRoles(): string[] {
  const store = useStore()
  const roles = store.userInfo.roles || []
  if (roles.length) {
    return roles
  }
  return store.userInfo.role ? [store.userInfo.role] : []
}

export function getPrivileges(): UserPrivilege[] {
  const store = useStore()
  return (store.userInfo.privileges || []) as UserPrivilege[]
}

export function hasPrivilege(privilege: UserPrivilege): boolean {
  return getPrivileges().includes(privilege)
}

export function canViewSystem(): boolean {
  return hasPrivilege('VIEW_SYSTEM')
}

export function canViewCatalog(): boolean {
  return hasPrivilege('VIEW_CATALOG')
}

export function canManageCatalog(): boolean {
  return hasPrivilege('MANAGE_CATALOG')
}

export function canViewTable(): boolean {
  return hasPrivilege('VIEW_TABLE')
}

export function canManageTable(): boolean {
  return hasPrivilege('MANAGE_TABLE')
}

export function canViewOptimizer(): boolean {
  return hasPrivilege('VIEW_OPTIMIZER')
}

export function canManageOptimizer(): boolean {
  return hasPrivilege('MANAGE_OPTIMIZER')
}

export function canExecuteSql(): boolean {
  return hasPrivilege('EXECUTE_SQL')
}

export function canManagePlatform(): boolean {
  return hasPrivilege('MANAGE_PLATFORM')
}

export function getDefaultRoute(): string {
  if (canViewTable()) {
    return '/tables'
  }
  if (canViewCatalog()) {
    return '/catalogs'
  }
  if (canViewOptimizer()) {
    return '/optimizing'
  }
  if (canViewSystem()) {
    return '/overview'
  }
  if (canExecuteSql()) {
    return '/terminal'
  }
  if (canManagePlatform()) {
    return '/settings'
  }
  return '/login'
}
