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
/ -->

<script lang="ts" setup>
import {ref } from 'vue'
import SingleDataCard from './components/SingleDataCard.vue';
import MultipleDataCard from './components/MultipleDataCard.vue';
import ResourceUsageCard from './components/ResourceUsageCard.vue';
import OptimizingTablesCard from './components/OptimizingTablesCard.vue';
import DataSizeCard from './components/DataSizeCard.vue';
// import SortedTablesCard from './components/SortedTablesCard.vue';

const sizeRandomData = (length: number, type: string) => {
  return Array.from({ length }, (_, index) => ({
    time: new Date(Date.now() - index * 60000).toISOString().slice(11, 19),
    size: Math.random() * 100,
  }));
};
const ResourceUsageRandomData = (length: number) => {
  return Array.from({ length }, (_, index) => ({
    time: new Date(Date.now() - index * 60000).toISOString().slice(11, 19),
    memory: Math.random(),
    cpu: Math.random()
  }));
};

const multipleData = ref([
  { subtitle: 'CPU', data: 50 },
  { subtitle: 'Memory', data: 60 }
]);

const resourceUsageData = ref(ResourceUsageRandomData(30));

const optimizingTables = ref([
  { name: 'Table 1', status: 'Optimizing', duration: '1h 20m' },
  { name: 'Table 2', status: 'Idle', duration: '0h 45m' },
]);

const dataSizeData = ref(sizeRandomData(30, 'size'));
const sortedTables = ref([
  { rank: 1, name: 'Table A', size: 500, files: 20 },
  { rank: 2, name: 'Table B', size: 450, files: 18 },
  { rank: 3, name: 'Table C', size: 400, files: 15 },
  // Add more entries as needed
]);


</script>

<template>
  <div :style="{ background: '#F8F7F8', padding: '24px', minHeight: '900px' }" class="overview-content">
    <a-row :gutter="[16,8]">
      <a-col :span=6>
        <single-data-card title="Catalog" :data=3 />
      </a-col>
      <a-col :span=6>
        <single-data-card title="Table" :data=243 />
      </a-col>
      <a-col :span=6>
        <single-data-card title="Data" :data="300.00" />
      </a-col>
      <a-col :span=6>
        <multiple-data-card title="Catalog" :items=multipleData />
      </a-col>
      <a-col :span=12>
        <resource-usage-card :data=resourceUsageData />
      </a-col>
      <a-col :span=12>
        <optimizing-tables-card title="Optimizing Tables" :tables=optimizingTables />
      </a-col>
      <a-col :span=12>
        <DataSizeCard title="Data Size" :data="dataSizeData" />
      </a-col>
      <a-col :span=12>
        <!-- <sorted-tables-card title="Top 10 Tables" :tables=sortedTables /> -->
      </a-col>
    </a-row>

  </div>
</template>

<style lang="less" scoped>
.home-section {
  margin: 0 auto;
  display: flex;
  justify-content: center;
  background-color: #fff;
  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 30px;
    .img {
      max-width: 100%;
    }
  }
}

.home-section .content .title,
.home-feature .content .title {
  font-size: 20px;
  font-weight: bold;
  line-height: 24px;
  text-align: center;
  margin-bottom: 40px;
}
.home-feature {
  background: #f5f6fa;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 41px 80px;
    .title {
      font-size: 32px;
    }
    .features {
      display: grid;
      grid-row-gap: 24px;
      grid-column-gap: 24px;
      max-width: 1100px;
      grid-template-columns: auto auto;
      .feature-item {
        background-color: #fff;
        padding: 32px;
        border-radius: 5px;
        position: relative;
        .fix-icon {
          position: absolute;
          top: 0px;
          right: 20px;
        }
      }
    }
  }
}

@media screen and (max-width: 800px) {
  .home-feature .features {
    grid-template-columns: auto;
  }
}

.home-feature .features .item-title {
  font-size: 24px;
  font-weight: bold;
  line-height: 28px;
  display: flex;
  align-items: center;
}

.home-feature .features .item-title img {
  margin-right: 13px;
}

.home-feature .features .item-desc {
  margin-top: 24px;
  font-size: 14px;
  font-weight: normal;
  line-height: 22px;
  color: #53576a;
}
</style>
