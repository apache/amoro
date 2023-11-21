package com.netease.arctic.server.catalog;

import static com.netease.arctic.server.table.internal.InternalTableConstants.CHANGE_STORE_TABLE_NAME_SUFFIX;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.formats.mixed.MixedTable;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.mixed.InternalMixedIcebergCatalog;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.table.internal.InternalMixedIcebergCreator;
import com.netease.arctic.server.table.internal.InternalMixedIcebergHandler;
import com.netease.arctic.server.table.internal.InternalTableCreator;
import com.netease.arctic.server.table.internal.InternalTableHandler;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.server.utils.InternalTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.requests.CreateTableRequest;

public class InternalMixedCatalogImpl extends InternalIcebergCatalogImpl {

  protected InternalMixedCatalogImpl(CatalogMeta metadata, Configurations serverConfiguration) {
    super(metadata, serverConfiguration);
  }

  @Override
  protected InternalTableCreator newTableCreator(
      String database, String tableName, CreateTableRequest request) {
    return new InternalMixedIcebergCreator(getMetadata(), database, tableName, request);
  }

  @Override
  public <O> InternalTableHandler<O> newTableHandler(String database, String tableStoreName) {
    String tableName = tableName(tableStoreName);
    boolean isChangeStore = isChangeStoreName(tableStoreName);
    TableMetadata metadata = loadTableMetadata(database, tableName);
    Preconditions.checkState(
        metadata.getFormat() == format(),
        "the catalog only support to handle %s table",
        format().name());
    //noinspection unchecked
    return (InternalTableHandler<O>) newTableStoreHandler(metadata, isChangeStore);
  }

  private InternalTableHandler<TableOperations> newTableStoreHandler(
      TableMetadata metadata, boolean isChangeStore) {
    return new InternalMixedIcebergHandler(getMetadata(), metadata, isChangeStore);
  }

  private String tableName(String tableStoreName) {
    if (isChangeStoreName(tableStoreName)) {
      return tableStoreName.substring(
          0, tableStoreName.length() - CHANGE_STORE_TABLE_NAME_SUFFIX.length());
    }
    return tableStoreName;
  }

  private boolean isChangeStoreName(String tableName) {
    String separator = InternalMixedIcebergCatalog.CHANGE_STORE_SEPARATOR;
    if (!tableName.contains(separator)) {
      return false;
    }
    Preconditions.checkArgument(
        tableName.indexOf(separator) == tableName.lastIndexOf(separator)
            && tableName.endsWith(CHANGE_STORE_TABLE_NAME_SUFFIX),
        "illegal table name: %s, %s is not allowed in table name.",
        tableName,
        separator);

    return true;
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    Preconditions.checkArgument(
        !isChangeStoreName(tableName), "table name is invalid for load table");
    TableMetadata tableMetadata =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableMetaByName(getMetadata().getCatalogName(), database, tableName));
    if (tableMetadata == null) {
      return null;
    }
    Preconditions.checkArgument(
        TableFormat.MIXED_ICEBERG == tableMetadata.getFormat(),
        "Table: %s.%s.%s is not a mixed-iceberg table",
        name(),
        database,
        tableName);

    com.netease.arctic.table.TableIdentifier tableIdentifier =
        com.netease.arctic.table.TableIdentifier.of(name(), database, tableName);
    ArcticFileIO fileIO = InternalTableUtil.newIcebergFileIo(getMetadata());
    ArcticTable mixedIcebergTable;

    BaseTable baseTable = loadTableStore(tableMetadata, false);
    if (InternalTableUtil.isKeyedMixedTable(tableMetadata)) {
      BaseTable changeTable = loadTableStore(tableMetadata, true);

      PrimaryKeySpec.Builder keySpecBuilder = PrimaryKeySpec.builderFor(baseTable.schema());
      tableMetadata.buildTableMeta().getKeySpec().getFields().forEach(keySpecBuilder::addColumn);
      PrimaryKeySpec keySpec = keySpecBuilder.build();

      mixedIcebergTable =
          new BasicKeyedTable(
              tableMetadata.getTableLocation(),
              keySpec,
              new BasicKeyedTable.BaseInternalTable(
                  tableIdentifier, baseTable, fileIO, getMetadata().getCatalogProperties()),
              new BasicKeyedTable.ChangeInternalTable(
                  tableIdentifier, changeTable, fileIO, getMetadata().getCatalogProperties()));
    } else {

      mixedIcebergTable =
          new BasicUnkeyedTable(
              tableIdentifier, baseTable, fileIO, getMetadata().getCatalogProperties());
    }

    return new MixedTable(mixedIcebergTable, TableFormat.MIXED_ICEBERG);
  }

  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  protected BaseTable loadTableStore(TableMetadata tableMetadata, boolean isChangeStore) {
    TableOperations ops = newTableStoreHandler(tableMetadata, isChangeStore).newTableOperator();
    return new BaseTable(
        ops,
        TableIdentifier.of(
                tableMetadata.getTableIdentifier().getDatabase(),
                tableMetadata.getTableIdentifier().getTableName())
            .toString());
  }
}
