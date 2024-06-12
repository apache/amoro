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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.PlatformFileManager;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** The controller that handles file requests. */
public class PlatformFileInfoController {

  private final PlatformFileManager platformFileInfoService;

  public PlatformFileInfoController(PlatformFileManager platformFileInfoService) {
    this.platformFileInfoService = platformFileInfoService;
  }

  /** Upload file. */
  public void uploadFile(Context ctx) throws IOException {
    InputStream bodyAsInputStream = ctx.uploadedFile("file").getContent();
    String name = ctx.uploadedFile("file").getFilename();
    byte[] bytes = IOUtils.toByteArray(bodyAsInputStream);

    // validate xml config
    if (name.toLowerCase().endsWith(".xml")) {
      try {
        Configuration configuration = new Configuration();
        configuration.addResource(new ByteArrayInputStream(bytes));
        configuration.setDeprecatedProperties();
      } catch (Exception e) {
        ctx.json(new ErrorResponse("Uploaded file is not in valid XML format"));
        return;
      }
    }
    String content = Base64.getEncoder().encodeToString(bytes);
    Integer fid = platformFileInfoService.addFile(name, content);
    Map<String, String> result = new HashMap<>();
    result.put("id", String.valueOf(fid));
    result.put("url", "/ams/v1/files/" + fid);
    ctx.json(OkResponse.of(result));
  }

  /** Download file. */
  public void downloadFile(Context ctx) {
    String fileId = ctx.pathParam("fileId");
    Preconditions.checkArgument(StringUtils.isNumeric(fileId), "Invalid file id");
    byte[] content = platformFileInfoService.getFileContentById(Integer.valueOf(fileId));
    ctx.result(content);
  }
}
