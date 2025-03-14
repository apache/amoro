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

import org.apache.amoro.server.dashboard.controller.OptimizerGroupController;
import org.junit.Assert;
import org.junit.Test;

public class TestValidateGroupName {
  @Test
  public void testValidateGroupName_ValidNames() {
    Assert.assertTrue(OptimizerGroupController.validateGroupName("ValidName"));
    Assert.assertTrue(OptimizerGroupController.validateGroupName("valid-name"));
    Assert.assertTrue(OptimizerGroupController.validateGroupName("Valid_Name123"));
    Assert.assertTrue(OptimizerGroupController.validateGroupName("A"));
    Assert.assertTrue(OptimizerGroupController.validateGroupName(createString(50)));
  }

  @Test
  public void testValidateGroupNameNull() {
    Assert.assertFalse(OptimizerGroupController.validateGroupName(null));
  }

  @Test
  public void testValidateGroupNameEmpty() {
    Assert.assertFalse(OptimizerGroupController.validateGroupName(""));
  }

  @Test
  public void testValidateGroupNameTooLong() {
    String tooLong = createString(51);
    Assert.assertFalse(OptimizerGroupController.validateGroupName(tooLong));
  }

  @Test
  public void testValidateGroupNameInvalidCharacters() {
    Assert.assertFalse(OptimizerGroupController.validateGroupName("invalid name"));
    Assert.assertFalse(OptimizerGroupController.validateGroupName("name@domain"));
    Assert.assertFalse(OptimizerGroupController.validateGroupName("name#hash"));
    Assert.assertFalse(OptimizerGroupController.validateGroupName("中文"));
    Assert.assertFalse(OptimizerGroupController.validateGroupName("name.with.dots"));
  }

  private String createString(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append('a');
    }
    return sb.toString();
  }
}
