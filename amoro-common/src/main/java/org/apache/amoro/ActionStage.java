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

public class ActionStage {

  private final String desc;
  private final int weight;

  /**
   * Action Stage description
   * @param desc Action Stage description value, normally this value should be identical within certain actions
   * @param weight the weight number of this action, the bigger the weight number, the higher position on front pages
   */
  public ActionStage(String desc, int weight) {
    this.desc = desc;
    this.weight = weight;
  }

  public String getDesc() {
    return desc;
  }

  public int getWeight() {
    return weight;
  }
}
