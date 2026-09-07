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

package org.apache.amoro.formats.mixed;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.TableFormat;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.junit.jupiter.api.Test;

public class TestMixedCatalog {

  @Test
  public void testListTablesPreservesUnexpectedFailure() {
    MixedFormatCatalog delegate = mock(MixedFormatCatalog.class);
    IllegalStateException failure = new IllegalStateException("failed to read table metadata");
    when(delegate.listTables("db")).thenThrow(failure);

    MixedCatalog catalog = new MixedCatalog(delegate, TableFormat.MIXED_ICEBERG);

    IllegalStateException actual =
        assertThrows(IllegalStateException.class, () -> catalog.listTables("db"));
    assertSame(failure, actual);
  }

  @Test
  public void testListTablesConvertsMissingNamespace() {
    MixedFormatCatalog delegate = mock(MixedFormatCatalog.class);
    NoSuchNamespaceException failure = new NoSuchNamespaceException("missing namespace");
    when(delegate.listTables("missing_db")).thenThrow(failure);

    MixedCatalog catalog = new MixedCatalog(delegate, TableFormat.MIXED_ICEBERG);

    NoSuchDatabaseException actual =
        assertThrows(NoSuchDatabaseException.class, () -> catalog.listTables("missing_db"));
    assertSame(failure, actual.getCause());
  }
}
