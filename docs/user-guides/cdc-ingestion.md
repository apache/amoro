---
title: "CDC Ingestion"
url: cdc-ingestion
aliases:
    - "user-guides/cdc-ingestion"
menu:
    main:
        parent: User Guides
        weight: 400
---
# CDC Ingestion
CDC stands for Change Data Capture, which is a broad concept, as long as it can capture the change data, it can be called CDC. Flink CDC is a Log message-based data capture tool, all the inventory and incremental data can be captured. Taking MySQL as an example, it can easily capture Binlog data through Debezium and process the calculations in real time to send them to the Arctic data lake. The Arctic data lake can then be queried by other engines.

This section will show how to practice one table into the lake and multiple tables into the lake for both Native-Iceberg and Mixed-Iceberg format.
## One table into the lake
### Native Iceberg format
The following example will show how MySQL CDC data is written to a Native-Iceberg table.
```sql
CREATE TABLE products (
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'localhost',
    'port' = '3306',
    'username' = 'root',
    'password' = '123456',
    'database-name' = 'mydb',
    'table-name' = 'products'
);
  
CREATE CATALOG iceberg_hadoop_catalog WITH (
    'type'='iceberg',
    'catalog-type'='hadoop',
    'warehouse'='hdfs://nn:8020/warehouse/path',
    'property-version'='1'
);

CREATE TABLE IF NOT EXISTS `iceberg_hadoop_catalog`.`default`.`sample` (
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
);

insert into `iceberg_hadoop_catalog`.`default`.`sample` select * from products;
```

### Mixed Iceberg format
The following example will show how MySQL CDC data is written to a Mixed-Iceberg table.
```sql
CREATE TABLE products (
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'localhost',
    'port' = '3306',
    'username' = 'root',
    'password' = '123456',
    'database-name' = 'mydb',
    'table-name' = 'products'
);

CREATE CATALOG arctic_catalog WITH (
    'type'='arctic',
    'metastore.url'='thrift://<ip>:<port>/<catalog_name_in_metastore>'
); 

CREATE TABLE IF NOT EXISTS `arctic_catalog`.`db`.`test_tb`(
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
);

insert into `arctic_catalog`.`db`.`test_tb` select * from products;
```

## Multiple tables into the lake
### Native Iceberg format
The following example will show how to write CDC data from multiple MySQL tables into the corresponding Native-Iceberg table.
```java
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.MySqlDeserializationConverterFactory;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.ververica.cdc.debezium.table.MetadataConverter;
import com.ververica.cdc.debezium.table.RowDataDebeziumDeserializeSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.catalog.UniqueConstraint;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.conversion.RowRowConverter;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ververica.cdc.connectors.mysql.table.MySqlReadableMetadata.DATABASE_NAME;
import static com.ververica.cdc.connectors.mysql.table.MySqlReadableMetadata.TABLE_NAME;
import static java.util.stream.Collectors.toMap;

public class MySqlCDC2IcebergExample {
  public static void main(String[] args) throws Exception {
    List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable = initSourceTables();
    Map<String, RowDataDebeziumDeserializeSchema> debeziumDeserializeSchemas = getDebeziumDeserializeSchemas(pathAndTable);
    MySqlSource<RowData> mySqlSource = MySqlSource.<RowData>builder()
      .hostname("yourHostname")
      .port(yourPort)
      .databaseList("test_db")
      // setting up tables to be captured
      .tableList("test_db.user", "test_db.product")
      .username("yourUsername")
      .password("yourPassword")
      .deserializer(new CompositeDebeziumDeserializationSchema(debeziumDeserializeSchemas))
      .build();

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // enable checkpoint
    env.enableCheckpointing(60000);
    
    // Split CDC streams by table name
    SingleOutputStreamOperator<Void> process = env
      .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
      .setParallelism(4)
      .process(new RowDataVoidProcessFunction(pathAndTable.stream()
          .collect(toMap(e -> e.f0.toString(),
              e -> RowRowConverter.create(e.f1.getResolvedSchema().toPhysicalRowDataType())))))
      .name("split stream");

    // create Iceberg sink and insert into CDC data
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, "yourWarehouseLocation");
    properties.put(CatalogProperties.URI, "yourThriftUri");
    CatalogLoader catalogLoader = CatalogLoader.hadoop("hadoop_catalog", new Configuration(), properties);
    Catalog icebergHadoopCatalog = catalogLoader.loadCatalog();
    Map<String, TableSchema> tableSchemas = new HashMap<>();
    tableSchemas.put("user", TableSchema.builder().field("id", DataTypes.INT())
      .field("name", DataTypes.STRING()).field("op_time", DataTypes.TIMESTAMP()).build());
    tableSchemas.put("product", TableSchema.builder().field("productId", DataTypes.INT())
      .field("price", DataTypes.DECIMAL(12, 6)).field("saleCount", DataTypes.INT()).build());

    for (Map.Entry<String, TableSchema> entry : tableSchemas.entrySet()) {
      TableIdentifier identifier = TableIdentifier.of(Namespace.of("test_db"), entry.getKey());
      Table table = icebergHadoopCatalog.loadTable(identifier);
      TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, identifier);

      FlinkSink.forRowData(process.getSideOutput(new OutputTag<RowData>(entry.getKey()){}))
        .tableLoader(tableLoader)
        .table(table)
        .append();
    }

    env.execute("Sync MySQL to Native Iceberg table");
  }

  static class CompositeDebeziumDeserializationSchema
    implements DebeziumDeserializationSchema<RowData> {

    private final Map<String, RowDataDebeziumDeserializeSchema> deserializationSchemaMap;

    public CompositeDebeziumDeserializationSchema(
      final Map<String, RowDataDebeziumDeserializeSchema> deserializationSchemaMap) {
      this.deserializationSchemaMap = deserializationSchemaMap;
    }

    @Override
    public void deserialize(final SourceRecord record, final Collector<RowData> out)
      throws Exception {
      final Struct value = (Struct) record.value();
      final Struct source = value.getStruct("source");
      final String db = source.getString("db");
      final String table = source.getString("table");
      if (deserializationSchemaMap == null) {
        throw new IllegalStateException("deserializationSchemaMap can not be null!");
      }
      deserializationSchemaMap.get(db + "." + table).deserialize(record, out);
    }

    @Override
    public TypeInformation<RowData> getProducedType() {
      return TypeInformation.of(RowData.class);
    }
  }

  static class RowDataVoidProcessFunction extends ProcessFunction<RowData, Void> {
    private final Map<String, RowRowConverter> converters;

    public RowDataVoidProcessFunction(final Map<String, RowRowConverter> converterMap) {
      this.converters = converterMap;
    }

    @Override
    public void processElement(final RowData rowData,
                               final ProcessFunction<RowData, Void>.Context ctx, final Collector<Void> out)
      throws Exception {
      final String key = rowData.getString(rowData.getArity() - 2).toString();
      ctx.output(new OutputTag<RowData>(key) {},
        getField(JoinedRowData.class, (JoinedRowData) rowData, "row1"));
    }

    private static <O, V> V getField(Class<O> clazz, O obj, String fieldName) {
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object v = field.get(obj);
        return v == null ? null : (V) v;
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static List<Tuple2<ObjectPath, ResolvedCatalogTable>> initSourceTables() {
    List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable = new ArrayList<>();
    // build table "user"
    Schema.Builder userTableBuilder = Schema.newBuilder();
    List<Column> userTableCols = new ArrayList<>();
    userTableCols.add(Column.physical("id", DataTypes.INT().notNull()));
    userTableBuilder.column("id", DataTypes.INT().notNull());
    userTableCols.add(Column.physical("name", DataTypes.STRING()));
    userTableBuilder.column("name", DataTypes.STRING());
    userTableCols.add(Column.physical("op_time", DataTypes.TIMESTAMP()));
    userTableBuilder.column("op_time", DataTypes.TIMESTAMP());
    userTableBuilder.primaryKey("id");
    Schema userSchema = userTableBuilder.build();
    Schema.UnresolvedPrimaryKey userPrimaryKey = userSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table user required pk "));
    ResolvedSchema userResolvedSchema = new ResolvedSchema(userTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
      userPrimaryKey.getConstraintName(), userPrimaryKey.getColumnNames()));
    ResolvedCatalogTable userTable = new ResolvedCatalogTable(
      CatalogTable.of(userSchema, "", Collections.emptyList(), new HashMap<>()), userResolvedSchema);
    pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "user"), userTable));
    // build table "product"
    Schema.Builder productTableBuilder = Schema.newBuilder();
    List<Column> productTableCols = new ArrayList<>();
    productTableCols.add(Column.physical("productId", DataTypes.INT().notNull()));
    productTableBuilder.column("productId", DataTypes.INT().notNull());
    productTableCols.add(Column.physical("price", DataTypes.DECIMAL(12, 6)));
    productTableBuilder.column("price", DataTypes.DECIMAL(12, 6));
    productTableCols.add(Column.physical("saleCount", DataTypes.INT()));
    productTableBuilder.column("saleCount", DataTypes.INT());
    productTableBuilder.primaryKey("productId");
    Schema productSchema = productTableBuilder.build();
    Schema.UnresolvedPrimaryKey productPrimaryKey = productSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table product required pk "));
    ResolvedSchema productResolvedSchema = new ResolvedSchema(productTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
      productPrimaryKey.getConstraintName(), productPrimaryKey.getColumnNames()));
    ResolvedCatalogTable productTable = new ResolvedCatalogTable(
      CatalogTable.of(productSchema, "", Collections.emptyList(), new HashMap<>()), productResolvedSchema);
    pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "product"), productTable));
    return pathAndTable;
  }

  private static Map<String, RowDataDebeziumDeserializeSchema> getDebeziumDeserializeSchemas(
    final List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable) {
    return pathAndTable.stream()
      .collect(toMap(e -> e.f0.toString(), e -> RowDataDebeziumDeserializeSchema.newBuilder()
        .setPhysicalRowType(
          (RowType) e.f1.getResolvedSchema().toPhysicalRowDataType().getLogicalType())
        .setUserDefinedConverterFactory(MySqlDeserializationConverterFactory.instance())
        .setMetadataConverters(
          new MetadataConverter[] {TABLE_NAME.getConverter(), DATABASE_NAME.getConverter()})
        .setResultTypeInfo(TypeInformation.of(RowData.class)).build()));
  }
}
```

### Mixed Iceberg format
The following example will show how to write CDC data from multiple MySQL tables into the corresponding Mixed-Iceberg table.
```java
import com.netease.arctic.flink.InternalCatalogBuilder;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.flink.write.FlinkSink;
import com.netease.arctic.table.TableIdentifier;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.MySqlDeserializationConverterFactory;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.ververica.cdc.debezium.table.MetadataConverter;
import com.ververica.cdc.debezium.table.RowDataDebeziumDeserializeSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.catalog.UniqueConstraint;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.conversion.RowRowConverter;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ververica.cdc.connectors.mysql.table.MySqlReadableMetadata.DATABASE_NAME;
import static com.ververica.cdc.connectors.mysql.table.MySqlReadableMetadata.TABLE_NAME;
import static java.util.stream.Collectors.toMap;

public class MySqlCDC2ArcticExample {
  public static void main(String[] args) throws Exception {
    List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable = initSourceTables();
    Map<String, RowDataDebeziumDeserializeSchema> debeziumDeserializeSchemas = getDebeziumDeserializeSchemas(
      pathAndTable);
    MySqlSource<RowData> mySqlSource = MySqlSource.<RowData>builder()
      .hostname("yourHostname")
      .port(yourPort)
      .databaseList("test_db")
      // setting up tables to be captured
      .tableList("test_db.user", "test_db.product")
      .username("yourUsername")
      .password("yourPassword")
      .deserializer(new CompositeDebeziumDeserializationSchema(debeziumDeserializeSchemas))
      .build();

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // enable checkpoint
    env.enableCheckpointing(60000);

    // Split CDC streams by table name
    SingleOutputStreamOperator<Void> process = env
      .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source").setParallelism(4)
      .process(new RowDataVoidProcessFunction(pathAndTable.stream()
        .collect(toMap(e -> e.f0.toString(),
          e -> RowRowConverter.create(e.f1.getResolvedSchema().toPhysicalRowDataType())))))
      .name("split stream");

    // create Arctic sink and insert into cdc data
    InternalCatalogBuilder catalogBuilder = InternalCatalogBuilder.builder().metastoreUrl(
      "thrift://<ip>:<port>/<catalog_name_in_metastore>");
    Map<String, TableSchema> tableSchemas = new HashMap<>();
    tableSchemas.put("user", TableSchema.builder().field("id", DataTypes.INT())
      .field("name", DataTypes.STRING()).field("op_time", DataTypes.TIMESTAMP()).build());
    tableSchemas.put("product", TableSchema.builder().field("productId", DataTypes.INT())
      .field("price", DataTypes.DECIMAL(12, 6)).field("saleCount", DataTypes.INT()).build());

    for (Map.Entry<String, TableSchema> entry : tableSchemas.entrySet()) {
      TableIdentifier tableId =
        TableIdentifier.of("yourCatalogName", "yourDatabaseName", entry.getKey());
      ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

      FlinkSink.forRowData(process.getSideOutput(new OutputTag<RowData>(entry.getKey()){}))
        .flinkSchema(entry.getValue())
        .table(ArcticUtils.loadArcticTable(tableLoader))
        .tableLoader(tableLoader).build();
    }

    env.execute("Sync MySQL to Mixed Iceberg table");
  }

  static class CompositeDebeziumDeserializationSchema
    implements DebeziumDeserializationSchema<RowData> {

    private final Map<String, RowDataDebeziumDeserializeSchema> deserializationSchemaMap;

    public CompositeDebeziumDeserializationSchema(
      final Map<String, RowDataDebeziumDeserializeSchema> deserializationSchemaMap) {
      this.deserializationSchemaMap = deserializationSchemaMap;
    }

    @Override
    public void deserialize(final SourceRecord record, final Collector<RowData> out)
      throws Exception {
      final Struct value = (Struct) record.value();
      final Struct source = value.getStruct("source");
      final String db = source.getString("db");
      final String table = source.getString("table");
      if (deserializationSchemaMap == null) {
        throw new IllegalStateException("deserializationSchemaMap can not be null!");
      }
      deserializationSchemaMap.get(db + "." + table).deserialize(record, out);
    }

    @Override
    public TypeInformation<RowData> getProducedType() {
      return TypeInformation.of(RowData.class);
    }
  }

  static class RowDataVoidProcessFunction extends ProcessFunction<RowData, Void> {
    private final Map<String, RowRowConverter> converters;

    public RowDataVoidProcessFunction(final Map<String, RowRowConverter> converterMap) {
      this.converters = converterMap;
    }

    @Override
    public void processElement(final RowData rowData,
                               final ProcessFunction<RowData, Void>.Context ctx, final Collector<Void> out)
      throws Exception {
      final String key = rowData.getString(rowData.getArity() - 2).toString();
      ctx.output(new OutputTag<RowData>(key) {},
        getField(JoinedRowData.class, (JoinedRowData) rowData, "row1"));
    }

    private static <O, V> V getField(Class<O> clazz, O obj, String fieldName) {
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object v = field.get(obj);
        return v == null ? null : (V) v;
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static List<Tuple2<ObjectPath, ResolvedCatalogTable>> initSourceTables() {
    List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable = new ArrayList<>();
    // build table "user"
    Schema.Builder userTableBuilder = Schema.newBuilder();
    List<Column> userTableCols = new ArrayList<>();
    userTableCols.add(Column.physical("id", DataTypes.INT().notNull()));
    userTableBuilder.column("id", DataTypes.INT().notNull());
    userTableCols.add(Column.physical("name", DataTypes.STRING()));
    userTableBuilder.column("name", DataTypes.STRING());
    userTableCols.add(Column.physical("op_time", DataTypes.TIMESTAMP()));
    userTableBuilder.column("op_time", DataTypes.TIMESTAMP());
    userTableBuilder.primaryKey("id");
    Schema userSchema = userTableBuilder.build();
    Schema.UnresolvedPrimaryKey userPrimaryKey = userSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table user required pk "));
    ResolvedSchema userResolvedSchema = new ResolvedSchema(userTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
      userPrimaryKey.getConstraintName(), userPrimaryKey.getColumnNames()));
    ResolvedCatalogTable userTable = new ResolvedCatalogTable(
      CatalogTable.of(userSchema, "", Collections.emptyList(), new HashMap<>()), userResolvedSchema);
    pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "user"), userTable));
    // build table "product"
    Schema.Builder productTableBuilder = Schema.newBuilder();
    List<Column> productTableCols = new ArrayList<>();
    productTableCols.add(Column.physical("productId", DataTypes.INT().notNull()));
    productTableBuilder.column("productId", DataTypes.INT().notNull());
    productTableCols.add(Column.physical("price", DataTypes.DECIMAL(12, 6)));
    productTableBuilder.column("price", DataTypes.DECIMAL(12, 6));
    productTableCols.add(Column.physical("saleCount", DataTypes.INT()));
    productTableBuilder.column("saleCount", DataTypes.INT());
    productTableBuilder.primaryKey("productId");
    Schema productSchema = productTableBuilder.build();
    Schema.UnresolvedPrimaryKey productPrimaryKey = productSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table product required pk "));
    ResolvedSchema productResolvedSchema = new ResolvedSchema(productTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
      productPrimaryKey.getConstraintName(), productPrimaryKey.getColumnNames()));
    ResolvedCatalogTable productTable = new ResolvedCatalogTable(
      CatalogTable.of(productSchema, "", Collections.emptyList(), new HashMap<>()), productResolvedSchema);
    pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "product"), productTable));
    return pathAndTable;
  }

  private static Map<String, RowDataDebeziumDeserializeSchema> getDebeziumDeserializeSchemas(
    final List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable) {
    return pathAndTable.stream()
      .collect(toMap(e -> e.f0.toString(), e -> RowDataDebeziumDeserializeSchema.newBuilder()
        .setPhysicalRowType(
          (RowType) e.f1.getResolvedSchema().toPhysicalRowDataType().getLogicalType())
        .setUserDefinedConverterFactory(MySqlDeserializationConverterFactory.instance())
        .setMetadataConverters(
          new MetadataConverter[] {TABLE_NAME.getConverter(), DATABASE_NAME.getConverter()})
        .setResultTypeInfo(TypeInformation.of(RowData.class)).build()));
  }
}
```