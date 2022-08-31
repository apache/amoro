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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.model.AMSTransactionsOfTable;
import com.netease.arctic.ams.server.model.BaseMajorCompactRecord;
import com.netease.arctic.ams.server.model.CompactRangeType;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.TransactionsOfTable;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class AmsUtils {
  public static TableIdentifier toTableIdentifier(com.netease.arctic.table.TableIdentifier tableIdentifier) {
    if (tableIdentifier == null) {
      return null;
    }
    return new TableIdentifier(tableIdentifier.getCatalog(), tableIdentifier.getDatabase(),
            tableIdentifier.getTableName());
  }

  public static AMSTransactionsOfTable toTransactionsOfTable(
          TransactionsOfTable info) {
    AMSTransactionsOfTable transactionsOfTable = new AMSTransactionsOfTable();
    transactionsOfTable.setTransactionId(info.getTransactionId() + "");
    transactionsOfTable.setFileCount(info.getFileCount());
    transactionsOfTable.setFileSize(byteToXB(info.getFileSize()));
    transactionsOfTable.setCommitTime(info.getCommitTime());
    transactionsOfTable.setSnapshotId(info.getTransactionId() + "");
    return transactionsOfTable;
  }

  public static BaseMajorCompactRecord transferToBaseMajorCompactRecord(
          OptimizeHistory record) {
    BaseMajorCompactRecord result = new BaseMajorCompactRecord();
    result.setTableIdentifier(record.getTableIdentifier());
    result.setOptimizeType(record.getOptimizeType());
    result.setCompactRange(toCompactRange(record.getOptimizeRange()));
    result.setVisibleTime(record.getVisibleTime());
    result.setPlanTime(record.getPlanTime());
    result.setDuration(record.getDuration());
    result.setTotalFilesStatBeforeCompact(record.getTotalFilesStatBeforeOptimize());
    result.setTotalFilesStatAfterCompact(record.getTotalFilesStatAfterOptimize());
    result.setCommitTime(record.getCommitTime());
    result.setRecordId(record.getRecordId());
    return result;
  }

  private static CompactRangeType toCompactRange(com.netease.arctic.ams.api.OptimizeRangeType compactRange) {
    if (compactRange == null) {
      return null;
    }
    return CompactRangeType.valueOf(compactRange.name());
  }

  public static Long longOrNull(String value) {
    if (value == null) {
      return null;
    } else {
      return Long.parseLong(value);
    }
  }


  /**
   * Convert size to a different unit, ensuring that the converted value is > 1
   */
  public static String byteToXB(long size) {
    String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
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

  public static InetAddress getLocalHostExactAddress(String prefix) {
    if (prefix.startsWith("0")) {
      throw new RuntimeException("config " + ArcticMetaStoreConf.THRIFT_BIND_HOST_PREFIX.key() + " can't start with 0");
    }
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface iface = networkInterfaces.nextElement();
        for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
          InetAddress inetAddr = inetAddrs.nextElement();
          if (checkHostAddress(inetAddr, prefix)) {
            return inetAddr;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static boolean checkHostAddress(InetAddress address, String prefix) {
    return address != null && address.getHostAddress().startsWith(prefix);
  }

}
