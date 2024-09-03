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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.Constants;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AMSUtil provides utility methods for working with AMS (Amoro Management Service) related
 * operations.
 */
public class AmsUtil {

  private static final String ZOOKEEPER_ADDRESS_FORMAT = "zookeeper://%s/%s";
  private static final String THRIFT_ADDRESS_FORMAT = "thrift://%s:%s";

  public static TableIdentifier toTableIdentifier(
      org.apache.amoro.table.TableIdentifier tableIdentifier) {
    if (tableIdentifier == null) {
      return null;
    }
    return new TableIdentifier(
        tableIdentifier.getCatalog(),
        tableIdentifier.getDatabase(),
        tableIdentifier.getTableName());
  }

  public static Long longOrNull(String value) {
    if (value == null) {
      return null;
    } else {
      return Long.parseLong(value);
    }
  }

  /** Convert size to a different unit, ensuring that the converted value is > 1 */
  public static String byteToXB(long size) {
    String[] units = new String[] {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    float result = size, tmpResult = size;
    int unitIdx = 0;
    int unitCnt = units.length;
    while (true) {
      result = result / 1024;
      if (result < 1 || unitIdx >= unitCnt - 1) {
        return String.format("%2.2f%s", tmpResult, units[unitIdx]);
      }
      tmpResult = result;
      unitIdx += 1;
    }
  }

  public static String getFileName(String path) {
    return path == null ? null : new File(path).getName();
  }

  public static Map<String, String> getNotDeprecatedAndNotInternalStaticFields(Class<?> clazz)
      throws IllegalAccessException {

    List<Field> fields =
        Arrays.stream(clazz.getDeclaredFields())
            // filter out the non-static fields
            .filter(f -> Modifier.isStatic(f.getModifiers()))
            .filter(f -> f.getAnnotation(Deprecated.class) == null)
            // collect to list
            .collect(Collectors.toList());

    Map<String, String> result = new HashMap<>();
    for (Field field : fields) {
      result.put(field.getName(), field.get(null) + "");
    }
    return result;
  }

  public static String getStackTrace(Throwable throwable) {
    StringWriter sw = new StringWriter();

    try (PrintWriter printWriter = new PrintWriter(sw)) {
      throwable.printStackTrace(printWriter);
      return sw.toString();
    }
  }

  public static InetAddress lookForBindHost(String prefix) {
    if (prefix.startsWith("0")) {
      throw new RuntimeException(
          "config " + AmoroManagementConf.SERVER_EXPOSE_HOST.key() + " can't start with 0");
    }
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        for (Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
            enumeration.hasMoreElements(); ) {
          InetAddress inetAddress = enumeration.nextElement();
          if (checkHostAddress(inetAddress, prefix)) {
            return inetAddress;
          }
        }
      }
      throw new IllegalArgumentException("Can't find host address start with " + prefix);
    } catch (Exception e) {
      throw new RuntimeException("Look for bind host failed", e);
    }
  }

  public static String getAMSThriftAddress(Configurations conf, String serviceName) {
    if (conf.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      return String.format(
          ZOOKEEPER_ADDRESS_FORMAT,
          conf.getString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS),
          conf.getString(AmoroManagementConf.HA_CLUSTER_NAME));
    } else {
      if (Constants.THRIFT_TABLE_SERVICE_NAME.equals(serviceName)) {
        return String.format(
            THRIFT_ADDRESS_FORMAT,
            conf.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
            conf.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT));
      } else if (Constants.THRIFT_OPTIMIZING_SERVICE_NAME.equals(serviceName)) {
        return String.format(
            THRIFT_ADDRESS_FORMAT,
            conf.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
            conf.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT));
      } else {
        throw new IllegalArgumentException(String.format("Unknown service name %s", serviceName));
      }
    }
  }

  public static String formatString(String beforeFormat) {
    StringBuilder result = new StringBuilder();
    int flag = 0;
    for (int i = 0; i < beforeFormat.length(); i++) {
      String s = String.valueOf(beforeFormat.toCharArray()[i]);
      if (!s.matches("[a-zA-Z]+")) {
        flag = 0;
        result.append(" ");
      } else {
        result.append(flag == 0 ? s.toUpperCase() : s.toLowerCase());
        flag++;
      }
    }
    return result.toString();
  }

  private static boolean checkHostAddress(InetAddress address, String prefix) {
    return address != null && address.getHostAddress().startsWith(prefix);
  }
}
