---
title: "Mixed-Hive"
url: mixed-hive-format
aliases:
    - "formats/mixed-hive"
menu:
    main:
        parent: Formats
        weight: 400
---
# Mixed-Hive Format

Mixed Hive format is a format that has better compatibility with Hive than Mixed Iceberg format.
Mixed Hive format uses a Hive table as the BaseStore and an Iceberg table as the ChangeStore. Mixed Hive format supports:
- schema, partition, and types consistent with Hive format
- Using the Hive connector to read and write Mixed Hive format tables as Hive tables
- Upgrading a Hive table in-place to a Mixed Hive format table without data rewriting or migration, with a response time in seconds
- All the functional features of Mixed Iceberg format

The structure of Mixed Hive format is shown below:

<left>
![Mixed Hive format](../images/formats/mixed_hive_format.png){:height="80%" width="80%"}
</left>

In the BaseStore, files under the Hive location are also indexed by the Iceberg manifest, avoiding data redundancy between the two formats.
Mixed Hive format combines the snapshot, ACID, and MVCC features of Iceberg, and provides a great degree of compatibility with Hive, offering flexible selection and extension options for data platforms, processes, and products built around Hive format in the past.

{{< hint info >}}
The freshness of data under the Hive location is guaranteed by Full optimizing.
Therefore, the timeliness of native Hive reads is significantly different from that of Mixed Iceberg tables.
It is recommended to use Merge-on-read to read data with freshness in the order of minutes in Mixed Hive format.
{{< /hint >}}


