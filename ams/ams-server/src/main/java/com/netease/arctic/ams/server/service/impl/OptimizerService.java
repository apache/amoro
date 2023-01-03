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

package com.netease.arctic.ams.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OptimizerDescriptor;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizerStateReport;
import com.netease.arctic.ams.server.config.ConfigFileProperties;
import com.netease.arctic.ams.server.handler.impl.OptimizeManagerHandler;
import com.netease.arctic.ams.server.mapper.OptimizeTasksMapper;
import com.netease.arctic.ams.server.mapper.OptimizerGroupMapper;
import com.netease.arctic.ams.server.mapper.OptimizerMapper;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.Container;
import com.netease.arctic.ams.server.model.Optimizer;
import com.netease.arctic.ams.server.model.OptimizerGroup;
import com.netease.arctic.ams.server.model.OptimizerGroupInfo;
import com.netease.arctic.ams.server.model.OptimizerResourceInfo;
import com.netease.arctic.ams.server.model.TableTaskStatus;
import com.netease.arctic.ams.server.optimize.OptimizeTaskItem;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.optimizer.StatefulOptimizer;
import com.netease.arctic.optimizer.factory.OptimizerFactory;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.OptimizerProperties.OPTIMIZER_LAUNCHER_INFO;

public class OptimizerService extends IJDBCService {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerService.class);

  public List<Optimizer> getOptimizers(String optimizerGroup) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      List<Optimizer> optimizers = optimizerMapper.selectOptimizersByGroupName(optimizerGroup);
      optimizers.forEach(this::fillContainerType);
      return optimizers;
    }
  }

  public List<Optimizer> getOptimizers() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      List<Optimizer> optimizers = optimizerMapper.selectOptimizers();
      optimizers.forEach(this::fillContainerType);
      return optimizers;
    }
  }

  public Optimizer getOptimizer(Long optimizerId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizer(optimizerId);
    }
  }

  public Optimizer getOptimizer(String jobName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizerByName(jobName);
    }
  }

  public void deleteOptimizer(Long optimizerId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      optimizerMapper.deleteOptimizer(optimizerId);
    }
  }

  public void addOptimizerInstance(Long optimizerId, byte[] instance) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      optimizerMapper.addOptimizerInstance(optimizerId, instance);
    }
  }

  public void updateOptimizerState(OptimizerStateReport reportData) throws NoSuchObjectException {
    LOG.info("get report {}", reportData);
    if (reportData.optimizerState == null || reportData.optimizerState.isEmpty()) {
      return;
    }
    Optimizer optimizer = getOptimizer(reportData.optimizerId);
    byte[] optimizerinstance = null;
    if (optimizer != null) {
      OptimizerGroupInfo optimizerGroupInfo = getOptimizerGroupInfo(optimizer.getGroupName());
      Container container = ServiceContainer.getContainerMetaService().getContainer(optimizerGroupInfo.getContainer());

      checkOptimizerRetry(reportData,optimizer);

      if (!container.getType().equals(ConfigFileProperties.EXTERNAL_CONTAINER_TYPE)) {
        OptimizerFactory factory =
                ServiceContainer.getOptimizeExecuteService().findOptimizerFactory(container.getType());
        com.netease.arctic.optimizer.Optimizer instance = factory.deserialize(optimizer.getInstance());
        if (instance instanceof StatefulOptimizer) {
          Map<String, String> state = ((StatefulOptimizer) instance).getState();
          if (state.containsKey(OPTIMIZER_LAUNCHER_INFO)) {
            JSONObject old = JSONObject.parseObject(state.get(OPTIMIZER_LAUNCHER_INFO));
            old.putAll(reportData.optimizerState);
            state.put(OPTIMIZER_LAUNCHER_INFO, JSONObject.toJSONString(old));
          } else {
            state.put(OPTIMIZER_LAUNCHER_INFO, JSONObject.toJSONString(reportData.optimizerState));
          }
          ((StatefulOptimizer) instance).updateState(state);
          optimizerinstance = factory.serialize(instance);
        }
      }

      try (SqlSession sqlSession = getSqlSession(true)) {
        OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
        Map<String, String> stateInfo = optimizer.getStateInfo();
        if (stateInfo != null) {
          stateInfo.putAll(reportData.optimizerState);
        } else {
          stateInfo = reportData.optimizerState;
        }

        optimizerMapper.updateOptimizerState(reportData.optimizerId, optimizerinstance,
                stateInfo, TableTaskStatus.RUNNING.name());
      }
    }
  }

  public void updateOptimizerStatus(long optimizerId, TableTaskStatus status) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      optimizerMapper.updateOptimizerStatus(optimizerId, status.name());
    }
  }

  public List<OptimizerGroup> getOptimizerGroups() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerGroupMapper optimizerGroupMapper = getMapper(sqlSession, OptimizerGroupMapper.class);
      return optimizerGroupMapper.selectOptimzerGroups();
    }
  }

  public OptimizerResourceInfo getOptimizerGroupResourceInfo(String optimizerGroup) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizerGroupResourceInfoByGroupName(optimizerGroup);
    }
  }

  public OptimizerResourceInfo getOptimizerGroupsResourceInfo() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizerGroupResourceInfo();
    }
  }

  public OptimizerGroupInfo getOptimizerGroupInfo(String optimizerGroup) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerGroupMapper optimizerMapper = getMapper(sqlSession, OptimizerGroupMapper.class);
      return optimizerMapper.selectOptimizerGroupInfo(optimizerGroup);
    }
  }

  public List<OptimizerGroupInfo> getAllOptimizerGroupInfo() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerGroupMapper optimizerMapper = getMapper(sqlSession, OptimizerGroupMapper.class);
      return optimizerMapper.selectAllOptimizerGroupInfo();
    }
  }


  public void insertOptimizer(
          String optimizerName, int queueId, String queueName, TableTaskStatus status, String startTime,
          int coreNumber, long memory, int parallelism, String container) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      optimizerMapper.insertOptimizer(optimizerName, queueId, queueName, status, startTime, coreNumber, memory,
              parallelism, container);
    }
  }

  public String selectOptimizerIdByOptimizerName(String optimizerName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizerIdByOptimizerName(optimizerName);
    }
  }

  public void deleteOptimizerByName(String optimizerName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      optimizerMapper.deleteOptimizerByName(optimizerName);
    }
  }

  public OptimizerDescriptor registerOptimizer(OptimizerRegisterInfo registerInfo) throws InvalidObjectException {
    OptimizerGroupInfo optimizerGroupInfo = getOptimizerGroupInfo(registerInfo.getOptimizerGroupName());
    if (optimizerGroupInfo == null) {
      throw new InvalidObjectException("optimizer group not found");
    }
    String currentTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
    String optimizerName = "arctic_optimizer_" + currentTime;
    insertOptimizer(optimizerName, optimizerGroupInfo.getId(), optimizerGroupInfo.getName(),
            TableTaskStatus.STARTING, currentTime, registerInfo.getCoreNumber(), registerInfo.getMemorySize(),
            registerInfo.getCoreNumber(), optimizerGroupInfo.getContainer());
    return getOptimizer(optimizerName).convertToDescriptor();
  }

  private Optimizer fillContainerType(Optimizer optimizer) {
    if (optimizer == null) {
      return null;
    }
    OptimizerGroupInfo optimizerGroupInfo = getOptimizerGroupInfo(optimizer.getGroupName());
    Container container = ServiceContainer.getContainerMetaService().getContainer(optimizerGroupInfo.getContainer());
    optimizer.setContainerType(container.getType());
    return optimizer;
  }

  /**
   * If lastmodification is not null and is not the same as before, then the task has occurred retry and returns true.
   */
  private void checkOptimizerRetry(OptimizerStateReport newReportData,Optimizer oldOptimizer) {

    LOG.info("checkOptimizerRetry");
    Map<String, String> stateInfo = oldOptimizer.getStateInfo();
    if (stateInfo == null) {
      return;
    }
    String lastmodification = newReportData.optimizerState.get("last-modefication");
    if (lastmodification.equals(stateInfo.get("last-modefication")) && stateInfo.get("last-modefication") != null) {

      LOG.info("checkOptimizerRetry retry");
      //出问题，任务重试过
      //通过optimizer的id
      long optimizerId = newReportData.optimizerId;
      try (SqlSession sqlSession = getSqlSession(true)) {
        OptimizeTasksMapper optimizeTasksMapper = getMapper(sqlSession, OptimizeTasksMapper.class);
        //年月日时分秒
        List<BaseOptimizeTask> baseOptimizeTasks = optimizeTasksMapper.selectOptimizeTasksByJobID(optimizerId);
        for (BaseOptimizeTask baseOptimizeTask : baseOptimizeTasks) {

          long nowTimeStamp = System.currentTimeMillis();
          OptimizeTaskItem optimizeTaskItem = new OptimizeTaskItem(baseOptimizeTask,
                  new BaseOptimizeTaskRuntime(baseOptimizeTask.getTaskId()));
          optimizeTaskItem.onFailed(new ErrorMessage(nowTimeStamp,"optimizer job has occurred retry"),
                  nowTimeStamp - baseOptimizeTask.getCreateTime());
        }
      }

    }
    LOG.info("checkOptimizerRetry Finish");
  }
}

