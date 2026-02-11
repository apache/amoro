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

<script lang="ts">
import { computed, defineComponent } from 'vue'
import { useRoute } from 'vue-router'
import SideBar from '@/components/Sidebar.vue'
import TopBar from '@/components/Topbar.vue'

export default defineComponent({
  name: 'Layout',
  components: {
    SideBar,
    TopBar,
  },
  props: {
    menus: {
      type: Array,
      default: () => [],
    },
    showTopBar: {
      type: Boolean,
      default: true,
    },
  },
  setup() {
    const route = useRoute()
    const isTablesPage = computed(() => route.path.includes('/tables'))

    return {
      isTablesPage,
    }
  },
})
</script>

<template>
  <div class="layout">
    <!-- sidebar -->
    <SideBar />
    <div class="right-content">
      <!-- topbar -->
      <TopBar v-if="showTopBar" />
      <!-- content -->
      <div class="content" :class="{ 'content--workspace': isTablesPage }">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100%;
  min-width: 1200px;
  .right-content {
    display: flex;
    flex: 1;
    flex-direction: column;
    transition: width 0.3s;
    overflow: hidden;
    .content {
      height: calc(100% - 48px);
      overflow: auto;
    }
    .content--workspace {
      overflow: hidden;
    }
  }
}
</style>
