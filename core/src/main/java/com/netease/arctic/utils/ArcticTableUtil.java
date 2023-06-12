package com.netease.arctic.utils;

import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.iceberg.BasicIcebergTable;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Map;

public class ArcticTableUtil {
  /**
   * check arctic table is iceberg table format
   *
   * @param arcticTable target arctic table
   * @return Whether iceberg table format
   */
  public static boolean isIcebergTableFormat(ArcticTable arcticTable) {
    return arcticTable instanceof BasicIcebergTable;
  }

  /**
   * Return the base store of the arctic table.
   */
  public static UnkeyedTable baseStore(ArcticTable arcticTable) {
    if (arcticTable.isKeyedTable()) {
      return arcticTable.asKeyedTable().baseTable();
    } else {
      return arcticTable.asUnkeyedTable();
    }
  }

  /**
   * Return the table root location of the arctic table.
   */
  public static String tableRootLocation(ArcticTable arcticTable) {
    String tableRootLocation;
    if (!ArcticTableUtil.isIcebergTableFormat(arcticTable) && arcticTable.isUnkeyedTable()) {
      tableRootLocation = TableFileUtil.getFileDir(arcticTable.location());
    } else {
      tableRootLocation = arcticTable.location();
    }
    return tableRootLocation;
  }

  /**
   * get the change store identifier by base store.
   *
   * @param baseStoreIdentifier table identifier of a mixed-format table.
   * @param catalogProperties   catalog properties
   * @return change store identifier
   */
  public static TableIdentifier changeStoreIdentifier(
      TableIdentifier baseStoreIdentifier, Map<String, String> catalogProperties) {
    String separator = catalogProperties.getOrDefault(
        CatalogMetaProperties.MIXED_FORMAT_CHANGE_STORE_SEPARATOR,
        CatalogMetaProperties.MIXED_FORMAT_CHANGE_STORE_SEPARATOR_DEFAULT
    );
    String identifierTemplate = separator + "%s" + separator + "change" + separator;
    return TableIdentifier.of(
        baseStoreIdentifier.getCatalog(),
        baseStoreIdentifier.getDatabase(),
        String.format(identifierTemplate, baseStoreIdentifier.getTableName())
    );
  }

  /**
   * load change store identifier from base store
   *
   * @param base base store
   * @return change store table identifier
   */
  public static TableIdentifier changeStoreIdentifier(ArcticTable base) {
    if (!base.properties().containsKey(TableProperties.MIXED_ICEBERG_CHANGE_STORE_IDENTIFIER)) {
      throw new IllegalStateException("can read change store identifier from base store properties");
    }
    String change = base.properties().get(TableProperties.MIXED_ICEBERG_CHANGE_STORE_IDENTIFIER);
    String[] databaseAndTable = change.split("\\.");
    Preconditions.checkArgument(databaseAndTable.length == 2,
        "invalid table identifier: " + change);
    return TableIdentifier.of(base.id().getCatalog(), base.id().getTableName(), databaseAndTable[1]);
  }
}
