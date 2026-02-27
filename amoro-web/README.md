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

This is the Dashboard frontend for Amoro Management Service (AMS).

## Requirements

- node >= `18.18.0`
- pnpm

## Installing Dependencies

In the `amoro-web` directory, run `pnpm install` to install all the dependencies.

## Running Locally

After completing the [Installing Dependencies](#Installing Dependencies) step, you're ready to start the project!

### Start a development server

<b>If You are a frontend developer</b>

In the `amoro-web` directory, run `pnpm dev:mock` to start a development server for the dashboard app at `http://127.0.0.1:8080`.

<b>If You not. You just running `pnpm dev`</b>

### Configure the ams Server to connect to

1. open the `vite.config.ts` under the folder `amoro-web`

2. find the `proxy` configuration then unpack annotations and change the `target` to you server location

```ts
proxy: {
  '^/ams': {
  // change the target to your backend server
  // Such as target: 'http://127.0.0.1:xxx',
  target: 'http://127.0.0.1:8080',
  changeOrigin: true,
  configure(_, options) {
      // configure proxy header here
      options.headers = {
        'cookie': 'JSESSIONID=node07rhpm05aujgi1amdr8stpj9xa4.node0',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': 'true',
        'Access-Control-Allow-Headers':
        'Content-Type, Content-Length, Authorization, Accept, X-Requested-With , yourHeaderFeild',
        'Access-Control-Allow-Methods': 'PUT,POST,GET,DELETE,OPTIONS'
      }
    }
  }
}
```

## Building Project

In the `amoro-web` directory, run `pnpm build` to prepare the dashboard for deployment.

## Top Level Directory Structure

```
amoro-web
 |-- mock
 |-- public
 |-- src
     |-- components
     |-- hooks
     |-- store
     |-- services
     |-- utils
     |-- views
     |-- main.tx
     |-- App.vue
 |-- vue.config.js
 |-- package.json
 |-- pnpm-lock
```

- [public](amoro-web/public): The index.html and favicon.ico of this project.
- [src](amoro-web/src): The source files of this project.
  - [src/components](amoro-web/src/components): All the shared components in the repo, can be used in all views.
  - [src/views](amoro-web/src/views): All the views.
  - [src/services](amoro-web/src/services): The services for the feature component to communicate with ams server.
  - [src/utils](amoro-web/src/utils): The utility functions in this project.
  - [src/App.vue](amoro-web/src/main.tsx): The entrance of this project.
- [vite.config.ts](/amoro-web/vite.config.ts): The configuration file of vue3.
- [package.json](amoro-web/package.json): The project build files and package management, it defines the scripts/tasks and the dependent packages for this project.
- [pnpm-lock.yaml](amoro-web/pnpm-lock.yaml): The package management pnpm lock file.
