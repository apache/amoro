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

package org.apache.amoro.utils;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.properties.MetaTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Util class convert types to metastore api structs. */
public class ConvertStructUtil {

  public static TableMetaBuilder newTableMetaBuilder(TableIdentifier identifier, Schema schema) {
    return new TableMetaBuilder(identifier, schema);
  }

  public static class TableMetaBuilder {
    TableMeta meta = new TableMeta();
    Schema schema;
    Map<String, String> properties = new HashMap<>();
    Map<String, String> locations = new HashMap<>();

    TableFormat format;

    public TableMetaBuilder(TableIdentifier identifier, Schema schema) {
      meta.setTableIdentifier(identifier.buildTableIdentifier());
      this.schema = schema;
    }

    public TableMetaBuilder withPrimaryKeySpec(PrimaryKeySpec keySpec) {
      if (keySpec == null) {
        return this;
      }
      org.apache.amoro.api.PrimaryKeySpec primaryKeySpec =
          new org.apache.amoro.api.PrimaryKeySpec();
      List<String> fields =
          keySpec.primaryKeyStruct().fields().stream()
              .map(Types.NestedField::name)
              .collect(Collectors.toList());
      primaryKeySpec.setFields(fields);
      meta.setKeySpec(primaryKeySpec);
      return this;
    }

    public TableMetaBuilder withTableLocation(String location) {
      locations.put(MetaTableProperties.LOCATION_KEY_TABLE, location);
      return this;
    }

    public TableMetaBuilder withBaseLocation(String baseLocation) {
      locations.put(MetaTableProperties.LOCATION_KEY_BASE, baseLocation);
      return this;
    }

    public TableMetaBuilder withChangeLocation(String changeLocation) {
      locations.put(MetaTableProperties.LOCATION_KEY_CHANGE, changeLocation);
      return this;
    }

    public TableMetaBuilder withProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public TableMetaBuilder withProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
    }

    public TableMetaBuilder withFormat(TableFormat format) {
      this.format = format;
      return this;
    }

    public TableMeta build() {
      Preconditions.checkNotNull(this.format, "table format must set.");
      meta.setLocations(locations);
      meta.setProperties(this.properties);
      meta.setFormat(this.format.name());
      return meta;
    }
  }
}
