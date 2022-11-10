/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.terminal;

import com.netease.arctic.ams.server.config.ConfigOption;
import com.netease.arctic.ams.server.config.ConfigOptions;
import com.netease.arctic.ams.server.config.Configuration;
import com.netease.arctic.table.TableMetaStore;
import java.util.List;

/**
 * factory to create a TerminalSession
 */
public interface TerminalSessionFactory {

  /**
   * this will be called after factory is created.
   * @param properties - terminal properties and factory properties.
   */
  void initialize(Configuration properties);

  /**
   * create a new session
   * @param metaStore - auth info
   * @param configuration - configuration of session, all properties are defined in {@link SessionConfigOptions}
   * @return - new terminal context
   */
  TerminalSession create(TableMetaStore metaStore, Configuration configuration);

  class SessionConfigOptions {
    public static ConfigOption<Integer> FETCH_SIZE = ConfigOptions
        .key("session.fetch-size")
        .intType()
        .defaultValue(1000);

    public static ConfigOption<List<String>> CATALOGS = ConfigOptions
        .key("session.catalogs")
        .stringType()
        .asList()
        .noDefaultValue();

    public static ConfigOption<String> catalogType(String catalog){
      return ConfigOptions.key("catalog." + catalog + ".type")
          .stringType()
          .noDefaultValue();
    }

    public static ConfigOption<String> catalogUrl(String catalog){
      return ConfigOptions.key("catalog." + catalog + ".url")
          .stringType()
          .noDefaultValue();
    }
  }

  ConfigOption<Integer> FETCH_SIZE = ConfigOptions.key("fetch-size")
      .intType()
      .defaultValue(1000);
}
