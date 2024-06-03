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

# How to build docker images

We provide a bash script to help you build docker image easier.

You can control which image is built by the follow parameters :

- amoro
- amoro-flink-optimizer
- amoro-spark-optimizer

e.g.

```shell
./build.sh amoro
```

- NOTICE: The amoro image, amoro-spark-optimizer image and amoro-flink-optimizer image required the project had been packaged.
  So run `mvn clean package -DskipTests` before building amoro, amoro-spark-optimizer, or amoro-flink-optimizer image.

You can speed up image building via 

```shell
./build.sh \
  --apache-archive https://mirrors.aliyun.com/apache \
  --debian-mirror https://mirrors.aliyun.com  \
  amoro-flink-optimizer
```

more options see `./build.sh --help`
