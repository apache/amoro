---
title: "Paimon"
url: paimon-format
aliases:
- "formats/paimon"
menu:
main:
parent: Formats
weight: 200
---
# Paimon Format

Paimon format refers to [Apache Paimon](https://paimon.apache.org/) table.
Paimon is a streaming data lake platform with high-speed data ingestion, changelog tracking and efficient real-time analytics.

By registering Paimon's catalog with Amoro, users can view information such as Schema, Options, Files, Snapshots, DDLs, Compaction information, and more for Paimon tables.
Furthermore, they can operate on Paimon tables using Spark SQL in the Terminal. The current supported catalog types and file system types for Paimon are all supported.
For registering catalog operation steps, please refer to [Managing Catalogs](../managing-catalogs/).

