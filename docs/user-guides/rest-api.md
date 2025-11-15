---
title: "REST API"
url: rest-api
aliases:
    - "user-guides/rest-api"
menu:
    main:
        parent: User Guides
        weight: 600
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
# REST API

## Accessing Swagger UI

Access the Swagger UI at:

```
http://<host>:<port>/#/openapi-ui
```

You can also view the OpenAPI specification YAML in the repository at `amoro-ams/src/main/resources/openapi/openapi.yaml`, or use the [Swagger Editor](https://editor-next.swagger.io/?url=https://raw.githubusercontent.com/apache/amoro/master/amoro-ams/src/main/resources/openapi/openapi.yaml) online.

## Building the OpenAPI SDK

Build the OpenAPI SDK using Maven:

```bash
./mvnw clean package -pl amoro-openapi-sdk -am -Popenapi-sdk
```
