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
import { ref, shallowReactive, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { OverviwOperationItem } from '@/types/common.type'
import { getOverviewOperations } from '@/services/overview.service'
import { dateFormat } from '@/utils'

const { t } = useI18n()
const router = useRouter()
const loading = ref<boolean>(false)
const dataSource = reactive<OverviwOperationItem[]>([])
function goTableDetail(record: OverviwOperationItem) {
  const { catalog, database, tableName } = record.tableIdentifier
  router.push({
    path: '/tables',
    query: {
      catalog,
      db: database,
      table: tableName,
    },
  })
}

async function getOperationInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getOverviewOperations();
    (result || []).forEach((ele: OverviwOperationItem) => {
      ele.ts = ele.ts ? dateFormat(ele.ts) : ''
      dataSource.push(ele)
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

const columns = shallowReactive([
  { dataIndex: 'tableName', title: t('table'), ellipsis: true },
  { dataIndex: 'operation', title: t('operation'), width: '50%', ellipsis: true},
  { dataIndex: 'ts', title: t('time'), width: '25%', ellipsis: true },
])

onMounted(() => {
  getOperationInfo()
})
</script>

<template>
  <a-card class="operations-card" title="Latest Operations">
    <div class="list-wrap">
      <a-table class="ant-table-common" :loading="loading" :columns="columns" :data-source="dataSource" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'tableName'">
            <span :title="record.tableName" class="primary-link" @click="goTableDetail(record)">
              {{ record.tableName }}
            </span>
          </template>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<style lang="less" scoped>
.operations-card {
  height: 350px;
}

.list-wrap {
  .primary-link {
    color: @primary-color;

    &:hover {
      cursor: pointer;
    }

    &.disabled {
      color: #999;

      &:hover {
        cursor: not-allowed;
      }
    }
  }
}

</style>
