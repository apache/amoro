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

import com.google.common.collect.Lists;
import org.apache.iceberg.mapping.MappedField;
import org.apache.iceberg.mapping.MappedFields;
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
    MessageType fileSchema = Types.buildMessage().addField(Types.buildGroup(Type.Repetition.OPTIONAL)
        .addField(Types.primitive(PrimitiveType.PrimitiveTypeName.DOUBLE, Type.Repetition.REQUIRED).named("X"))
        .addField(Types.primitive(PrimitiveType.PrimitiveTypeName.DOUBLE, Type.Repetition.REQUIRED).named("Y"))
        .named("custom_element_name".toUpperCase())).named("table");
    MappedFields mappedFields = MappedFields.of(Lists.newArrayList(
        MappedField.of(2, "x"),
        MappedField.of(3, "y")));
    MappedField mappedField = MappedField.of(1, "custom_element_name", mappedFields);
    NameMapping nameMapping = NameMapping.of(mappedField);
    MessageType actual = (MessageType) ParquetTypeVisitor.visit(fileSchema, new AdaptHiveApplyNameMapping(nameMapping));
    Assert.assertEquals("custom_element_name".toUpperCase(), actual.getFields().get(0).getName());
    Assert.assertEquals(1, actual.getFields().get(0).getId().intValue());
    Assert.assertEquals(2, actual.getFields().get(0).asGroupType().getType("X").getId().intValue());
  }
}
