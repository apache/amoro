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

package com.netease.arctic.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class TableConstants {

  public static final String PROPERTY_KEY_CREATE_TYPE = "_hidden.arctic.table.create_type";
  public static final String PROPERTY_VALUE_CREATE_TYPE_NEW = "create";
  public static final String PROPERTY_VALUE_CREATE_TYPE_UPGRADE = "upgrade";

  public static final Set<String> HIDDEN_PROPERTIES = Collections.unmodifiableSet(
      Arrays.stream(new String[] {
          PROPERTY_KEY_CREATE_TYPE
      }).collect(Collectors.toSet())
  );
}
