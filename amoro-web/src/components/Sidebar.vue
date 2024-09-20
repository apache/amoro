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
import { computed, defineComponent, nextTick, reactive, ref, toRefs, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import useStore from '@/store/index'
import TableMenu from '@/components/tables-sub-menu/TablesMenu.vue'
import { getQueryString } from '@/utils'

interface MenuItem {
  key: string
  title: string
  icon: string
}

export default defineComponent({
  name: 'Sidebar',
  components: {
    TableMenu,
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const route = useRoute()
    const store = useStore()

    const state = reactive({
      collapsed: false,
      selectedKeys: [] as string[],
    })
    const hasToken = computed(() => {
      return !!(getQueryString('token') || '')
    })
    const timer = ref(0)
    const menuList = computed(() => {
      const menu: MenuItem[] = [
        {
          key: 'tables',
          title: t('tables'),
          icon: 'TableOutlined',
        },
      ]
      const allMenu: MenuItem[] = [
        {
          key: 'overview',
          title: t('overview'),
          icon: 'overview',
        },
        {
          key: 'tables',
          title: t('tables'),
          icon: 'tables',
        },
        {
          key: 'catalogs',
          title: t('catalogs'),
          icon: 'catalogs',
        },
        {
          key: 'optimizing',
          title: t('optimizing'),
          icon: 'optimizers',
        },
        {
          key: 'terminal',
          title: t('terminal'),
          icon: 'terminal',
        },
        {
          key: 'settings',
          title: t('settings'),
          icon: 'settings',
        },
      ]
      return hasToken.value ? menu : allMenu
    })

    const setCurMenu = () => {
      const pathArr = route.path.split('/')
      if (route.path) {
        const routePath = [pathArr[1]]
        state.selectedKeys = routePath.includes('hive-tables') ? ['tables'] : routePath
      }
    }
    watchEffect(() => {
      setCurMenu()
    })
    const toggleCollapsed = () => {
      state.collapsed = !state.collapsed
      // The stretch animation is 300 ms and the trigger method needs to be delayed here
      setTimeout(() => {
        window.dispatchEvent(new Event('resize'))
      }, 300)
    }

    const navClick = (item: MenuItem) => {
      if (item.key === 'tables') {
        nextTick(() => {
          setCurMenu()
        })
        return
      }
      router.replace({
        path: `/${item.key}`,
      })
      nextTick(() => {
        setCurMenu()
      })
    }

    const mouseenter = (item: MenuItem) => {
      toggleTablesMenu(item.key === 'tables')
    }

    const goCreatePage = () => {
      toggleTablesMenu(false)
      router.push({
        path: '/tables/create',
      })
    }

    function toggleTablesMenu(flag = false) {
      if (hasToken.value) {
        return
      }
      timer.value && clearTimeout(timer.value)
      const time = flag ? 0 : 200
      timer.value = setTimeout(() => {
        store.updateTablesMenu(flag)
      }, time)
    }

    const viewOverview = () => {
      router.push({
        path: '/overview',
      })
    }

    return {
      ...toRefs(state),
      hasToken,
      menuList,
      toggleCollapsed,
      navClick,
      mouseenter,
      store,
      toggleTablesMenu,
      goCreatePage,
      viewOverview,
    }
  },
})
</script>

<template>
  <div :class="{ 'side-bar-collapsed': collapsed }" class="side-bar">
    <div :class="{ 'logo-collapsed': collapsed }" class="logo g-flex-ae" @mouseenter="toggleTablesMenu(false)" @click="viewOverview">
      <img src="../assets/images/logo1.svg" class="logo-img" alt="">
      <img v-show="!collapsed" src="../assets/images/arctic-dashboard1.svg" class="arctic-name" alt="">
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="inline"
      theme="dark"
      :inline-collapsed="collapsed"
    >
      <a-menu-item v-for="item in menuList" :key="item.key" :class="{ 'active-color': (store.isShowTablesMenu && item.key === 'tables'), 'table-item-tab': item.key === 'tables' }" @click="navClick(item)" @mouseenter="mouseenter(item)">
        <template #icon>
          <svg-icon :icon-class="item.icon" class="svg-icon" />
        </template>
        <span>{{ item.title }}</span>
      </a-menu-item>
    </a-menu>
    <a-button type="link" class="toggle-btn" @click="toggleCollapsed">
      <MenuUnfoldOutlined v-if="collapsed" />
      <MenuFoldOutlined v-else />
    </a-button>
    <div v-if="store.isShowTablesMenu && !hasToken" :class="{ 'collapsed-sub-menu': collapsed }" class="tables-menu-wrap" @click.self="toggleTablesMenu(false)" @mouseleave="toggleTablesMenu(false)" @mouseenter="toggleTablesMenu(true)">
      <TableMenu @go-create-page="goCreatePage" />
    </div>
  </div>
</template>

<style lang="less" scoped>
  .side-bar {
    position: relative;
    height: 100%;
    transition: width 0.3s;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    :deep(.ant-menu) {
      height: 100%;
      width: 200px;
      &.ant-menu-inline-collapsed {
        width: 64px;
        .logo {
          padding-left: 14px;
        }
        .toggle-btn {
          position: absolute;
          right: -68px;
          top: 8px;
          font-size: 18px;
          padding: 0 24px;
        }
      }
    }
    :deep(.ant-menu-item) {
      margin: 0;
      padding-left: 22px !important;
      .ant-menu-title-content {
        width: 100%;
        margin-left: 12px;
      }
      &.active {
        background-color: @primary-color;
        color: #fff;
      }
      &.active-color {
        color: #fff;
        background-color: @dark-bg-color;
      }
      &:hover {
        color: #fff;
      }
      &.table-item-tab:hover {
        background-color: @dark-bg-color;
      }
    }
    .logo {
      padding: 12px 0 12px 16px;
      overflow: hidden;
      background-color: #001529;
      cursor: pointer;
      padding: 12px 20px;
    }
    .logo-img {
      width: 24px;
      height: 24px;
    }
    .arctic-name {
      width: 66px;
      margin: 4px 0 0 4px;
    }
    .toggle-btn {
      position: absolute;
      right: -68px;
      top: 8px;
      font-size: 18px;
      padding: 0 24px;
    }
    .svg-icon {
      font-size: 16px;
    }
  }
  .tables-menu-wrap {
    position: absolute;
    top: 0;
    left: 200px;
    right: 0;
    bottom: 0;
    z-index: 1100;
    &.collapsed-sub-menu {
      left: 64px;
    }
  }
</style>
