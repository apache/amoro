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

package org.apache.amoro.flink.table;

import org.apache.amoro.TableFormat;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.Factory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.util.Preconditions;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * UnifiedDynamicTableFactory is a factory for creating dynamic table sources and sinks. It
 * implements both DynamicTableSourceFactory and DynamicTableSinkFactory interfaces.
 */
public class UnifiedDynamicTableFactory
    implements DynamicTableSourceFactory, DynamicTableSinkFactory {

  private final Map<TableFormat, AbstractCatalog> availableCatalogs;

  public UnifiedDynamicTableFactory(Map<TableFormat, AbstractCatalog> availableCatalogs) {
    this.availableCatalogs =
        Preconditions.checkNotNull(availableCatalogs, "availableCatalogs cannot be null");
  }

  @Override
  public DynamicTableSink createDynamicTableSink(Context context) {
    ObjectIdentifier identifier = context.getObjectIdentifier();
    FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
    Configuration options = (Configuration) helper.getOptions();
    TableFormat tableFormat = TableFormat.valueOf(options.get(MixedFormatValidator.TABLE_FORMAT));

    return getOriginalCatalog(tableFormat)
        .flatMap(AbstractCatalog::getFactory)
        .filter(factory -> factory instanceof DynamicTableSinkFactory)
        .map(factory -> ((DynamicTableSinkFactory) factory).createDynamicTableSink(context))
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    String.format(
                        "Invalid catalog or factory for table format: %s， table: %s.",
                        tableFormat, identifier)));
  }

  @Override
  public DynamicTableSource createDynamicTableSource(Context context) {
    ObjectIdentifier identifier = context.getObjectIdentifier();
    FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
    Configuration options = (Configuration) helper.getOptions();
    TableFormat tableFormat = TableFormat.valueOf(options.get(MixedFormatValidator.TABLE_FORMAT));

    return getOriginalCatalog(tableFormat)
        .flatMap(AbstractCatalog::getFactory)
        .filter(factory -> factory instanceof DynamicTableSourceFactory)
        .map(factory -> ((DynamicTableSourceFactory) factory).createDynamicTableSource(context))
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    String.format(
                        "Invalid catalog or factory for table format: %s， table: %s.",
                        tableFormat, identifier)));
  }

  private Optional<AbstractCatalog> getOriginalCatalog(TableFormat format) {
    return Optional.of(availableCatalogs.get(format));
  }

  @Override
  public String factoryIdentifier() {
    return CatalogFactoryOptions.UNIFIED_IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    Set<ConfigOption<?>> requiredOptions = Sets.newHashSet();
    availableCatalogs.forEach(
        (format, catalog) -> {
          Optional<Factory> factory = catalog.getFactory();
          factory.ifPresent(value -> requiredOptions.addAll(value.requiredOptions()));
        });
    requiredOptions.add(MixedFormatValidator.TABLE_FORMAT);
    return requiredOptions;
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    Set<ConfigOption<?>> optionalOptions = Sets.newHashSet();
    availableCatalogs.forEach(
        (format, catalog) -> {
          Optional<Factory> factory = catalog.getFactory();
          factory.ifPresent(value -> optionalOptions.addAll(value.optionalOptions()));
        });
    return optionalOptions;
  }
}
