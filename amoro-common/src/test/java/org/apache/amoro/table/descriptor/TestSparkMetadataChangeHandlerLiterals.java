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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Guards single-quoted Spark SQL literal escaping. Spark uses backslash escapes inside regular
 * string literals, not the SQL-standard doubled apostrophe, so a raw backslash must be doubled and
 * a raw apostrophe backslash-escaped wherever a key, value or comment is spliced between single
 * quotes.
 */
public class TestSparkMetadataChangeHandlerLiterals {

  private static final String TABLE_NAME = "tbl";

  /**
   * Plain ASCII keys, values and comments must keep producing exactly the same DDL as before: the
   * escaping must be a no-op when there is nothing to escape.
   */
  @Test
  public void simpleLiteralsAreUnchanged() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);

    Assertions.assertEquals(
        "ALTER TABLE tbl SET TBLPROPERTIES ('k1' = 'v1')",
        handler.changeAndAddProperties(Collections.singletonMap("k1", "v1")));
    Assertions.assertEquals(
        "ALTER TABLE tbl UNSET TBLPROPERTIES ('k1')",
        handler.removeProperties(Collections.singleton("k1")));
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT 'new comment'",
        handler.changeColumnsComment("c", "new comment"));
  }

  /** {@code '} must be escaped in both the property key and the property value. */
  @Test
  public void changeAndAddPropertiesEscapesApostropheInKeyAndValue() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    String ddl = handler.changeAndAddProperties(Collections.singletonMap("user's", "O'Reilly"));

    Assertions.assertEquals("ALTER TABLE tbl SET TBLPROPERTIES ('user\\'s' = 'O\\'Reilly')", ddl);
    Assertions.assertFalse(ddl.contains("''"), "apostrophe must be backslash-escaped, not doubled");
  }

  /** {@code \} must be doubled in both the property key and the property value. */
  @Test
  public void changeAndAddPropertiesEscapesBackslashInKeyAndValue() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    String ddl = handler.changeAndAddProperties(Collections.singletonMap("a\\b", "C:\\tmp"));

    Assertions.assertEquals("ALTER TABLE tbl SET TBLPROPERTIES ('a\\\\b' = 'C:\\\\tmp')", ddl);
  }

  /** {@code \} must be doubled in a removed property key as well. */
  @Test
  public void removePropertiesEscapesBackslashInKey() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl UNSET TBLPROPERTIES ('a\\\\b')",
        handler.removeProperties(Collections.singleton("a\\b")));
  }

  /** A column comment containing both an apostrophe and a backslash. */
  @Test
  public void changeColumnsCommentEscapesApostropheAndBackslash() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT 'O\\'Reilly\\'s C:\\\\tmp'",
        handler.changeColumnsComment("c", "O'Reilly's C:\\tmp"));
  }

  /**
   * A backslash immediately before an apostrophe must be escaped backslash-first, leaving the quote
   * unambiguous instead of a stray unterminated quote.
   */
  @Test
  public void backslashImmediatelyBeforeQuoteIsEscapedInOrder() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT 'a\\\\\\'b'",
        handler.changeColumnsComment("c", "a\\'b"));
  }

  /** A raw newline or tab must be emitted escaped so the comment value survives. */
  @Test
  public void changeColumnsCommentEscapesNewlineAndTab() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT 'line1\\nline2\\tend'",
        handler.changeColumnsComment("c", "line1\nline2\tend"));
  }

  /** A null comment removes the comment, which Spark spells as an empty literal. */
  @Test
  public void changeColumnsCommentNullBecomesEmptyLiteral() {
    SparkMetadataChangeHandler handler = new SparkMetadataChangeHandler(TABLE_NAME);
    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT ''", handler.changeColumnsComment("c", null));
  }

  /**
   * A comment removal arriving through the real {@link DDLReverser} history path must also emit an
   * empty literal rather than the text {@code null}.
   */
  @Test
  public void ddlReverserRemovingCommentEmitsEmptyLiteral() {
    List<DDLInfo> ddls =
        reverse(
            meta(0L, "old comment", Collections.emptyMap()),
            meta(1L, null, Collections.emptyMap()));

    Assertions.assertEquals(1, ddls.size());
    Assertions.assertEquals("ALTER TABLE tbl ALTER COLUMN c COMMENT ''", ddls.get(0).getDdl());
    Assertions.assertEquals(DDLInfo.DDLType.UPDATE_SCHEMA, ddls.get(0).getDdlType());
  }

  /** Drives the {@link DDLReverser} property history (removed key, changed value) and comment. */
  @Test
  public void ddlReverserEscapesPropertyAndCommentHistory() {
    Map<String, String> preProperties = new LinkedHashMap<>();
    preProperties.put("d\\rop", "x");
    preProperties.put("k", "old");

    List<DDLInfo> ddls =
        reverse(
            meta(0L, "old comment", preProperties),
            meta(1L, "it's", Collections.singletonMap("k", "O'Reilly")));

    Assertions.assertEquals(3, ddls.size());

    Assertions.assertEquals(
        "ALTER TABLE tbl UNSET TBLPROPERTIES ('d\\\\rop')", ddls.get(0).getDdl());
    Assertions.assertEquals(DDLInfo.DDLType.UPDATE_PROPERTIES, ddls.get(0).getDdlType());

    Assertions.assertEquals(
        "ALTER TABLE tbl SET TBLPROPERTIES ('k' = 'O\\'Reilly')", ddls.get(1).getDdl());
    Assertions.assertEquals(DDLInfo.DDLType.UPDATE_PROPERTIES, ddls.get(1).getDdlType());

    Assertions.assertEquals(
        "ALTER TABLE tbl ALTER COLUMN c COMMENT 'it\\'s'", ddls.get(2).getDdl());
    Assertions.assertEquals(DDLInfo.DDLType.UPDATE_SCHEMA, ddls.get(2).getDdlType());
  }

  /** Drives {@link DDLReverser} with two fake metadata snapshots. */
  private static List<DDLInfo> reverse(
      TableMetaExtract.InternalTableMeta pre, TableMetaExtract.InternalTableMeta current) {
    TableMetaExtract<String> extractor = table -> Arrays.asList(pre, current);
    return new DDLReverser<>(extractor)
        .reverse("table", TableIdentifier.of("catalog", "db", TABLE_NAME));
  }

  private static TableMetaExtract.InternalTableMeta meta(
      long time, String comment, Map<String, String> properties) {
    return new TableMetaExtract.InternalTableMeta(
        time, Collections.singletonList(schema(0, comment, false)), properties);
  }

  private static TableMetaExtract.InternalSchema schema(int id, String comment, boolean required) {
    return new TableMetaExtract.InternalSchema(id, null, "c", "int", comment, required);
  }
}
