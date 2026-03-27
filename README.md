<!--
 - Licensed to the Apache Software Foundation (ASF) under one
 - or more contributor license agreements.  See the NOTICE file
 - distributed with this work for additional information
 - regarding copyright ownership.  The ASF licenses this file
 - to you under the Apache License, Version 2.0 (the
 - "License"); you may not use this file except in compliance
 - with the License.  You may obtain a copy of the License at
 - 
 -     http://www.apache.org/licenses/LICENSE-2.0
 - 
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and 
 - limitations under the License.
-->
<p align="center">
  <img src="https://amoro.apache.org/img/amoro-logo.svg" alt="Amoro logo" height="120px"/>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0.html">
    <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" />
  </a>
  <a href="https://github.com/apache/amoro/actions/workflows/core-hadoop3-ci.yml">
    <img src="https://github.com/apache/amoro/actions/workflows/core-hadoop3-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/apache/amoro/actions/workflows/core-hadoop2-ci.yml">
    <img src="https://github.com/apache/amoro/actions/workflows/core-hadoop2-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/apache/amoro/actions/workflows/trino-ci.yml">
    <img src="https://github.com/apache/amoro/actions/workflows/trino-ci.yml/badge.svg" />
  </a>
  <a href="https://deepwiki.com/apache/amoro">
    <img src="https://deepwiki.com/badge.svg" alt="Ask DeepWiki">
  </a>
</p>

Apache Amoro (incubating) is a Lakehouse management system built on open data lake formats.
Working with compute engines including Flink, Spark, and Trino, Amoro brings pluggable and self-managed features for Lakehouse to provide out-of-the-box data warehouse experience,
and helps data platforms or products easily build infra-decoupled, stream-and-batch-fused and lake-native architecture.

Learn more about Amoro at https://amoro.apache.org/, contact the developers and community on the [mailing list](https://amoro.apache.org/join-community/#mailing-lists) if you need any help.

## Getting Started

The quickest way to experience Amoro is to use the [Quick Start Guide](https://amoro.apache.org/docs/latest/quickstart/setup/). You can also start the Amoro Management Service (AMS) and its components using Docker:

```bash
docker run -p 1630:1630 -p 1260:1260 neteaseamoro/amoro:latest
```
Access the dashboard at `http://localhost:1630` (Default: admin/admin).

## Architecture

Here is the architecture diagram of Amoro:

<p align="center">
  <img src="https://amoro.apache.org/img/home-content.png" alt="Amoro architecture" height="360px"/>
</p>

* AMS: Amoro Management Service provides Lakehouse management features, like self-optimizing, data expiration, etc.
  It also provides a unified catalog service for all compute engines, which can also be combined with existing metadata services.
* Plugins: Amoro provides a wide selection of external plugins to meet different scenarios.
    * Optimizers: The self-optimizing execution engine plugin asynchronously performs merging, sorting, deduplication,