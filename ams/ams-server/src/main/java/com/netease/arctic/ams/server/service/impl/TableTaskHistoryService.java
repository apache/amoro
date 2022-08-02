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

import com.netease.arctic.ams.server.mapper.TaskHistoryMapper;
import com.netease.arctic.ams.server.model.TableTaskHistory;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.ITableTaskHistoryService;
import com.netease.arctic.table.TableIdentifier;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class TableTaskHistoryService extends IJDBCService implements ITableTaskHistoryService {

  public TableTaskHistoryService() {
    super();
  }

  @Override
  public List<TableTaskHistory> selectTaskHistory(TableIdentifier identifier, String historyId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      return taskHistoryMapper.selectTaskHistory(identifier, historyId);
    }
  }

  @Override
  public void insertTaskHistory(TableTaskHistory taskHistory) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);
      taskHistoryMapper.insertTaskHistory(taskHistory);
    }
  }

  @Override
  public void updateTaskHistory(TableTaskHistory taskHistory) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);
      taskHistoryMapper.updateTaskHistory(taskHistory);
    }
  }

  @Override
  public List<TableTaskHistory> selectTaskHistoryByQueueIdAndTime(int queueId, long startTime, long endTime) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      return taskHistoryMapper.selectTaskHistoryByQueueIdAndTime(queueId, startTime, endTime);
    }
  }

  @Override
  public List<TableTaskHistory> selectTaskHistoryByTime(long startTime, long endTime) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      return taskHistoryMapper.selectTaskHistoryByTime(startTime, endTime);
    }
  }

  @Override
  public List<TableTaskHistory> selectTaskHistoryByTableIdAndTime(TableIdentifier identifier,
                                                                  long startTime,
                                                                  long endTime) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      return taskHistoryMapper.selectTaskHistoryByTableIdAndTime(identifier, startTime, endTime);
    }
  }

  @Override
  public void deleteTaskHistory(TableIdentifier identifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      taskHistoryMapper.deleteTaskHistory(identifier);
    }
  }

  @Override
  public void deleteTaskHistoryWithHistoryId(TableIdentifier identifier, String taskHistoryId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper =
          getMapper(sqlSession, TaskHistoryMapper.class);

      taskHistoryMapper.deleteTaskHistoryWithHistoryId(identifier, taskHistoryId);
    }
  }
}
