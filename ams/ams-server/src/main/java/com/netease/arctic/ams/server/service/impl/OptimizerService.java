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
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OptimizerStateReport;
import com.netease.arctic.ams.server.mapper.OptimizerGroupMapper;
import com.netease.arctic.ams.server.mapper.OptimizerMapper;
import com.netease.arctic.ams.server.model.Container;
import com.netease.arctic.ams.server.model.Optimizer;
import com.netease.arctic.ams.server.model.OptimizerGroup;
import com.netease.arctic.ams.server.model.OptimizerGroupInfo;
import com.netease.arctic.ams.server.model.OptimizerResourceInfo;
import com.netease.arctic.ams.server.model.TableTaskStatus;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.optimizer.StatefulOptimizer;
import com.netease.arctic.optimizer.factory.OptimizerFactory;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.OptimizerProperties.OPTIMIZER_LAUNCHER_INFO;

public class OptimizerService extends IJDBCService implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerService.class);

  public List<Optimizer> getOptimizers(String optimizerGroup) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizersByGroupName(optimizerGroup);
    }
  }

  public List<Optimizer> getOptimizers() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizers();
    }
  }

  public Optimizer getOptimizer(Long optimizerId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
      return optimizerMapper.selectOptimizer(optimizerId);
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
    if (optimizer != null) {
      OptimizerGroupInfo optimizerGroupInfo = getOptimizerGroupInfo(optimizer.getGroupName());
      Container container = ServiceContainer.getContainerMetaService().getContainer(optimizerGroupInfo.getContainer());
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
      }
      try (SqlSession sqlSession = getSqlSession(true)) {
        OptimizerMapper optimizerMapper = getMapper(sqlSession, OptimizerMapper.class);
        Map<String, String> stateInfo = optimizer.getStateInfo();
        if (stateInfo != null) {
          stateInfo.putAll(reportData.optimizerState);
        } else {
          stateInfo = reportData.optimizerState;
        }
        optimizerMapper.updateOptimizerState(reportData.optimizerId, factory.serialize(instance),
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

  @Override
  public void close() throws IOException {

  }
}

