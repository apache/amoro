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

- node `16.x`
- yarn `1.x` (optional)

## Installing Dependencies

In the `ams/dashboard` directory, run `yarn install` or `npm install` to install all the dependencies.

## Running Locally

After completing the [Installing Dependencies](#Installing Dependencies) step, you're ready to start the project!

### Configure the ams Server to connect to

1. open the `vue.config.js` under the folder `ams/dashboard`

2. find the `ENV_HOST` configuration and change the environment corresponding to the `DEV`

```
const ENV_HOST = {
  DEV: 'http://127.0.0.1:1630/', // Change it to the address of your development server
  TEST: '',
  ONLINE: ''
}
```

### Start a development server

In the `ams/dashboard` directory, run `yarn serve` or `npm run serve` to start a development server for the dashboard app at `http://127.0.0.1:8080`.

## Building Project

In the `ams/dashboard` directory, run `yarn build` or `npm run build` to prepare the dashboard for deployment.

Since we don't currently have a front-end CI configured, we must execute the `yarn build` or `npm run build` command before committing the front-end code each time, and commit the static file changes in the `ams/dashboard/src/main/resources/static` directory along with it.

In addition, you can run `yarn build:vue-dev` or `npm run build:vue-dev` to create dev build for the dashboard.

## Top Level Directory Structure
```
ams/dashboard
 |-- mock
 |-- tests
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
 |-- yarn.lock
```
- [public](ams/dashboard/public): The index.html and favicon.ico of this project.
- [src](ams/dashboard/src): The source files of this project.
  - [src/components](ams/dashboard/src/components): All the shared components in the repo, can be used in all views.
  - [src/views](ams/dashboard/src/views): All the views.
  - [src/services](ams/dashboard/src/services): The services for the feature component to communicate with ams server.
  - [src/utils](ams/dashboard/src/utils): The utility functions in this project.
  - [src/App.vue](ams/dashboard/src/main.tsx): The entrance of this project.
- [vue.config.js](ams/dashboard/vue.config.js): The configuration file of vue3.
- [package.json](ams/dashboard/package.json): The project build files and package management, it defines the scripts/tasks and the dependent packages for this project.
- [yarn.lock](ams/dashboard/yarn.lock): The package management yarn lock file.
