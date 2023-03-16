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

package org.apache.iceberg.parquet;

import org.apache.iceberg.mapping.MappedField;
import org.apache.iceberg.mapping.NameMapping;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.apache.parquet.schema.Types;
import org.junit.Assert;
import org.junit.Test;

public class TestAdaptHiveReadConf {

  @Test
  public void testConvertNameMapping() {
    String fieldName = "Test";
    MessageType fileSchema = Types.buildMessage().addField(Types.primitive(PrimitiveType.PrimitiveTypeName.DOUBLE,
        Type.Repetition.REQUIRED).named(fieldName)).named("table");
    MappedField mappedField = MappedField.of(1, fieldName.toLowerCase());
    NameMapping nameMapping = NameMapping.of(mappedField);
    NameMapping actual = AdaptHiveReadConf.convertNameMapping(fileSchema, nameMapping);
    Assert.assertNotNull(actual.find(fieldName));
    Assert.assertEquals(1, (int) actual.find(fieldName).id());
  }
}
