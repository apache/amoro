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

package com.netease.arctic.hive;

import org.apache.hadoop.hive.metastore.PartitionDropOptions;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface HMSClient {

  void close();

  void reconnect() throws MetaException;

  List<String> getAllDatabases() throws TException;

  void alterPartition(
      String dbName, String tblName, Partition newPart, EnvironmentContext environmentContext)
      throws TException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
          IllegalAccessException;

  Partition getPartition(String dbName, String tblName, List<String> partVals) throws TException;

  Partition getPartition(String dbName, String tblName, String name) throws TException;

  Table getTable(String dbName, String tableName) throws TException;

  void alterTable(String defaultDatabaseName, String tblName, Table table) throws TException;

  List<Partition> listPartitions(String dbName, String tblName, short maxParts) throws TException;

  List<Partition> listPartitions(
      String dbName, String tblName, List<String> partVals, short maxParts) throws TException;

  List<String> listPartitionNames(String dbName, String tblName, short maxParts) throws TException;

  void createDatabase(Database db) throws TException;

  void dropDatabase(String name, boolean deleteData, boolean ignoreUnknownDb, boolean cascade)
      throws TException;

  void dropTable(String dbname, String tableName, boolean deleteData, boolean ignoreUnknownTab)
      throws TException;

  void createTable(Table tbl) throws TException;

  Database getDatabase(String databaseName) throws TException;

  Partition addPartition(Partition partition) throws TException;

  boolean dropPartition(
      String dbName, String tblName, List<String> partVals, PartitionDropOptions options)
      throws TException;

  int addPartitions(List<Partition> partitions) throws TException;

  List<String> getAllTables(String dbName) throws TException;

  void alterPartitions(
      String dbName,
      String tblName,
      List<Partition> newParts,
      EnvironmentContext environmentContext)
      throws TException, InstantiationException, IllegalAccessException, NoSuchMethodException,
          InvocationTargetException, ClassNotFoundException;
}
