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

<script setup lang="ts">
import { defineProps, ref } from 'vue';
const props = defineProps<{ title: string; items: { subtitle: string; data: number; precision: number; suffix: string }[] }>();

const loading = ref(true);
const fetchData = () => {
  loading.value = false;
};
fetchData();

</script>

<template>
  <a-card :loading="loading">
    <template #title>
      <span class="card-title">{{ props.title }}</span>
    </template>
    <a-row>
      <a-col :span=12 v-for="item in props.items" :key="item.subtitle" >
        <a-statistic :title="item.subtitle" :value="item.data" :precision="item.precision" :suffix="item.suffix" :value-style="{ color: '#07A7F0' }"
          style="margin-right: 50px">
        </a-statistic>
      </a-col>
    </a-row>

    <!-- <div v-for="item in props.items" :key="item.subtitle">
      <p>{{ item.subtitle }}</br> {{ item.data }}</p>
    </div> -->
  </a-card>
</template>

<style scoped>
.card-title {
  font-size: 28px;
}
</style>