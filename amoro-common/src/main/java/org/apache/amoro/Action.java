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

import java.util.Arrays;
import java.util.Objects;

public final class Action {

  /** supported table formats of this action */
  private final TableFormat[] formats;

  private String name;
  /**
   * the weight number of this action, the bigger the weight number, the higher positions of
   * schedulers or front pages
   */
  private final int weight;

  /** description of this action, will be shown in front pages */
  private final String desc;

  public Action(TableFormat[] formats, int weight, String desc) {
    this.formats = formats;
    this.desc = desc;
    this.weight = weight;
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

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Action action = (Action) o;
    return Objects.equals(name, action.name) && Arrays.equals(formats, action.formats);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name);
    result = 31 * result + Arrays.hashCode(formats);
    return result;
  }
}
