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

package org.apache.amoro.table.descriptor;

import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Guards the nullability contract of the reversed DDL: {@code required == true} means the column is
 * NOT NULL, so becoming required must produce {@code SET NOT NULL} and becoming optional must
 * produce {@code DROP NOT NULL}.
 */
public class TestSparkMetadataChangeHandler {

  private static final String TABLE_NAME = "tbl";

  @Test
  public void changeColumnsRequireToRequired() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c SET NOT NULL", handler.changeColumnsRequire("c", true));
  }

  @Test
  public void changeColumnsRequireToOptional() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c DROP NOT NULL", handler.changeColumnsRequire("c", false));
  }

  @Test
  public void ddlReverserOptionalToRequired() {
    List<DDLInfo> ddls = reverseColumnRequired(false, true);
    Assertions.assertEquals(1, ddls.size());
    Assertions.assertEquals("ALTER TABLE tbl ALTER COLUMN c SET NOT NULL", ddls.get(0).getDdl());
  }

  @Test
  public void ddlReverserRequiredToOptional() {
    List<DDLInfo> ddls = reverseColumnRequired(true, false);
    Assertions.assertEquals(1, ddls.size());
    Assertions.assertEquals("ALTER TABLE tbl ALTER COLUMN c DROP NOT NULL", ddls.get(0).getDdl());
  }

  /** Drives {@link DDLReverser} with a fake extractor holding two metadata snapshots. */
  private static List<DDLInfo> reverseColumnRequired(boolean preRequired, boolean currentRequired) {
    TableMetaExtract<String> extractor =
        table -> {
          TableMetaExtract.InternalTableMeta pre =
              new TableMetaExtract.InternalTableMeta(
                  0L, Collections.singletonList(schema(0, preRequired)), Collections.emptyMap());
          TableMetaExtract.InternalTableMeta current =
              new TableMetaExtract.InternalTableMeta(
                  1L,
                  Collections.singletonList(schema(0, currentRequired)),
                  Collections.emptyMap());
          return Arrays.asList(pre, current);
        };

    return new DDLReverser<>(extractor)
        .reverse("table", TableIdentifier.of("catalog", "db", TABLE_NAME));
  }

  private static TableMetaExtract.InternalSchema schema(int id, boolean required) {
    return new TableMetaExtract.InternalSchema(id, null, "c", "int", null, required);
  }
}
