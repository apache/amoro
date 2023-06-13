## Introduce multi-catalog

A catalog is a metadata namespace that stores information about databases, tables, views, indexes, users, and UDFs. It provides a higher-level namespace for `table` and `database`. Typically, a catalog is associated with a specific type of data source or cluster. In Flink, Spark, and Trino, the multi-catalog feature can be used to support SQL across data sources, such as:

```SQL
SELECT c.ID, c.NAME, c.AGE, o.AMOUNT
FROM MYSQL.ONLINE.CUSTOMERS c JOIN HIVE.OFFLINE.ORDERS o
ON (c.ID = o.CUSTOMER_ID)
```

In the past, data lakes were managed using the Hive Metastore (HMS) to handle metadata. Unfortunately, HMS does not support multi-catalog, which limits the capabilities of engines on the data lake. For example, some users may want to use Spark to perform federated computation across different Hive clusters by specifying the catalog name, requiring them to develop a Hive catalog plugin in the upper layer. Additionally, data lake formats are moving from a single Hive-centric approach to a landscape of competing formats such as Iceberg, Delta, and Hudi. These new data lake formats are more cloud-friendly and will facilitate the migration of data lakes to the cloud. In this context, a management system that supports multi-catalog is needed to help users govern data lakes with different environments and formats.

Users can create catalogs in Arctic for different environments, clusters, and table formats, and leverage the multi-catalog feature in Flink, Spark, and Trino to enable federated computation across multiple clusters and formats. Additionally, properties configured in catalogs can be shared by all tables and users, avoiding duplication. By leveraging the multi-catalog design, Arctic provides support for a metadata center in data platforms.

When AMS and HMS are used together, HMS serves as the storage foundation for AMS. With the [Iceberg Format](//TODO), users can leverage the multi-catalog management functionality of AMS without introducing any Arctic dependencies.

## How to use

Arctic v0.4 introduced the catalog management feature, where table creation is performed under a catalog. Users can create, edit, and delete catalogs in the catalogs module, which requires configuration of metastore, table format, and environment information upon creation. For more information, please refer to the documentation: [Managing catalogs](../guides/managing-catalogs.md)

???+ Notes

	Users can set configurations for all tables in catalog properties or specify table configurations when creating tables. Configurations can also be set during runtime for each engine. The overriding rule for these configurations is that engine-level configurations take precedence over table-level configurations, which take precedence over catalog-level configurations.

In practice, it is recommended to create a catalog as follows:

- If you want to collaborate with HMS, it is recommended to choose Hive as the `Metastore`, and choose Mixed Hive or Iceberg as the `Format` according to your needs.
- If you want to use the Mixed Iceberg Format provided by Arctic, it is recommended to choose Arctic as the `Metastore`.

Currently, only one table format can be selected when creating an Arctic catalog. This is mainly because the engine will parse the catalog into a specific data source when using it, and a one-to-one format is intuitive. On the other hand, this limitation can be bypassed when using HMS directly, such as the SessionCatalog implementation provided by the Iceberg community. In the future, Arctic will consider providing users with more flexible management methods.

## Future work

AMS will focus on two goals to enhance the value of the metadata center in the future:

- Expand data sources —  In addition to data lakes, message queues, databases, and data warehouses can all be managed as objects in the catalog. Through metadata center and SQL-based federated computing of the computing engine, AMS will provide infrastructure solutions for data platforms such as DataOps and DataFabric
- Automatic Catalog Detection —  In computing engines like Spark and Flink, it is possible to automatically detect the creation and changes of a catalog, enabling a one-time configuration for permanent scalability.
  