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

package org.apache.amoro.flink.read.hybrid.reader;

import org.apache.amoro.flink.read.hybrid.split.AmoroSplit;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.iceberg.io.CloseableIterator;

import java.io.Serializable;
import java.util.function.Function;

/**
 * This function that accepts one {@link AmoroSplit} and produces an iterator of {@link
 * AmoroRecordWithOffset <T>}.
 */
@FunctionalInterface
public interface ReaderFunction<T>
    extends Serializable,
        Function<AmoroSplit, CloseableIterator<RecordsWithSplitIds<AmoroRecordWithOffset<T>>>> {}
