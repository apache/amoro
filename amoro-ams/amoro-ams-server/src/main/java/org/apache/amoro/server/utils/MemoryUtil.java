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

package org.apache.amoro.server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This is a memory converter util. */
public class MemoryUtil {
    private static final Pattern MEMORY_PATTERN = Pattern.compile("(\\d+)([kKmMgG]?)");

    // Convert memory string to megabytes (m)
    public static long convertToMegabytes(String memoryString) {
        Matcher matcher = MEMORY_PATTERN.matcher(memoryString.trim());
        if (matcher.matches()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            switch (unit) {
                case "":
                case "m":
                    return value;
                case "k":
                    return value / 1024;
                case "g":
                    return value * 1024;
                default:
                    throw new IllegalArgumentException("Invalid memory unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("Invalid memory string: " + memoryString);
        }
    }

}
