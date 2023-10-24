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

package com.netease.arctic.server.dashboard.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParamSignatureCalculator {
  public static final Logger LOG = LoggerFactory.getLogger(ParamSignatureCalculator.class);

  public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  public static String getMD5(byte[] bytes) {
    char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    char[] str = new char[16 * 2];
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      md.update(bytes);
      byte[] tmp = md.digest();
      int k = 0;
      for (int i = 0; i < 16; i++) {
        byte byte0 = tmp[i];
        str[k++] = hexDigits[byte0 >>> 4 & 0xf];
        str[k++] = hexDigits[byte0 & 0xf];
      }
    } catch (Exception e) {
      LOG.error("failed", e);
    }
    return new String(str);
  }

  /**
   * Calculates the MD5 hash of the given value.
   *
   * @param value the input value to calculate the MD5 hash
   * @return the MD5 hash of the given value as a string
   */
  public static String getMD5(String value) {
    String result = "";
    try {
      result = getMD5(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.error("get MD5 Error!!", e);
    }
    return result;
  }

  /**
   * Arranges argument pairs in ascending order, removes null values and concatenates them
   * Example: params: name=&value=111&age=11&sex=1&high=180&nick=
   * The result is: age11high180sex1value111
   *
   * @param map The map to process
   * @return The resulting string
   */
  public static String generateParamStringWithValueList(Map<String, List<String>> map) {
    return map.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> {
          List<String> sortedValues = entry.getValue().stream()
              .filter(value -> !StringUtils.isBlank(value))
              .sorted()
              .collect(Collectors.toList());

          try {
            return sortedValues.isEmpty() ? "" : entry.getKey() + URLDecoder.decode(sortedValues.get(0), "utf-8");
          } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to calculate signature", e);
            return null;
          }
        })
        .collect(Collectors.joining());
  }

  /**
   * Gets an ascending KeyValue concatenation string based on
   * the request argument pair.
   * <p>
   * Example:
   * params: name=&value=111&age=11&sex=1&high=180&nick=
   * Remove null and arrange in ascending order: age11high180sex1value111
   *
   * @param map input key-value pairs
   * @return an ascending, concatenated string without nulls; null if error occurs.
   */
  public static String generateParamStringWithValue(Map<String, String> map) {
    return map.keySet().stream()
        .sorted()
        .map(key -> {
          if (StringUtils.isNotBlank(map.get(key))) {
            try {
              return key + URLDecoder.decode(map.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
              LOG.error("Failed to decode url value: " + map.get(key), e);
            }
          }
          return null;
        })
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining());
  }
}
