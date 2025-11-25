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

## Thrift API

### Install Thrift

To build and install the thrift compiler, run:

```
wget -nv https://archive.apache.org/dist/thrift/0.20.0/thrift-0.20.0.tar.gz
tar xzf thrift-0.20.0.tar.gz
cd thrift-0.20.0
chmod +x ./configure
./configure --disable-libs
sudo make install -j
```

If you're on OSX and use homebrew, you can instead install Thrift 0.20.0 with `brew` and ensure that it comes first in your `PATH`.

```
brew install thrift
export PATH="/usr/local/opt/thrift@0.20.0/bin:$PATH"
```

### Compile Thrift

```shell
./mvnw -pl amoro-common -Pthrift-gen generate-sources
```
