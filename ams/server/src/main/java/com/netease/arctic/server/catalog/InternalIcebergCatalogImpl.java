package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.catalog.IcebergCatalogWrapper;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOAdapter;
import com.netease.arctic.server.iceberg.InternalIcebergTableOperations;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.utils.IcebergTableUtils;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.io.FileIO;

public class InternalIcebergCatalogImpl extends MixedCatalogImpl {
  protected InternalIcebergCatalogImpl(CatalogMeta metadata) {
    super(metadata);
  }


  @Override
  public ArcticTable loadTable(String database, String tableName) {
    TableMetadata tableMetadata = getAs(TableMetaMapper.class, mapper ->
        mapper.selectTableMetaByName(getMetadata().getCatalogName(), database, tableName));
    if (tableMetadata == null) {
      return null;
    }
    FileIO io = IcebergTableUtils.newIcebergFileIo(getMetadata());
    ArcticFileIO fileIO = new ArcticFileIOAdapter(io);
    TableOperations ops = InternalIcebergTableOperations.buildForLoad(this.getMetadata(), tableMetadata, io);
    BaseTable table = new BaseTable(ops, TableIdentifier.of(database, tableName).toString());
    return new IcebergCatalogWrapper.BasicIcebergTable(
        com.netease.arctic.table.TableIdentifier.of(name(), database, tableName),
        table, fileIO, getMetadata().getCatalogProperties()
    );
  }
}
