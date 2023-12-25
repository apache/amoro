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

package com.netease.arctic.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/** Util class to encode configuration files with base64 */
public class ConfigurationFileUtil {
  /**
   * encode target file with base64, such as krb5.conf、**.keytab files
   *
   * @param filePath target file path
   * @return file content with base64 encode
   * @throws IOException if an error occurs while reading
   */
  public static String encodeConfigurationFileWithBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return null;
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }

  /**
   * encode target xml file with base64, such as core-site.xml、hdfs-site.xml files
   *
   * @param filePath target file path
   * @return file content with base64 encode
   * @throws IOException if an error occurs while reading
   */
  public static String encodeXmlConfigurationFileWithBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return Base64.getEncoder()
          .encodeToString("<configuration></configuration>".getBytes(StandardCharsets.UTF_8));
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }
}
