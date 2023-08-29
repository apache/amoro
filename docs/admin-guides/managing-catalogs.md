---
title: "Managing Catalogs"
url: managing-catalogs
aliases:
    - "admin-guides/managing-catalogs"
menu:
    main:
        parent: Admin Guides
        weight: 200
---
# Managing Catalogs

Users can import your test or online clusters through the catalog management function provided by the AMS Dashboard. Before adding a new Catalog,
please read the following guidelines and select the appropriate creation according to your actual needs.

## Create catalog
In Amoro, the catalog is a namespace for a group of libraries and tables. Under the catalog, it is further divided into different databases, and under each database, there are different tables. The name of a table in Amoro is uniquely identified by the format `catalog.database.table`. In practical applications, a catalog generally corresponds to a metadata service, such as the commonly used Hive Metastore in big data.

AMS can also serve as a metadata service. In order to differentiate the storage method of metadata, Amoro classifies the catalog type into `Internal Catalog` and `External Catalog`. Catalogs that use AMS as the metadata service are internal catalogs, while others are external catalogs. When creating an external catalog, you need to select the storage backend for its metadata, such as Hive, Hadoop, or Custom.

In addition, when defining a catalog, you also need to select the table format used under it. Currently, Amoro supports the following table formats:
[Iceberg](../iceberg-format/) 、[Mixed-Hive](../mixed-hive-format/)、[Mixed-Iceberg](../mixed-iceberg-format/).

You can create a catalog in the AMS frontend:
![create_catalog](../images/admin/create-catalog.png)

### Configure basic information

- name: catalog name, only numbers, letters, _, - , starting with letters are supported (lower case letters are recommended)
- type: Internal Catalog or External Catalog
- metastore: storage type for table metadata. Hive Metastore (for using HMS to store metadata), Hadoop (corresponding to iceberg's Hadoop catalog), Custom (other iceberg catalog implementations).
- table format: Iceberg 、Mixed-Hive、Mixed  Iceberg. Currently, only Hive Metastore supports both Mixed-Hive and Iceberg format table, only internal catalog support both Mixed-Iceberg and Iceberg format table. All other metastore types only support Iceberg, currently a Metastore only supports one kind of TableFormat, in the future will support multiple
- optimizer group: tables under the catalog will automatically perform self-optimizing within this group.

### Configure storage
- core-site: the core-site.xml of the hadoop cluster
- hdfs-site: the hdfs-site.xml of the hadoop cluster
- hive-site: the hive-site.xml for Hive

### Configure authentication
- auth - SIMPLE or KERBEROS
- hadoop username: username of the hadoop cluster
- keytab: keytab file
- principal: principal of keytab
- krb5: Kerberos krb5.conf configuration file

### Configure properties
Common properties include:
- warehouse: Warehouse **must be configured**, as it determines where our database and table files should be placed
- catalog-impl: when the metastore is **Custom**, an additional catalog-impl must be defined, and the user must put the jar package for the custom catalog implementation into the **{ARCTIC_HOME}/lib** directory, **and the service must be restarted to take effect**
- table.*: If you want to add the same table configuration to all tables under a catalog, you can add `table.` before the configuration key to indicate that it is a table-level configuration. For example, `table.self-optimizing.group`
- table.filter-regular-expression: If you want to filter the same table under a database, you can add `table.filter-regular-expression` before the configuration key to indicate that it is a table-level configuration. For example, `table.filter-regular-expression`='(A\.a)|(B\.b)'

We recommend users to create a Catalog following the guidelines below：

- If you want to use it in conjunction with HMS, choose `External Catalog` for the `Type` and `Hive Metastore` for the `Metastore`, and choose the table format based on your needs, Mixed-Hive or Iceberg.
- If you want to use Mixed-Iceberg provided by amoro, choose `Internal Catalog` for the `Type` and `Mixed-Iceberg` for the table format.

## Delete catalog
When a user needs to delete a Catalog, they can go to the details page of the Catalog and click the Remove button at the bottom of the page to perform the deletion.

{{< hint info >}}
Before deleting a Catalog, AMS will verify whether there is metadata for tables under that Catalog.
If there are still tables under that Catalog, AMS will prompt that the deletion failed.
{{< /hint >}}