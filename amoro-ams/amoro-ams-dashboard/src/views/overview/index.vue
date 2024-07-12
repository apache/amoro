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
import { ref } from 'vue'
import SingleDataCard from './components/SingleDataCard.vue'
import MultipleDataCard from './components/MultipleDataCard.vue'
import UnhealthTablesCard from './components/UnhealthTablesCard.vue'
import OperationsCard from './components/OperationsCard.vue'
import PieChartCard from './components/PieChartCard.vue'

const cardsData = [
  { title: 'Catalog', data: 3, precision: 0, suffix: '' },
  { title: 'Table', data: 234, precision: 0, suffix: '' },
  { title: 'Data', data: 300.00, precision: 2, suffix: 'GB' },
]

const multipleData = ref([
  { subtitle: 'CPU', data: 96, precision: 0, suffix: 'Core' },
  { subtitle: 'Memory', data: 129, precision: 0, suffix: 'GB' },
])

const tableFormatData = ref([
  { value: 70, name: 'Iceberg format' },
  { value: 20, name: 'Mixed-Iceberg' },
  { value: 10, name: 'Mixed-Hive format' },
])

const OptimizingStatusData = ref([
  { value: 40, name: 'Full Optimizing' },
  { value: 20, name: 'Major Optimizing' },
  { value: 30, name: 'Minor Optimizing' },
  { value: 10, name: 'Committing' },
  { value: 2, name: 'Planning' },
  { value: 3, name: 'Pending' },
  { value: 50, name: 'Idle' },
])
</script>

<template>
  <div :style="{ background: '#F8F7F8', padding: '24px', minHeight: '900px' }" class="overview-content">
    <a-row :gutter="[16, 8]">
      <a-col v-for="(card, index) in cardsData" :key="index" :span="6">
        <SingleDataCard :title="card.title" :data="card.data" :precision="card.precision" :suffix="card.suffix" />
      </a-col>
      <a-col :span="6">
        <MultipleDataCard title="Resource" :items="multipleData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard title="Table Format" :data="tableFormatData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard title="Optimizing Status" :data="OptimizingStatusData" />
      </a-col>
      <a-col :span="12">
        <UnhealthTablesCard />
      </a-col>
      <a-col :span="12">
        <OperationsCard />
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
