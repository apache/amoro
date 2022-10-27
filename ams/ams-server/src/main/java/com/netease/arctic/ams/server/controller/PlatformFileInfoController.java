package com.netease.arctic.ams.server.controller;

import com.netease.arctic.ams.server.controller.response.ErrorResponse;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.PlatformFileInfoService;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.ws.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/26 10:36
 * @Description:
 */
public class PlatformFileInfoController extends RestBaseController {

  private static PlatformFileInfoService platformFileInfoService = ServiceContainer.getPlatformFileInfoService();

  /**
   * uplaod file
   *
   * @param ctx
   */
  public static void uploadFile(Context ctx) {
    try {
      InputStream bodyAsInputStream = ctx.uploadedFile("files").getContent();
      //todo get file name
      String name = ctx.headerMap().get("file-name");
      byte[] bytes = IOUtils.toByteArray(bodyAsInputStream);
      String content = Base64.getEncoder().encodeToString(bytes);
      platformFileInfoService.addFile(name, content);
    } catch (IOException e) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to upload file", null));
    }
    ctx.json(OkResponse.of(null));
  }

  /**
   * download file
   *
   * @param ctx
   */
  public static void downloadFile(Context ctx) {
    String fileId = ctx.pathParam("fileId");
    if (!StringUtils.isNumeric(fileId)) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Invalid file id", null));
      return;
    }
    String content = platformFileInfoService.getFileContentById(Integer.valueOf(fileId));
    ctx.json(OkResponse.of(content));
  }
}
