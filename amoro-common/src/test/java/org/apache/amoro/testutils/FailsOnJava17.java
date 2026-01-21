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

package org.apache.amoro.testutils;

/**
 * Marker interface for tests that fail on Java 17.
 *
 * <p>Tests annotated with this interface will be excluded when running on Java 17. Use this for
 * tests that depend on internal JDK APIs that have been removed or restricted in Java 17.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @Category(FailsOnJava17.class)
 * public class MyTest {
 *     // test code
 * }
 * }</pre>
 */
public interface FailsOnJava17 {}
