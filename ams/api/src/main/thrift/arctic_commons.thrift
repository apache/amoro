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

namespace java com.netease.arctic.ams.api

/**
* General definition of the arctic thrift interface.
* This file defines the type definitions that all of arctic's multiple thrift services depend on.
**/

exception AlreadyExistsException {
  1: string message
}

exception InvalidObjectException {
  1: string message
}

exception NoSuchObjectException {
  1: string message
}

exception MetaException {
  1: string message
}

exception NotSupportedException {
  1: string message
}

exception OperationConflictException {
  1: string message
}

exception ArcticException {
  1: i32 errorCode
  2: string errorName
  3: string message
}

struct TableIdentifier {
    1:string catalog;
    2:string database;
    3:string tableName;
}

// inner class begin

struct ColumnInfo {
    1:optional i32 id;
    2:string name;
    3:optional string type;
    4:optional string doc;
    5:bool isOptional;
}

struct Schema {
    1:list<ColumnInfo> columns;
    2:optional list<ColumnInfo> pks;
    3:optional list<ColumnInfo> partitionColumns;
    4:optional list<ColumnInfo> sortColumns;
}



