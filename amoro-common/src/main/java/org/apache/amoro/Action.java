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

public class Action {

  private final TableFormat[] formats;
  private final int code;
  private final int weight;
  private final String desc;

  Action(TableFormat[] formats, int code, int weight, String desc) {
    this.formats = formats;
    this.code = code;
    this.desc = desc;
    this.weight = weight;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public int getWeight() {
    return weight;
  }

  public TableFormat[] supportedFormats() {
    return formats;
  }
}
