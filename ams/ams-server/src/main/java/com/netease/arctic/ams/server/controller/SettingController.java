package com.netease.arctic.ams.server.controller;

import com.netease.arctic.ams.server.controller.response.OkResponse;
import io.javalin.http.Context;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/25 17:28
 * @Description:
 */
public class SettingController {
  /**
   * get systemSetting
   * @param ctx
   */
  public static void getSystemSetting(Context ctx) {
    ctx.json(OkResponse.of(null));
  }

  /**
   * get container setting
   * @param ctx
   */
  public static void getContainerSetting(Context ctx) {
    ctx.json(OkResponse.of(null));
  }
}
