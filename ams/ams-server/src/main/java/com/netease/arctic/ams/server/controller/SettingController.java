package com.netease.arctic.ams.server.controller;

import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.model.OptimizerGroup;
import com.netease.arctic.ams.server.optimize.IOptimizeService;
import com.netease.arctic.ams.server.optimize.OptimizeService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/25 17:28
 * @Description:
 */
public class SettingController extends RestBaseController {
  /**
   * get systemSetting
   *
   * @param ctx
   */
  public static void getSystemSetting(Context ctx) {
    ctx.json(OkResponse.of(ArcticMetaStore.getSystemSettingFromYaml()));
  }
}
