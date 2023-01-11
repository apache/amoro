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

import com.netease.arctic.TransactionSequence;
import com.netease.arctic.table.KeyedTable;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class TransactionUtil {
  private TransactionUtil() {
  }

  public static TransactionSequence getCurrentTransactionSequence(KeyedTable keyedTable) {
    Snapshot changeSnapshot = keyedTable.changeTable().currentSnapshot();
    Snapshot baseSnapshot = keyedTable.baseTable().currentSnapshot();
    return TransactionSequence.of(changeSnapshot == null ? 0L : changeSnapshot.sequenceNumber(),
        baseSnapshot == null ? 0L : baseSnapshot.sequenceNumber());
  }

  public static TransactionSequence getTransactionSequence(String formattedSequence) {
    Preconditions.checkArgument(StringUtils.isNotBlank(formattedSequence),
        "formattedSequence should not be blank " + formattedSequence);
    formattedSequence = formattedSequence.trim();
    String[] split = formattedSequence.split("_");
    if (split.length == 1) {
      long id = Long.parseLong(split[0]);
      if (id == 0) {
        return null;
      } else {
        return TransactionSequence.of(id, TransactionSequence.VOID_SEQUENCE_NUMBER);
      }
    } else if (split.length == 2) {
      return TransactionSequence.of(Long.parseLong(split[0]), Long.parseLong(split[1]));
    } else {
      throw new IllegalArgumentException("illegal sequence format " + formattedSequence);
    }
  }

  public static String formatTransactionSequence(TransactionSequence transactionSequence) {
    if (transactionSequence == null) {
      return "0";
    }
    if (transactionSequence.getBaseSequence() == TransactionSequence.VOID_SEQUENCE_NUMBER) {
      return transactionSequence.getChangeSequence() + "";
    }
    return transactionSequence.getChangeSequence() + "_" + transactionSequence.getBaseSequence();
  }
}
