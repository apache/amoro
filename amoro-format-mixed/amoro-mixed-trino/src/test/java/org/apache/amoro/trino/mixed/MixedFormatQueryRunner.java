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

package org.apache.amoro.trino.mixed;

import static io.airlift.testing.Closeables.closeAllSuppress;
import static io.trino.testing.TestingSession.testSessionBuilder;
import static java.util.Objects.requireNonNull;

import io.airlift.log.Logger;
import io.trino.plugin.tpch.TpchPlugin;
import io.trino.testing.DistributedQueryRunner;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.trino.MixedFormatPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MixedFormatQueryRunner {
  public static final String MIXED_FORMAT_CATALOG = "test_mixed_format_catalog";

  public static final String MIXED_FORMAT_CATALOG_PREFIX = MIXED_FORMAT_CATALOG + ".";

  private MixedFormatQueryRunner() {}

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends DistributedQueryRunner.Builder<Builder> {
    private Optional<File> metastoreDirectory = Optional.empty();
    private ImmutableMap.Builder<String, String> icebergProperties = ImmutableMap.builder();

    protected Builder() {
      super(testSessionBuilder().setCatalog(MIXED_FORMAT_CATALOG).setSchema("tpch").build());
    }

    public Builder setMetastoreDirectory(File metastoreDirectory) {
      this.metastoreDirectory = Optional.of(metastoreDirectory);
      return self();
    }

    public Builder setIcebergProperties(Map<String, String> icebergProperties) {
      this.icebergProperties =
          ImmutableMap.<String, String>builder()
              .putAll(requireNonNull(icebergProperties, "icebergProperties is null"));
      return self();
    }

    public Builder addIcebergProperty(String key, String value) {
      this.icebergProperties.put(key, value);
      return self();
    }

    @Override
    public DistributedQueryRunner build() throws Exception {
      DistributedQueryRunner queryRunner = super.build();
      try {
        queryRunner.installPlugin(new TpchPlugin());
        queryRunner.createCatalog("tpch", "tpch");

        queryRunner.installPlugin(new MixedFormatPlugin());
        Map<String, String> icebergProperties =
            new HashMap<>(this.icebergProperties.buildOrThrow());
        queryRunner.createCatalog(MIXED_FORMAT_CATALOG, "mixed-format", icebergProperties);
        return queryRunner;
      } catch (Exception e) {
        closeAllSuppress(e, queryRunner);
        throw e;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    DistributedQueryRunner queryRunner = null;
    queryRunner =
        MixedFormatQueryRunner.builder()
            .setExtraProperties(ImmutableMap.of("http-server.http.port", "8080"))
            .build();
    Thread.sleep(10);
    Logger log = Logger.get(MixedFormatQueryRunner.class);
    log.info("======== SERVER STARTED ========");
    log.info("\n====\n%s\n====", queryRunner.getCoordinator().getBaseUrl());
  }
}
