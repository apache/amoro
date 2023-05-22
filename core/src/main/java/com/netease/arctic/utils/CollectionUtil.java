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

package com.netease.arctic.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class CollectionUtil {

  public static <A, B> List<Pair<A, B>> zip(Iterable<A> as, Iterable<B> bs) {
    Iterator<A> itA = as.iterator();
    Iterator<B> itB = bs.iterator();

    List<Pair<A, B>> zipResult = Lists.newArrayList();
    while (itA.hasNext()) {
      A a = itA.next();
      if (itB.hasNext()) {
        B b = itB.next();
        zipResult.add(Pair.of(a, b));
      } else {
        break;
      }
    }
    return zipResult;
  }
}
