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

package org.apache.amoro.utils.map;

import org.apache.lucene.util.RamUsageEstimator;

import java.io.Serializable;

public class DefaultSizeEstimator<T> implements SizeEstimator<T>, Serializable {
  @Override
  public long sizeEstimate(T t) {
    // RamUsageEstimator calculate shallow size for complex objects, which is enough for our use
    // case for now.
    // But it should be noticed that it is not a common way to calculate size of all java objects.
    return RamUsageEstimator.sizeOfObject(t, 0);
  }
}
