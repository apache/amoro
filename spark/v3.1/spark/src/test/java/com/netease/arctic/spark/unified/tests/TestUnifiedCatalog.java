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

package com.netease.arctic.spark.unified.tests;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.test.SparkTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true, unifiedCatalog = true)
public class TestUnifiedCatalog extends SparkTestBase {
  public static Stream<Arguments> testTableFormats() {
    return Arrays.stream(TableFormat.values()).map(Arguments::of);
  }


  @ParameterizedTest
  @MethodSource
  public void testTableFormats(TableFormat format) {
    String sqlText =
        "CREATE TABLE "
            + target()
            + " ( "
            + "id int, "
            + "data string, "
            + "pt string"
            + ") USING "
            + provider(format)
            + " PARTITIONED BY (pt) ";
    sql(sqlText);

    sqlText =
        "INSERT INTO "
            + target()
            + " VALUES "
            + "(1, 'a', '2020-01-01'), (2, 'b', '2020-01-02'), (3, 'c', '2020-01-03')";
    sql(sqlText);

    sqlText = "SELECT * FROM " + target();
    long count = sql(sqlText).count();
    Assertions.assertEquals(3, count);
  }

}
