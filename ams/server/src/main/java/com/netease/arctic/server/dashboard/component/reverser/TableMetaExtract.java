/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard.component.reverser;

import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;


/**
 * An interface for returning the historical metadata of a table.
 * The table can be iceberg,mixed-iceberg,mixed-hive,paimon and so on.
 *
 * @param <T> Table type
 */
interface TableMetaExtract<T> {

  /**
   * Extract the historical metadata of a table.
   */
  List<InternalTableMeta> extractTable(T table);

  class InternalTableMeta {

    private final long time;

    private final List<InternalSchema> internalSchemas;

    private final Map<String, String> properties;

    public InternalTableMeta(long time, List<InternalSchema> internalSchemas, Map<String, String> properties) {
      this.time = time;
      this.internalSchemas = internalSchemas;
      this.properties = properties;
    }

    public long getTime() {
      return time;
    }

    public List<InternalSchema> getInternalSchema() {
      return internalSchemas;
    }

    public Map<String, String> getProperties() {
      return properties;
    }
  }

  class InternalSchema {
    private final int id;

    private final Integer parentId;

    private final String name;

    private final String type;

    private final String comment;

    private final boolean required;

    public InternalSchema(
        int id,
        @Nullable Integer parentId,
        String name,
        String type,
        String comment,
        boolean required) {
      this.id = id;
      this.parentId = parentId;
      this.name = name;
      this.type = type;
      this.comment = comment;
      this.required = required;
    }

    public int getId() {
      return id;
    }

    public Integer getParentId() {
      return parentId;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getComment() {
      return comment;
    }

    public boolean isRequired() {
      return required;
    }

    public String columnString() {
      return String.format("%s %s", name, type);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("parentId", parentId)
          .add("name", name)
          .add("type", type)
          .add("comment", comment)
          .add("required", required)
          .toString();
    }
  }
}
