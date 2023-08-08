package com.netease.arctic.spark.utils;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.hive.utils.CatalogUtil;
import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.netease.arctic.spark.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES;
import static com.netease.arctic.spark.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT;

public class SparkCatalogUtil {

  /**
   * Build an Arctic {@link TableIdentifier} for the given Spark identifier.
   *
   * @param catalog    Amoro's catalog
   * @param identifier Spark's identifier
   * @return an Arctic identifier
   */
  public static TableIdentifier buildIdentifier(String catalog, Identifier identifier) {
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length > 0,
        "database is not specific, table identifier: " + identifier.name()
    );
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length == 1,
        "arctic does not support multi-level namespace: " +
            Joiner.on(".").join(identifier.namespace())
    );
    return TableIdentifier.of(
        catalog,
        identifier.namespace()[0].split("\\.")[0],
        identifier.name());
  }


  /**
   * Build an Arctic TableStore {@link TableIdentifier} for the given Spark identifier.
   *
   * @param catalog    Amoro's catalog
   * @param identifier Spark's identifier
   * @return an Arctic identifier
   */
  public static TableIdentifier buildInnerTableIdentifier(String catalog, Identifier identifier) {
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length > 0,
        "database is not specific, table identifier: " + identifier.name()
    );
    Preconditions.checkArgument(
        identifier.namespace().length == 2,
        "arctic does not support multi-level namespace: " +
            Joiner.on(".").join(identifier.namespace())
    );

    return TableIdentifier.of(
        catalog,
        identifier.namespace()[0],
        identifier.namespace()[1]
    );
  }

  public static boolean usingTimestampWithoutZoneInNewTables(SparkSession spark, ArcticCatalog catalog) {
    boolean value = Boolean.parseBoolean(
        spark.conf().get(USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
            USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT));
    return CatalogUtil.isHiveCatalog(catalog) || value;
  }

  /**
   * Convert Spark schema to Iceberg schema
   *
   * @param schema                     - spark schema
   * @param properties                 - properties of table
   * @param handleTimestampWithoutZone - whether to use timestamp without zone in new tables
   * @return iceberg schema
   */
  public static Schema convertSchema(
      StructType schema, Map<String, String> properties, boolean handleTimestampWithoutZone) {
    Schema convertSchema = SparkSchemaUtil.convert(schema, handleTimestampWithoutZone);
    // schema add primary keys
    if (properties.containsKey("primary.keys")) {
      PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(convertSchema)
          .addDescription(properties.get("primary.keys"))
          .build();
      List<String> primaryKeys = primaryKeySpec.fieldNames();
      Set<String> pkSet = new HashSet<>(primaryKeys);
      Set<Integer> identifierFieldIds = new HashSet<>();
      List<Types.NestedField> columnsWithPk = new ArrayList<>();
      convertSchema.columns().forEach(nestedField -> {
        if (pkSet.contains(nestedField.name())) {
          columnsWithPk.add(nestedField.asRequired());
          identifierFieldIds.add(nestedField.fieldId());
        } else {
          columnsWithPk.add(nestedField);
        }
      });
      return new Schema(columnsWithPk, identifierFieldIds);
    }
    return convertSchema;
  }


  public static boolean isIdentifierLocation(String catalog, @Nullable String location, Identifier identifier) {
    List<String> nameParts = Lists.newArrayList();
    nameParts.add(catalog);
    nameParts.addAll(Arrays.asList(identifier.namespace()));
    nameParts.add(identifier.name());
    String ident = Joiner.on('.').join(nameParts);
    return ident.equalsIgnoreCase(location);
  }


  public static TableFormat providerToFormat(ArcticCatalog catalog, String provider) {
    if (SparkSQLProperties.PROVIDER_ICEBERG.equalsIgnoreCase(provider)) {
      return TableFormat.ICEBERG;
    } else if (SparkSQLProperties.PROVIDER_MIXED_ICEBERG.equalsIgnoreCase(provider)) {
      return TableFormat.MIXED_ICEBERG;
    } else if (SparkSQLProperties.PROVIDER_MIXED_HIVE.equalsIgnoreCase(provider)) {
      return TableFormat.MIXED_HIVE;
    } else if (SparkSQLProperties.PROVIDER_ARCTIC.equalsIgnoreCase(provider) || StringUtils.isBlank(provider)) {
      return CatalogUtil.isHiveCatalog(catalog) ? TableFormat.MIXED_HIVE : TableFormat.MIXED_ICEBERG;
    } else {
      throw new IllegalArgumentException(String.format("Unknown provider: %s", provider));
    }
  }
}
