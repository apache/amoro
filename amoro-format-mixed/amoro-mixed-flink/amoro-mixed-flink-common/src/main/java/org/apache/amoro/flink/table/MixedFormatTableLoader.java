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

import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.interceptor.FlinkTablePropertiesInvocationHandler;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ExpireSnapshots;
import org.apache.iceberg.ManageSnapshots;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.util.StructLikeMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** load a proxy table contains both mixed-format table properties and flink table properties */
public class MixedFormatTableLoader implements TableLoader {

  private static final long serialVersionUID = 1L;

  protected final InternalCatalogBuilder catalogBuilder;
  protected final TableIdentifier tableIdentifier;
  protected final Map<String, String> flinkTableProperties;
  /**
   * The mark of loading internal table, base or change table. For compatible with iceberg
   * committer.
   */
  protected boolean loadBaseForKeyedTable;

  protected transient MixedFormatCatalog mixedFormatCatalog;

  public static MixedFormatTableLoader of(
      TableIdentifier tableIdentifier, InternalCatalogBuilder catalogBuilder) {
    return of(tableIdentifier, catalogBuilder, new HashMap<>());
  }

  public static MixedFormatTableLoader of(
      TableIdentifier tableIdentifier,
      InternalCatalogBuilder catalogBuilder,
      Map<String, String> flinkTableProperties) {
    return new MixedFormatTableLoader(tableIdentifier, catalogBuilder, flinkTableProperties);
  }

  public static MixedFormatTableLoader of(
      TableIdentifier tableIdentifier, Map<String, String> flinkTableProperties) {
    String metastoreUri = flinkTableProperties.get(CatalogFactoryOptions.AMS_URI.key());
    return new MixedFormatTableLoader(
        tableIdentifier,
        InternalCatalogBuilder.builder().amsUri(metastoreUri),
        flinkTableProperties);
  }

  public static MixedFormatTableLoader of(
      TableIdentifier tableIdentifier,
      String metastoreUri,
      Map<String, String> flinkTableProperties) {
    return new MixedFormatTableLoader(
        tableIdentifier,
        InternalCatalogBuilder.builder().amsUri(metastoreUri),
        flinkTableProperties);
  }

  protected MixedFormatTableLoader(
      TableIdentifier tableIdentifier,
      InternalCatalogBuilder catalogBuilder,
      Map<String, String> flinkTableProperties) {
    this(tableIdentifier, catalogBuilder, flinkTableProperties, null);
  }

  protected MixedFormatTableLoader(
      TableIdentifier tableIdentifier,
      InternalCatalogBuilder catalogBuilder,
      Map<String, String> flinkTableProperties,
      Boolean loadBaseForKeyedTable) {
    this.catalogBuilder = catalogBuilder;
    this.tableIdentifier = tableIdentifier;
    this.flinkTableProperties = new HashMap<>(flinkTableProperties);
    this.loadBaseForKeyedTable = loadBaseForKeyedTable == null || loadBaseForKeyedTable;
  }

  @Override
  public void open() {
    mixedFormatCatalog = catalogBuilder.build();
  }

  @Override
  public boolean isOpen() {
    return mixedFormatCatalog != null;
  }

  public MixedTable loadMixedFormatTable() {
    MixedTable table = mixedFormatCatalog.loadTable(tableIdentifier);
    return wrapWithFlinkTableProperties(table, flinkTableProperties);
  }

  public void switchLoadInternalTableForKeyedTable(boolean loadBaseForKeyedTable) {
    this.loadBaseForKeyedTable = loadBaseForKeyedTable;
  }

  @Override
  public Table loadTable() {
    MixedTable table = loadMixedFormatTable();

    if (table.isKeyedTable()) {
      if (loadBaseForKeyedTable) {
        return table.asKeyedTable().baseTable();
      } else {
        return table.asKeyedTable().changeTable();
      }
    }
    if (!(table instanceof Table)) {
      throw new UnsupportedOperationException(
          String.format("table type mismatched. It's %s", table.getClass()));
    }
    return (Table) table;
  }

  @Override
  public TableLoader clone() {
    return new MixedFormatTableLoader(
        tableIdentifier, catalogBuilder, flinkTableProperties, loadBaseForKeyedTable);
  }

  public MixedFormatTableLoader copyWithFlinkTableProperties(Map<String, String> extraProperties) {
    Map<String, String> merged = new HashMap<>(flinkTableProperties);
    merged.putAll(extraProperties);
    return new MixedFormatTableLoader(
        tableIdentifier, catalogBuilder, merged, loadBaseForKeyedTable);
  }

  @Override
  public void close() throws IOException {}

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("tableIdentifier", tableIdentifier).toString();
  }

  @VisibleForTesting
  static MixedTable wrapWithFlinkTableProperties(
      MixedTable mixedTable, Map<String, String> flinkTableProperties) {
    if (flinkTableProperties == null || flinkTableProperties.isEmpty()) {
      return mixedTable;
    }

    if (mixedTable instanceof SupportHive) {
      return (MixedTable)
          new FlinkTablePropertiesInvocationHandler(flinkTableProperties, mixedTable).getProxy();
    }

    if (mixedTable.isUnkeyedTable()) {
      return new FlinkTablePropertiesUnkeyedTable(
          mixedTable.asUnkeyedTable(), flinkTableProperties);
    }

    if (mixedTable.isKeyedTable()) {
      return new FlinkTablePropertiesKeyedTable(mixedTable.asKeyedTable(), flinkTableProperties);
    }

    return mixedTable;
  }

  private static class FlinkTablePropertiesSupport implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final Map<String, String> flinkTableProperties;

    protected FlinkTablePropertiesSupport(Map<String, String> flinkTableProperties) {
      this.flinkTableProperties = new HashMap<>(flinkTableProperties);
    }

    protected Map<String, String> withFlinkTableProperties(Map<String, String> tableProperties) {
      Map<String, String> merged = new HashMap<>(tableProperties);
      merged.putAll(flinkTableProperties);
      return merged;
    }
  }

  private static class FlinkTablePropertiesUnkeyedTable extends BasicUnkeyedTable
      implements UnkeyedTable {
    private static final long serialVersionUID = 1L;

    private final UnkeyedTable delegate;
    private final FlinkTablePropertiesSupport propertiesSupport;

    private FlinkTablePropertiesUnkeyedTable(
        UnkeyedTable delegate, Map<String, String> flinkTableProperties) {
      super(delegate.id(), delegate, delegate.io(), null);
      this.delegate = delegate;
      this.propertiesSupport = new FlinkTablePropertiesSupport(flinkTableProperties);
    }

    @Override
    public Map<String, String> properties() {
      return propertiesSupport.withFlinkTableProperties(delegate.properties());
    }

    @Override
    public void refresh() {
      delegate.refresh();
    }

    @Override
    public UpdateSchema updateSchema() {
      return delegate.updateSchema();
    }

    @Override
    public AppendFiles newAppend() {
      return delegate.newAppend();
    }

    @Override
    public AppendFiles newFastAppend() {
      return delegate.newFastAppend();
    }

    @Override
    public RewriteFiles newRewrite() {
      return delegate.newRewrite();
    }

    @Override
    public OverwriteFiles newOverwrite() {
      return delegate.newOverwrite();
    }

    @Override
    public RowDelta newRowDelta() {
      return delegate.newRowDelta();
    }

    @Override
    public ReplacePartitions newReplacePartitions() {
      return delegate.newReplacePartitions();
    }

    @Override
    public DeleteFiles newDelete() {
      return delegate.newDelete();
    }

    @Override
    public ExpireSnapshots expireSnapshots() {
      return delegate.expireSnapshots();
    }

    @Override
    public ManageSnapshots manageSnapshots() {
      return delegate.manageSnapshots();
    }

    @Override
    public Transaction newTransaction() {
      return delegate.newTransaction();
    }

    @Override
    public StructLikeMap<Map<String, String>> partitionProperty() {
      return delegate.partitionProperty();
    }

    @Override
    public org.apache.amoro.op.UpdatePartitionProperties updatePartitionProperties(
        Transaction transaction) {
      return delegate.updatePartitionProperties(transaction);
    }
  }

  private static class FlinkTablePropertiesKeyedTable extends BasicKeyedTable
      implements KeyedTable {
    private static final long serialVersionUID = 1L;

    private final KeyedTable delegate;
    private final FlinkTablePropertiesSupport propertiesSupport;

    private FlinkTablePropertiesKeyedTable(
        KeyedTable delegate, Map<String, String> flinkTableProperties) {
      super(
          delegate.location(),
          delegate.primaryKeySpec(),
          new FlinkTablePropertiesBaseTable(delegate.baseTable(), flinkTableProperties),
          new FlinkTablePropertiesChangeTable(delegate.changeTable(), flinkTableProperties));
      this.delegate = delegate;
      this.propertiesSupport = new FlinkTablePropertiesSupport(flinkTableProperties);
    }

    @Override
    public Map<String, String> properties() {
      return propertiesSupport.withFlinkTableProperties(delegate.properties());
    }

    @Override
    public void refresh() {
      delegate.refresh();
    }
  }

  private static class FlinkTablePropertiesBaseTable extends BasicKeyedTable.BaseInternalTable {
    private static final long serialVersionUID = 1L;

    private final UnkeyedTable delegate;
    private final FlinkTablePropertiesSupport propertiesSupport;

    private FlinkTablePropertiesBaseTable(
        UnkeyedTable delegate, Map<String, String> flinkTableProperties) {
      super(delegate.id(), delegate, delegate.io(), null);
      this.delegate = delegate;
      this.propertiesSupport = new FlinkTablePropertiesSupport(flinkTableProperties);
    }

    @Override
    public Map<String, String> properties() {
      return propertiesSupport.withFlinkTableProperties(delegate.properties());
    }

    @Override
    public void refresh() {
      delegate.refresh();
    }
  }

  private static class FlinkTablePropertiesChangeTable extends BasicKeyedTable.ChangeInternalTable {
    private static final long serialVersionUID = 1L;

    private final ChangeTable delegate;
    private final FlinkTablePropertiesSupport propertiesSupport;

    private FlinkTablePropertiesChangeTable(
        ChangeTable delegate, Map<String, String> flinkTableProperties) {
      super(delegate.id(), delegate, delegate.io(), null);
      this.delegate = delegate;
      this.propertiesSupport = new FlinkTablePropertiesSupport(flinkTableProperties);
    }

    @Override
    public Map<String, String> properties() {
      return propertiesSupport.withFlinkTableProperties(delegate.properties());
    }

    @Override
    public void refresh() {
      delegate.refresh();
    }

    @Override
    public org.apache.amoro.scan.ChangeTableIncrementalScan newScan() {
      return delegate.newScan();
    }
  }
}
