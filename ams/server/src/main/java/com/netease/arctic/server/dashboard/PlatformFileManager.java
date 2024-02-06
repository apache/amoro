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

package com.netease.arctic.server.dashboard;

import com.netease.arctic.server.dashboard.model.PlatformFileInfo;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.PlatformFileMapper;

import java.util.Base64;

public class PlatformFileManager extends PersistentBase {

  /** Add a new file. */
  public Integer addFile(String name, String content) {
    PlatformFileInfo platformFileInfo = new PlatformFileInfo(name, content);
    doAs(PlatformFileMapper.class, e -> e.addFile(platformFileInfo));
    return platformFileInfo.getFileId();
  }

  /** Get the content of a file encoded in base64. */
  public String getFileContentB64ById(Integer fileId) {
    return getAs(PlatformFileMapper.class, e -> e.getFileById(fileId));
  }

  /** Get the content of a file. */
  public byte[] getFileContentById(Integer fileId) {
    String fileContent = getAs(PlatformFileMapper.class, e -> e.getFileById(fileId));
    return Base64.getDecoder().decode(fileContent);
  }
}
