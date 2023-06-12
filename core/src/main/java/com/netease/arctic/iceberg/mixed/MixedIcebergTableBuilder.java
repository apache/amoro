package com.netease.arctic.iceberg.mixed;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.iceberg.EmptyAmsClient;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicTableBuilder;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.ArcticTableUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public class MixedIcebergTableBuilder extends BasicTableBuilder<MixedIcebergTableBuilder> {


  private final TableMetaStore tableMetaStore;
  private final Catalog icebergCatalog;
  private final Map<String, String> catalogProperties;

  public MixedIcebergTableBuilder(
      TableMetaStore tableMetaStore,
      Catalog icebergCatalog,
      Map<String, String> catalogProperties,
      Schema schema,
      TableIdentifier identifier
  ) {
    super(schema, TableFormat.MIXED_ICEBERG, identifier);
    this.icebergCatalog = icebergCatalog;
    this.tableMetaStore = tableMetaStore;
    this.catalogProperties = catalogProperties;
  }

  @Override
  protected MixedIcebergTableBuilder self() {
    return this;
  }

  @Override
  public ArcticTable create() {
    Map<String, String> tableProperties = this.tableProperties();
    TableIdentifier changeIdentifier = ArcticTableUtil.changeStoreIdentifier(this.identifier, catalogProperties);

    if (keySpec.primaryKeyExisted() &&
        icebergCatalog.tableExists(icebergIdentifier(changeIdentifier))) {
      throw new IllegalStateException("the change store already exists");
    }

    Catalog.TableBuilder baseBuilder = icebergCatalog.buildTable(icebergIdentifier(this.identifier), schema)
        .withPartitionSpec(spec)
        .withProperties(tableProperties)
        .withSortOrder(sortOrder);
    if (keySpec.primaryKeyExisted()) {
      baseBuilder = baseBuilder.withProperty(
          TableProperties.MIXED_ICEBERG_CHANGE_STORE_IDENTIFIER,
          changeIdentifier.getDatabase() + "." + changeIdentifier.getTableName());
    }
    Table base = baseBuilder.create();
    ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());

    if (!keySpec.primaryKeyExisted()) {
      return new BasicUnkeyedTable(this.identifier, base, io, new EmptyAmsClient(), catalogProperties);
    }

    Table change = icebergCatalog.buildTable(icebergIdentifier(changeIdentifier), schema)
        .withProperties(tableProperties)
        .withPartitionSpec(spec)
        .withSortOrder(sortOrder)
        .create();
    AmsClient client = new EmptyAmsClient();
    return new BasicKeyedTable(
        keySpec, client,
        new BasicKeyedTable.BaseInternalTable(this.identifier, base, io, client, catalogProperties),
        new BasicKeyedTable.ChangeInternalTable(this.identifier, change, io, client, catalogProperties));
  }

  protected Map<String, String> tableProperties() {
    Map<String, String> properties = Maps.newHashMap(this.properties);
    properties.put(TableProperties.TABLE_FORMAT, TableProperties.TABLE_FORMAT_MIXED_ICEBERG);

    if (keySpec.primaryKeyExisted()) {
      String fields = Joiner.on(",").join(keySpec.fieldNames());
      properties.put(TableProperties.MIXED_ICEBERG_PRIMARY_KEY_FIELDS, fields);
    }

    properties.put(TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
    properties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    properties.put(org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
    properties.put("flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
    return properties;
  }


  static org.apache.iceberg.catalog.TableIdentifier icebergIdentifier(TableIdentifier identifier) {
    return org.apache.iceberg.catalog.TableIdentifier.of(
        identifier.getDatabase(), identifier.getTableName()
    );
  }

}
