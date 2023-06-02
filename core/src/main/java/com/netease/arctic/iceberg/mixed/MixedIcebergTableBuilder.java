package com.netease.arctic.iceberg.mixed;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ArcticTables;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicTableBuilder;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.Schema;

public class MixedIcebergTableBuilder extends BasicTableBuilder<MixedIcebergTableBuilder> {

  protected final ArcticTables icebergTables;

  public MixedIcebergTableBuilder(
      ArcticTables icebergTables,
      Schema schema,
      TableIdentifier identifier
  ) {
    super(schema, TableFormat.MIXED_ICEBERG, identifier);
    this.icebergTables = icebergTables;
  }

  @Override
  protected MixedIcebergTableBuilder self() {
    return this;
  }

  @Override
  public ArcticTable create() {
    ArcticTable base = icebergTables.newTableBuilder(schema, identifier)
        .withProperties(properties)
        .withPartitionSpec(spec)
        .withSortOrder(sortOrder)
        .create();

    if (!keySpec.primaryKeyExisted()) {
      return base;
    }
    TableIdentifier changeIdentifier = changeIdentifier();
    ArcticTable change = icebergTables.newTableBuilder(schema, changeIdentifier)
        .withProperties(properties)
        .withPartitionSpec(spec)
        .withSortOrder(sortOrder)
        .create();
    return new BasicKeyedTable(
        keySpec, null,
        new BasicKeyedTable.BaseInternalTable(base.asUnkeyedTable()),
        new BasicKeyedTable.ChangeInternalTable(identifier, change.asUnkeyedTable()));
  }


  protected TableIdentifier changeIdentifier() {
    return TableIdentifier.of(
        identifier.getCatalog(),
        identifier.getDatabase(),
        String.format("_%s_change_", identifier.getTableName())
        );
  }

}
