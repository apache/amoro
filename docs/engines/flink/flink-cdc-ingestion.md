---
title: "Flink CDC Ingestion"
url: flink-cdc-ingestion
aliases:
  - "flink/cdc"
menu:
    main:
        parent: User Guides
        weight: 400
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# Apache CDC Ingestion
CDC stands for Change Data Capture, which is a broad concept, as long as it can capture the change data, it can be called CDC. [Flink CDC](https://github.com/apache/flink-cdc) is a Log message-based data capture tool, all the inventory and incremental data can be captured. Taking MySQL as an example, it can easily capture Binlog data through Debezium and process the calculations in real time to send them to the data lake. The data lake can then be queried by other engines.

This section will show how to ingest one table or multiple tables into the data lake for both [Iceberg](../iceberg-format/) format and [Mixed-Iceberg](../mixed-iceberg-format/) format.
## Ingest into one table
### Iceberg format
The following example will show how [MySQL CDC](https://nightlies.apache.org/flink/flink-cdc-docs-release-3.1/docs/) data is written to an Iceberg table.

**Requirements**

Please add [Flink SQL Connector MySQL CDC](https://mvnrepository.com/artifact/org.apache.flink/flink-connector-mysql-cdc) and [Iceberg](https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-flink-1.18/1.6.0/iceberg-flink-1.18-1.6.0.jar) Jars to the lib directory of the Flink engine package.

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

INSERT INTO `iceberg_hadoop_catalog`.`default`.`sample` SELECT * FROM products;
```

### Mixed-Iceberg format
The following example will show how MySQL CDC data is written to a Mixed-Iceberg table.

**Requirements**

Please add [Flink SQL Connector MySQL CDC](https://mvnrepository.com/artifact/org.apache.flink/flink-connector-mysql-cdc/3.1.1) and [Amoro](../../../download/) Jars to the lib directory of the Flink engine package.

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

CREATE CATALOG amoro_catalog WITH (
    'type'='amoro',
    'metastore.url'='thrift://<ip>:<port>/<catalog_name_in_metastore>'
); 

CREATE TABLE IF NOT EXISTS `amoro_catalog`.`db`.`test_tb`(
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
);

INSERT INTO `amoro_catalog`.`db`.`test_tb` SELECT * FROM products;
```

## Ingest Into multiple tables
### Iceberg format
The following example will show how to write CDC data from multiple MySQL tables into the corresponding Iceberg table.

**Requirements**

Please add [Flink Connector MySQL CDC](https://mvnrepository.com/artifact/org.apache.flink/flink-connector-mysql-cdc/3.1.1)
and [Iceberg](https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-flink-1.18/1.6.0) dependencies to your 
Maven project's pom.xml file.

```java
import static java.util.stream.Collectors.toMap;
import static org.apache.flink.cdc.connectors.mysql.table.MySqlReadableMetadata.DATABASE_NAME;
import static org.apache.flink.cdc.connectors.mysql.table.MySqlReadableMetadata.TABLE_NAME;

import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.table.MySqlDeserializationConverterFactory;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.cdc.debezium.table.MetadataConverter;
import org.apache.flink.cdc.debezium.table.RowDataDebeziumDeserializeSchema;
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
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      .process(new SplitCdcStreamFunction(pathAndTable.stream()
          .collect(toMap(e -> e.f0.toString(),
              e -> RowRowConverter.create(e.f1.getResolvedSchema().toPhysicalRowDataType())))))
      .name("split stream");

    // create Iceberg sink and insert into CDC data
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, "yourWarehouseLocation");
    properties.put(CatalogProperties.URI, "yourThriftUri");
    CatalogLoader catalogLoader = CatalogLoader.hadoop("hadoop_catalog", new Configuration(), properties);
    Catalog icebergHadoopCatalog = catalogLoader.loadCatalog();
    Map<String, TableSchema> sinkTableSchemas = new HashMap<>();
    sinkTableSchemas.put("user", TableSchema.builder().field("id", DataTypes.INT())
      .field("name", DataTypes.STRING()).field("op_time", DataTypes.TIMESTAMP()).build());
    sinkTableSchemas.put("product", TableSchema.builder().field("productId", DataTypes.INT())
      .field("price", DataTypes.DECIMAL(12, 6)).field("saleCount", DataTypes.INT()).build());

    for (Map.Entry<String, TableSchema> entry : sinkTableSchemas.entrySet()) {
      TableIdentifier identifier = TableIdentifier.of(Namespace.of("test_db"), entry.getKey());
      Table table = icebergHadoopCatalog.loadTable(identifier);
      TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, identifier);

      FlinkSink.forRowData(process.getSideOutput(new OutputTag<RowData>(entry.getKey()){}))
        .tableLoader(tableLoader)
        .table(table)
        .append();
    }

    env.execute("Sync MySQL to the Iceberg table");
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

  static class SplitCdcStreamFunction extends ProcessFunction<RowData, Void> {
    private final Map<String, RowRowConverter> converters;

    public SplitCdcStreamFunction(final Map<String, RowRowConverter> converterMap) {
      this.converters = converterMap;
    }

    @Override
    public void processElement(final RowData rowData,
                               final ProcessFunction<RowData, Void>.Context ctx, final Collector<Void> out)
      throws Exception {
      // JoinedRowData like +I{row1=+I(1,2.340000,3), row2=+I(product,test_db)}
      // so rowData.getArity() - 2 is the tableName field index
      final String tableName = rowData.getString(rowData.getArity() - 2).toString();
      ctx.output(new OutputTag<RowData>(tableName) {},
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
    Schema userSchema = Schema.newBuilder()
      .column("id", DataTypes.INT().notNull())
      .column("name", DataTypes.STRING())
      .column("op_time", DataTypes.TIMESTAMP())
      .primaryKey("id")
      .build();
    List<Column> userTableCols = Stream.of(
      Column.physical("id", DataTypes.INT().notNull()),
      Column.physical("name", DataTypes.STRING()),
      Column.physical("op_time", DataTypes.TIMESTAMP())).collect(Collectors.toList());
    Schema.UnresolvedPrimaryKey userPrimaryKey = userSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table user required pk "));
    ResolvedSchema userResolvedSchema = new ResolvedSchema(userTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
      userPrimaryKey.getConstraintName(), userPrimaryKey.getColumnNames()));
    ResolvedCatalogTable userTable = new ResolvedCatalogTable(
      CatalogTable.of(userSchema, "", Collections.emptyList(), new HashMap<>()), userResolvedSchema);
    pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "user"), userTable));

    // build table "product"
    Schema productSchema = Schema.newBuilder()
      .column("productId", DataTypes.INT().notNull())
      .column("price", DataTypes.DECIMAL(12, 6))
      .column("saleCount", DataTypes.INT())
      .primaryKey("productId")
      .build();
    List<Column> productTableCols = Stream.of(
      Column.physical("productId", DataTypes.INT().notNull()),
      Column.physical("price", DataTypes.DECIMAL(12, 6)),
      Column.physical("saleCount", DataTypes.INT())).collect(Collectors.toList());
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

### Mixed-Iceberg format
The following example will show how to write CDC data from multiple MySQL tables into the corresponding Mixed-Iceberg table.

**Requirements**

Please add [Flink Connector MySQL CDC](https://mvnrepository.com/artifact/org.apache.flink/flink-connector-mysql-cdc/3.1.1) and [Amoro](https://mvnrepository.com/artifact/org.apache.amoro/amoro-format-mixed-flink-1.17/0.7.0-incubating) dependencies to your Maven project's pom.xml file.

```java
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.flink.write.FlinkSink;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.table.MySqlDeserializationConverterFactory;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.cdc.debezium.table.MetadataConverter;
import org.apache.flink.cdc.debezium.table.RowDataDebeziumDeserializeSchema;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.*;
import org.apache.flink.table.catalog.*;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.conversion.RowRowConverter;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.apache.flink.cdc.connectors.mysql.table.MySqlReadableMetadata.DATABASE_NAME;
import static org.apache.flink.cdc.connectors.mysql.table.MySqlReadableMetadata.TABLE_NAME;

public class MySqlCDC2MixedIcebergExample {
    public static void main(String[] args) throws Exception {
        List<Tuple2<ObjectPath, ResolvedCatalogTable>> pathAndTable = initSourceTables();
        Map<String, RowDataDebeziumDeserializeSchema> debeziumDeserializeSchemas = getDebeziumDeserializeSchemas(
                pathAndTable);
        MySqlSource<RowData> mySqlSource = MySqlSource.<RowData>builder()
                .hostname("yourHostname")
                .port(3306)
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
                .process(new SplitCdcStreamFunction(pathAndTable.stream()
                        .collect(toMap(e -> e.f0.toString(),
                                e -> RowRowConverter.create(e.f1.getResolvedSchema().toPhysicalRowDataType())))))
                .name("split stream");

        // create Amoro sink and insert into cdc data
        InternalCatalogBuilder catalogBuilder = InternalCatalogBuilder.builder().metastoreUrl(
                "thrift://<ip>:<port>/<catalog_name_in_metastore>");
        Map<String, TableSchema> sinkTableSchemas = new HashMap<>();
        sinkTableSchemas.put("user", TableSchema.builder().field("id", DataTypes.INT())
                .field("name", DataTypes.STRING()).field("op_time", DataTypes.TIMESTAMP()).build());
        sinkTableSchemas.put("product", TableSchema.builder().field("productId", DataTypes.INT())
                .field("price", DataTypes.DECIMAL(12, 6)).field("saleCount", DataTypes.INT()).build());

        for (Map.Entry<String, TableSchema> entry : sinkTableSchemas.entrySet()) {
            TableIdentifier tableId =
                    TableIdentifier.of("yourCatalogName", "yourDatabaseName", entry.getKey());
            MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

            FlinkSink.forRowData(process.getSideOutput(new OutputTag<RowData>(entry.getKey()) {
                    }))
                    .flinkSchema(entry.getValue())
                    .table(MixedFormatUtils.loadMixedTable(tableLoader))
                    .tableLoader(tableLoader).build();
        }

        env.execute("Sync MySQL to Mixed-Iceberg table");
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

    static class SplitCdcStreamFunction extends ProcessFunction<RowData, Void> {
        private final Map<String, RowRowConverter> converters;

        public SplitCdcStreamFunction(final Map<String, RowRowConverter> converterMap) {
            this.converters = converterMap;
        }

        @Override
        public void processElement(final RowData rowData,
                                   final ProcessFunction<RowData, Void>.Context ctx, final Collector<Void> out)
                throws Exception {
            // JoinedRowData like +I{row1=+I(1,2.340000,3), row2=+I(product,test_db)}
            // so rowData.getArity() - 2 is the tableName field index
            final String tableName = rowData.getString(rowData.getArity() - 2).toString();
            ctx.output(new OutputTag<RowData>(tableName) {
                       },
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
        Schema userSchema = Schema.newBuilder()
                .column("id", DataTypes.INT().notNull())
                .column("name", DataTypes.STRING())
                .column("op_time", DataTypes.TIMESTAMP())
                .primaryKey("id")
                .build();
        List<Column> userTableCols = Stream.of(
                Column.physical("id", DataTypes.INT().notNull()),
                Column.physical("name", DataTypes.STRING()),
                Column.physical("op_time", DataTypes.TIMESTAMP())).collect(Collectors.toList());
        Schema.UnresolvedPrimaryKey userPrimaryKey = userSchema.getPrimaryKey().orElseThrow(() -> new RuntimeException("table user required pk "));
        ResolvedSchema userResolvedSchema = new ResolvedSchema(userTableCols, Collections.emptyList(), UniqueConstraint.primaryKey(
                userPrimaryKey.getConstraintName(), userPrimaryKey.getColumnNames()));
        ResolvedCatalogTable userTable = new ResolvedCatalogTable(
                CatalogTable.of(userSchema, "", Collections.emptyList(), new HashMap<>()), userResolvedSchema);
        pathAndTable.add(Tuple2.of(new ObjectPath("test_db", "user"), userTable));

        // build table "product"
        Schema productSchema = Schema.newBuilder()
                .column("productId", DataTypes.INT().notNull())
                .column("price", DataTypes.DECIMAL(12, 6))
                .column("saleCount", DataTypes.INT())
                .primaryKey("productId")
                .build();
        List<Column> productTableCols = Stream.of(
                Column.physical("productId", DataTypes.INT().notNull()),
                Column.physical("price", DataTypes.DECIMAL(12, 6)),
                Column.physical("saleCount", DataTypes.INT())).collect(Collectors.toList());
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
                                new MetadataConverter[]{TABLE_NAME.getConverter(), DATABASE_NAME.getConverter()})
                        .setResultTypeInfo(TypeInformation.of(RowData.class)).build()));
    }
}
```