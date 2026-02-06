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

/** Pre-defined actions for Paimon table format operations. */
public class PaimonActions {

  private static final TableFormat[] PAIMON_FORMATS = new TableFormat[] {TableFormat.PAIMON};

  /** Minor compaction action for Paimon tables. */
  public static final Action COMPACT = new Action(PAIMON_FORMATS, 100, "compact");

  /** Full compaction action for Paimon tables. */
  public static final Action FULL_COMPACT = new Action(PAIMON_FORMATS, 200, "full-compact");

  /** Clean metadata action for removing expired snapshots. */
  public static final Action CLEAN_METADATA = new Action(PAIMON_FORMATS, 10, "clean-meta");

  /** Delete snapshots action for Paimon tables. */
  public static final Action DELETE_SNAPSHOTS = new Action(PAIMON_FORMATS, 5, "del-snapshots");

  private PaimonActions() {
    // Private constructor to prevent instantiation
  }
}
