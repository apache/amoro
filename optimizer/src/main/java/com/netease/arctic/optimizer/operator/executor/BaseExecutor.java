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

package com.netease.arctic.optimizer.operator.executor;

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public abstract class BaseExecutor<F extends ContentFile<F>> implements Executor<F> {
  protected Map<DataTreeNode, List<DataFile>> groupDataFilesByNode(List<DataFile> dataFiles) {
    return new HashMap<>(dataFiles.stream().collect(Collectors.groupingBy(dataFile ->
        DefaultKeyedFile.parseMetaFromFileName(dataFile.path().toString()).node())));
  }

  protected Map<DataTreeNode, List<DeleteFile>> groupDeleteFilesByNode(List<DeleteFile> deleteFiles) {
    return new HashMap<>(deleteFiles.stream().collect(Collectors.groupingBy(deleteFile ->
        DefaultKeyedFile.parseMetaFromFileName(deleteFile.path().toString()).node())));
  }

  protected long getMaxTransactionId(List<DataFile> dataFiles) {
    OptionalLong maxTransactionId = dataFiles.stream()
        .mapToLong(file -> DefaultKeyedFile.parseMetaFromFileName(file.path().toString()).transactionId()).max();
    if (maxTransactionId.isPresent()) {
      return maxTransactionId.getAsLong();
    }

    return 0;
  }
}
