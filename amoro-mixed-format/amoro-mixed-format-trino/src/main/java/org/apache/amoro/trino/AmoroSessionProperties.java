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

package org.apache.amoro.trino;

import static io.trino.spi.session.PropertyMetadata.booleanProperty;
import static io.trino.spi.session.PropertyMetadata.doubleProperty;

import io.trino.plugin.base.session.SessionPropertiesProvider;
import io.trino.plugin.iceberg.IcebergSessionProperties;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.session.PropertyMetadata;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.util.List;

/** Amoro supporting session properties */
public final class AmoroSessionProperties implements SessionPropertiesProvider {

  private static final String AMORO_STATISTICS_ENABLED = "amoro_table_statistics_enabled";

  private static final String AMORO_SPLIT_TASK_BY_DELETE_RATIO =
      "amoro_split_task_by_delete_ratio";
  private static final String AMORO_ENABLE_SPLIT_TASK_BY_DELETE_RATIO =
      "amoro_enable_split_task_by_delete_ratio";
  private final List<PropertyMetadata<?>> sessionProperties;

  @Inject
  public AmoroSessionProperties(
          AmoroConfig amoroConfig, IcebergSessionProperties icebergSessionProperties) {
    sessionProperties =
        ImmutableList.<PropertyMetadata<?>>builder()
            .addAll(icebergSessionProperties.getSessionProperties())
            .add(
                booleanProperty(
                        AMORO_STATISTICS_ENABLED,
                    "Expose table statistics for Amoro table",
                    amoroConfig.isTableStatisticsEnabled(),
                    false))
            .add(
                doubleProperty(
                        AMORO_SPLIT_TASK_BY_DELETE_RATIO,
                    "If task delete ratio less than this value will be split to more task",
                    amoroConfig.getSplitTaskByDeleteRatio(),
                    false))
            .add(
                booleanProperty(
                        AMORO_ENABLE_SPLIT_TASK_BY_DELETE_RATIO,
                    "Enable task split by ratio",
                    amoroConfig.isEnableSplitTaskByDeleteRatio(),
                    false))
            .build();
  }

  @Override
  public List<PropertyMetadata<?>> getSessionProperties() {
    return sessionProperties;
  }

  public static boolean isAmoroStatisticsEnabled(ConnectorSession session) {
    return session.getProperty(AMORO_STATISTICS_ENABLED, Boolean.class);
  }

  public static boolean enableSplitTaskByDeleteRatio(ConnectorSession session) {
    return session.getProperty(AMORO_ENABLE_SPLIT_TASK_BY_DELETE_RATIO, Boolean.class);
  }

  public static double splitTaskByDeleteRatio(ConnectorSession session) {
    return session.getProperty(AMORO_SPLIT_TASK_BY_DELETE_RATIO, Double.class);
  }
}
