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

package org.apache.amoro.spark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.spark.mixed.MixedSparkCatalogBase;
import org.apache.amoro.table.TableProperties;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.TableChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

/** Shared regression cases for the supported Spark catalog implementations. */
public abstract class MixedCatalogPropertyProtectionTestBase {
  private static final Identifier IDENTIFIER = Identifier.of(new String[] {"database"}, "table");

  protected abstract TableCatalog newCatalog();

  @ParameterizedTest
  @MethodSource("protectedChanges")
  void testInternalPropertiesAreRejectedBeforeCatalogAccess(String property, boolean remove) {
    TableCatalog catalog = newCatalog();
    TableChange change =
        remove
            ? TableChange.removeProperty(property)
            : TableChange.setProperty(property, "invalid");

    IllegalArgumentException failure =
        assertThrows(IllegalArgumentException.class, () -> catalog.alterTable(IDENTIFIER, change));

    assertTrue(failure.getMessage().contains(property));
  }

  @Test
  void testEntireRequestIsRejectedBeforeAnyCommit() {
    TableCatalog catalog = newCatalog();
    TableChange writable = TableChange.setProperty("self-optimizing.enabled", "false");
    TableChange internal =
        TableChange.setProperty(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID, "0");

    IllegalArgumentException failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> catalog.alterTable(IDENTIFIER, writable, internal));

    assertTrue(failure.getMessage().contains(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "self-optimizing.enabled",
        "format-version",
        "location",
        "schema.name-mapping.default"
      })
  void testSupportedDdlPropertiesAreStillAccepted(String property) {
    assertDoesNotThrow(
        () ->
            ValidationAccess.check(
                TableChange.setProperty(property, "value"), TableChange.removeProperty(property)));
  }

  @Test
  void testSchemaChangesAndEmptyRequestsRemainAllowed() {
    assertDoesNotThrow(() -> ValidationAccess.check());
    assertDoesNotThrow(
        () ->
            ValidationAccess.check(
                TableChange.updateColumnComment(new String[] {"column"}, "comment")));
  }

  protected static Stream<Arguments> protectedChanges() {
    return Stream.of(
            TableProperties.TABLE_PARTITION_PROPERTIES,
            TableProperties.BASE_TABLE_MAX_TRANSACTION_ID,
            TableProperties.PARTITION_OPTIMIZED_SEQUENCE,
            TableProperties.WATERMARK_TABLE,
            TableProperties.WATERMARK_BASE_STORE)
        .flatMap(
            property -> Stream.of(Arguments.of(property, false), Arguments.of(property, true)));
  }

  private abstract static class ValidationAccess extends MixedSparkCatalogBase {
    static void check(TableChange... changes) {
      validatePropertyChanges(changes);
    }
  }
}
