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

package org.apache.amoro;

public class IcebergActions {

  private static final TableFormat[] DEFAULT_FORMATS =
      new TableFormat[] {TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE};

  public static final Action SYSTEM = new Action(DEFAULT_FORMATS, 0, "system");
  public static final Action REWRITE = new Action(DEFAULT_FORMATS, 10, "rewrite");
  public static final Action DELETE_ORPHANS = new Action(DEFAULT_FORMATS, 2, "delete-orphans");
  public static final Action SYNC_HIVE = new Action(DEFAULT_FORMATS, 3, "sync-hive");
  public static final Action EXPIRE_DATA = new Action(DEFAULT_FORMATS, 1, "expire-data");
}
