package com.netease.arctic.spark;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.spark.table.SparkTableBuilder;
import com.netease.arctic.spark.table.SparkTableBuilderFactory;
import com.netease.arctic.spark.utils.SparkCatalogUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.NamespaceAlreadyExistsException;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.NamespaceChange;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import scala.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.arctic.spark.SparkSQLProperties.REFRESH_CATALOG_BEFORE_USAGE;
import static com.netease.arctic.spark.SparkSQLProperties.REFRESH_CATALOG_BEFORE_USAGE_DEFAULT;
import static com.netease.arctic.spark.utils.SparkCatalogUtil.buildIdentifier;
import static com.netease.arctic.spark.utils.SparkCatalogUtil.buildInnerTableIdentifier;

/**
 * base class for spark catalog.
 * reuse the code for different spark versions.
 */
public abstract class SparkCatalogBase implements TableCatalog, SupportsNamespaces {

  private String name;
  private ArcticCatalog catalog;
  private final Map<TableFormat, SparkTableBuilderFactory> tableBuilderFactories = Maps.newConcurrentMap();


  @Override
  public void initialize(String name, CaseInsensitiveStringMap options) {
    this.name = name;
    String catalogUrl = options.get("url");

    Preconditions.checkArgument(StringUtils.isNotBlank(catalogUrl),
        "lack required properties: url");
    Map<String, String> properties = Maps.newHashMap(options);
    properties.put(CatalogMetaProperties.SHOW_ONLY_MIXED_FORMAT, "true");
    catalog = CatalogLoader.load(catalogUrl, properties);
  }

  @Override
  public String name() {
    return name;
  }

  // ---------------------- namespace api begin ---------------------

  @Override
  public String[][] listNamespaces() throws NoSuchNamespaceException {
    return catalog.listDatabases().stream()
        .map(d -> new String[]{d})
        .toArray(String[][]::new);
  }

  @Override
  public String[][] listNamespaces(String[] namespace) throws NoSuchNamespaceException {
    return new String[0][];
  }

  @Override
  public Map<String, String> loadNamespaceMetadata(String[] namespace) throws NoSuchNamespaceException {
    String database = namespace[0];
    return catalog.listDatabases().stream()
        .filter(d -> StringUtils.equals(d, database))
        .map(d -> new HashMap<String, String>())
        .findFirst().orElseThrow(() -> new NoSuchNamespaceException(namespace));
  }

  @Override
  public void createNamespace(String[] namespace, Map<String, String> metadata)
      throws NamespaceAlreadyExistsException {
    if (namespace.length > 1) {
      throw new UnsupportedOperationException("arctic does not support multi-level namespace.");
    }
    String database = namespace[0];
    catalog.createDatabase(database);
  }

  @Override
  public void alterNamespace(String[] namespace, NamespaceChange... changes) throws NoSuchNamespaceException {
    throw new UnsupportedOperationException("Alter  namespace is not supported by catalog: " + this.name());
  }

  @Override
  public boolean dropNamespace(String[] namespace) throws NoSuchNamespaceException {
    String database = namespace[0];
    catalog.dropDatabase(database);
    return true;
  }

  // ---------------------- namespace api ends ---------------------
  // ---------------------- table api begin ---------------------

  @Override
  public Identifier[] listTables(String[] namespace) throws NoSuchNamespaceException {
    List<String> database;
    if (namespace == null || namespace.length == 0) {
      database = catalog.listDatabases();
    } else {
      database = new ArrayList<>();
      database.add(namespace[0]);
    }

    List<TableIdentifier> tableIdentifiers = database.stream()
        .map(d -> catalog.listTables(d))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    return tableIdentifiers.stream()
        .map(i -> Identifier.of(new String[]{i.getDatabase()}, i.getTableName()))
        .toArray(Identifier[]::new);
  }

  @Override
  public Table loadTable(Identifier ident) throws NoSuchTableException {
    checkAndRefreshCatalogMeta(catalog);
    TableIdentifier identifier;
    ArcticTable table;
    String internalTableName = null;
    try {
      if (isInnerTableIdentifier(ident)) {
        ArcticTableStoreType type = ArcticTableStoreType.from(ident.name());
        identifier = buildInnerTableIdentifier(registeredCatalogName(), ident);
        table = catalog.loadTable(identifier);
        internalTableName = type.name();
      } else {
        identifier = buildIdentifier(registeredCatalogName(), ident);
        table = catalog.loadTable(identifier);
      }
    } catch (org.apache.iceberg.exceptions.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
    return toSparkTable(table, internalTableName);
  }

  @Override
  public Table createTable(Identifier ident, StructType schema, Transform[] transforms, Map<String, String> properties)
      throws TableAlreadyExistsException, NoSuchNamespaceException {
    checkAndRefreshCatalogMeta(catalog);
    properties = Maps.newHashMap(properties);
    boolean handleTimestampWithoutZone = SparkCatalogUtil.usingTimestampWithoutZoneInNewTables(
        SparkSession.active(), catalog);
    Schema finalSchema = SparkCatalogUtil.convertSchema(schema, properties, handleTimestampWithoutZone);
    TableIdentifier identifier = buildIdentifier(registeredCatalogName(), ident);
    String provider = properties.get(TableCatalog.PROP_PROVIDER);
    TableFormat format = SparkCatalogUtil.providerToFormat(catalog, provider);
    TableBuilder builder = catalog.newTableBuilder(identifier, finalSchema, format);
    PartitionSpec spec = Spark3Util.toPartitionSpec(finalSchema, transforms);
    if (SparkCatalogUtil.isIdentifierLocation(registeredCatalogName(),
            properties.get(TableCatalog.PROP_LOCATION), ident)) {
      properties.remove(TableCatalog.PROP_LOCATION);
    }
    try {
      if (properties.containsKey("primary.keys")) {
        PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(finalSchema)
            .addDescription(properties.get("primary.keys"))
            .build();
        properties.remove("primary.keys");
        builder.withPartitionSpec(spec)
            .withProperties(properties)
            .withPrimaryKeySpec(primaryKeySpec);
      } else {
        builder.withPartitionSpec(spec)
            .withProperties(properties);
      }
      ArcticTable table = builder.create();
      return toSparkTable(table, null);
    } catch (AlreadyExistsException e) {
      throw new TableAlreadyExistsException("Table " + ident + " already exists", Option.apply(e));
    }
  }

  @Override
  public boolean dropTable(Identifier ident) {
    TableIdentifier identifier = buildIdentifier(registeredCatalogName(), ident);
    return catalog.dropTable(identifier, false);
  }

  @Override
  public boolean purgeTable(Identifier ident) throws UnsupportedOperationException {
    TableIdentifier identifier = buildIdentifier(registeredCatalogName(), ident);
    return catalog.dropTable(identifier, true);
  }

  @Override
  public void renameTable(Identifier oldIdent, Identifier newIdent)
      throws NoSuchTableException, TableAlreadyExistsException {
    throw new UnsupportedOperationException("Unsupported renameTable.");
  }


  // ---------------------- table api ends ---------------------

  // ---------------------- protected methods begin ---------------------

  protected void registerTableBuilder(TableFormat format, SparkTableBuilderFactory builderFactory) {
    Preconditions.checkNotNull(builderFactory, "table builder factory is null");
    tableBuilderFactories.put(format, builderFactory);
  }

  protected String registeredCatalogName() {
    return catalog.name();
  }

  protected ArcticCatalog catalog() {
    return catalog;
  }

  protected Table toSparkTable(ArcticTable table, String internalTableName) {
    TableFormat format = table.format();
    SparkTableBuilderFactory factory = tableBuilderFactories.get(format);
    if (factory == null) {
      throw new IllegalArgumentException("table format " + format + " is not supported, no table builder found");
    }
    SparkTableBuilder builder = factory.create(catalog, new CaseInsensitiveStringMap(Maps.newHashMap()));
    return builder.build(table, internalTableName);
  }

  // ---------------------- protected methods end ---------------------
  // ------------------ private methods begin ------------------
  private void checkAndRefreshCatalogMeta(ArcticCatalog catalog) {
    SparkSession sparkSession = SparkSession.active();
    if (Boolean.parseBoolean(sparkSession.conf().get(
        REFRESH_CATALOG_BEFORE_USAGE,
        REFRESH_CATALOG_BEFORE_USAGE_DEFAULT))) {
      catalog.refresh();
    }
  }

  private boolean isInnerTableIdentifier(Identifier identifier) {
    if (identifier.namespace().length != 2) {
      return false;
    }
    return ArcticTableStoreType.from(identifier.name()) != null;
  }
  // ------------------ private methods end ------------------
}
