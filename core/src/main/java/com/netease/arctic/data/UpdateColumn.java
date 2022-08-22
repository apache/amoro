/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.data;

import com.netease.arctic.trace.AmsTableTracer;
import org.apache.iceberg.types.Type;

public class UpdateColumn {
  private final String parent;
  private final String name;
  private final Type type;
  private final String doc;
  private final AmsTableTracer.SchemaOperateType operate;
  private final Boolean isOptional;
  private final String newName;

  public UpdateColumn(
      String name,
      String parent,
      Type type,
      String doc,
      AmsTableTracer.SchemaOperateType operate,
      Boolean isOptional,
      String newName) {
    this.parent = parent;
    this.name = name;
    this.type = type;
    this.doc = doc;
    this.operate = operate;
    this.isOptional = isOptional;
    this.newName = newName;
  }

  public String getParent() {
    return parent;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public String getDoc() {
    return doc;
  }

  public AmsTableTracer.SchemaOperateType getOperate() {
    return operate;
  }

  public Boolean getOptional() {
    return isOptional;
  }

  public String getNewName() {
    return newName;
  }
}
