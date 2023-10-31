package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.formats.mixed.MixedIcebergTable;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.server.utils.InternalTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class InternalMixedCatalogImpl extends InternalIcebergCatalogImpl {

  protected InternalMixedCatalogImpl(CatalogMeta metadata, Configurations serverConfiguration) {
    super(metadata, serverConfiguration);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
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
    ArcticTable mixedIcebergTable;

    ArcticFileIO fileIO = fileIO(getMetadata());
    BaseTable baseTable = loadIcebergTable(fileIO, tableMetadata);
    if (StringUtils.isNotEmpty(tableMetadata.getChangeLocation())
        && StringUtils.isNotEmpty(tableMetadata.getPrimaryKey())) {
      BaseTable changeTable = loadChangeStore(fileIO, tableMetadata);

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
    return new MixedIcebergTable(mixedIcebergTable);
  }

  @Override
  protected boolean isStageCreate(TableMetadata metadata) {
    return StringUtils.isNotEmpty(metadata.getPrimaryKey())
        && StringUtils.isEmpty(metadata.getChangeLocation());
  }

  protected BaseTable loadChangeStore(ArcticFileIO fileIO, TableMetadata tableMetadata) {
    TableOperations ops = InternalTableUtil.newTableOperations(tableMetadata, fileIO, true);
    return new BaseTable(
        ops,
        TableIdentifier.of(
                tableMetadata.getTableIdentifier().getDatabase(),
                tableMetadata.getTableIdentifier().getTableName())
            .toString());
  }
}
