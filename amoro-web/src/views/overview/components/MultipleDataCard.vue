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

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { IKeyAndValue } from '@/types/common.type'

const props = defineProps<{ title: string, data: IKeyAndValue[] }>()

const { t } = useI18n()
</script>

<template>
  <a-card class="multiple-data" :body-style="{ paddingTop: '0px' }">
    <template #title>
      <span class="card-title">{{ props.title }}</span>
    </template>
    <a-row>
      <a-col v-for="item in props.data" :key="item.key" :span="12">
        <a-statistic
          :title="t(item.key)" :value="item.value"
          :value-style="{ color: '#07A7F0', fontSize: '28px' }" style="margin-right: 20px;"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<style scoped>
.card-title {
  font-size: 28px;
}

.multiple-data {
  padding-top: 0px;
  height: 150px;
}
</style>
