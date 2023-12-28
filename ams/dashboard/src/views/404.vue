
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
/-->

<template>
  <div class="g-flex-center">
    <div class="g-flex-col container">
      <p class="title">404 - Not Found</p>
      <router-link to="/" style="text-align: center">
        <a-button class="button g-mt-32">{{$t('backHome')}}</a-button>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Page404'
})
</script>

<style lang="less" scoped>
.container {
  padding-top: 100px;
  .title {
    font-size: 20px;
    font-weight: bold;
  }
  .button {
    background: #1890ff;
    border-radius: 3px;
    color: #ffffff;
  }
}
</style>
