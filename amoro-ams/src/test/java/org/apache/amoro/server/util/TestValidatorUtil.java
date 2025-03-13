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

package org.apache.amoro.server.util;

import org.apache.amoro.server.utils.ValidatorUtil;
import org.junit.Assert;
import org.junit.Test;

public class TestValidatorUtil {

  @Test
  public void testValidateGroupName_ValidNames() {
    Assert.assertTrue(ValidatorUtil.validateGroupName("ValidName"));
    Assert.assertTrue(ValidatorUtil.validateGroupName("valid-name"));
    Assert.assertTrue(ValidatorUtil.validateGroupName("Valid_Name123"));
    Assert.assertTrue(ValidatorUtil.validateGroupName("A"));
    Assert.assertTrue(ValidatorUtil.validateGroupName(createString(50)));
  }

  @Test
  public void testValidateGroupNameNull() {
    Assert.assertFalse(ValidatorUtil.validateGroupName(null));
  }

  @Test
  public void testValidateGroupNameEmpty() {
    Assert.assertFalse(ValidatorUtil.validateGroupName(""));
  }

  @Test
  public void testValidateGroupNameTooLong() {
    String tooLong = createString(51);
    Assert.assertFalse(ValidatorUtil.validateGroupName(tooLong));
  }

  @Test
  public void testValidateGroupNameInvalidCharacters() {
    Assert.assertFalse(ValidatorUtil.validateGroupName("invalid name"));
    Assert.assertFalse(ValidatorUtil.validateGroupName("name@domain"));
    Assert.assertFalse(ValidatorUtil.validateGroupName("name#hash"));
    Assert.assertFalse(ValidatorUtil.validateGroupName("中文"));
    Assert.assertFalse(ValidatorUtil.validateGroupName("name.with.dots"));
  }

  private String createString(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append('a');
    }
    return sb.toString();
  }
}
