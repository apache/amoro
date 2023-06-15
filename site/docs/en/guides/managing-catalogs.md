
In the default AMS configuration, we have initialized a cluster named `local_catalog` based on the local file system for your testing convenience. 
Users can import more test or online clusters through the Catalog management function provided by the AMS Dashboard. Before adding a new Catalog, 
please read the following guidelines and select the appropriate creation according to your actual needs.


## Create Catalog
In Arctic, the catalog is a namespace for a group of libraries and tables. Under the catalog, it is further divided into different databases, and under each database, there are different tables. The name of a table in Arctic is uniquely identified by the format catalog.database.table. In practical applications, a catalog generally corresponds to a metadata service, such as the commonly used Hive Metastore in big data. Arctic MetaService can also serve as a metadata service. In order to differentiate the storage method of metadata, Arctic classifies the Catalog type into Internal Catalog and External Catalog. Catalogs that use Arctic MetaService as the metadata service are Internal Catalogs, while others are External Catalogs. When creating an External Catalog, you need to select the storage backend for its metadata, such as Hive, Hadoop, or Custom.
In addition, when defining a catalog, you also need to select the table format used under it. Currently, Arctic supports the following table formats:
：[Iceberg](../concepts/table-formats.md#iceberg-format) 、[Mixed Hive](../concepts/table-formats.md#mixed-hive-format)、[Mixed Iceberg](../concepts/table-formats.md#mixed-iceberg-format)。The creation method is as follows:

Recommend users to create a Catalog following the guidelines below：

| **Key**      | **Valid Values**                                                   | **Dscription**                                                     |
| --------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| name            | Only numbers, letters, _, - , starting with letters are supported (lower case letters are recommended)                | catalog name                                               |
| metastore       | Arctic Metastore (for using AMS to store metadata), Hive Metastore (for using HMS to store metadata), Hadoop (corresponding to iceberg's Hadoop catalog), Custom (other iceberg catalog implementations) | Storage type for table metadata                                      |
| table format    | Iceberg 、Mixed Hive、Mixed  Iceberg                         | Currently, only the metastore type Hive Metastore supports both Mixed Hive/Iceberg, and Arctic Metastore supports Mixed Iceberg type. All other metastore types only support Iceberg, currently a Metastore only supports one kind of TableFormat, in the future will support multiple|
| core-site       | Upload the core-site.xml of the hadoop cluster                             | core-site.xml                                                |
| hdfs-site       | Upload the hdfs-site.xml of the hadoop cluster                             | hdfs-site.xml                                                |
| hive-site       | Upload the hive-site.xml for Hive                                   | hive-site.xml                                                |
| auth            | SIMPLE, KERBEROS                                             | authentication method for the Hadoop cluster                                   |
| hadoop_username | Hadoop username                                                | Username to access the Hadoop cluster                                   |
| keytab          | keytab file                                           | The keytab file used in the KERBEROS authentication method                      |
| principal       |  principal of keytab                                   | principal of keytab                                      |
| krb5            | Kerberos krb5.conf configuration file.                 | Kerberos krb5.conf configuration file.                                |
| Properties      | (none)                                                  | The **warehouse** must be configured; when the metastore is **Custom**, an additional **catalog-impl** must be defined, and the user must put the jar package for the custom catalog implementation into the **{ARCTIC_HOME}/lib** directory, **and the service must be restarted to take effect**.|

We recommend users to create a Catalog following the guidelines below：

- If you want to use it in conjunction with HMS, select Hive as the Metastore, and choose the table format based on your needs. [Mixed Hive](../concepts/table-formats.md#Mixed-Hive-format) or Iceberg
- If you want to use  [Mixed Iceberg format](../concepts/table-formats.md#Mixed-Iceberg-format) provided by arctic ,choose `Arctic` as `Metastore`

The mapping between Catalog and Table formats is as follows:

| **Catalog Metastore ** | **Table format**                       | **Description**                               |
| -------------------------- | -------------------------------------- | -------------------------------------- |
| Arctic Metastore           | Mixed Iceberg                          | Iceberg-compatible tables, using AMS to store table metadata |
| Hive Metastore             | Mixed Hive                             | Hive-compatible tables                            |
| Hive Metastore             | Iceberg                                | Native Iceberg table, accessible as a Hive external table |
| Hadoop                     | Iceberg                                | Native Iceberg table                        |
| Custom                     | Iceberg                                | Native Iceberg table                        |

It is possible to edit and save the attributes of a catalog on the catalog details page.

## Delete Catalog
When a user needs to delete a Catalog, they can go to the details page of the Catalog and click the Remove button at the bottom of the page to perform the deletion.

???+ Attention

    Before deleting a Catalog, AMS will verify whether there is metadata for tables under that Catalog. 
    If there are still tables under that Catalog, AMS will prompt that the deletion failed.

