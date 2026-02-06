/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import type { Ref } from 'vue'
import { onActivated, onBeforeUnmount, onDeactivated, onMounted, ref } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { useRoute } from 'vue-router'

const pageScrollPositions: Record<string, number> = {}

function getRouteKey(route: RouteLocationNormalizedLoaded): string {
  if (route.name) {
    return String(route.name)
  }
  return route.path
}

export interface PageScrollController {
  pageScrollRef: Ref<HTMLElement | null>
}

export function usePageScroll(): PageScrollController {
  const route = useRoute()
  const pageScrollRef = ref<HTMLElement | null>(null)

  const restoreScroll = () => {
    const el = pageScrollRef.value
    if (!el) {
      return
    }
    const key = getRouteKey(route)
    const top = pageScrollPositions[key] ?? 0
    el.scrollTop = top
  }

  const saveScroll = () => {
    const el = pageScrollRef.value
    if (!el) {
      return
    }
    const key = getRouteKey(route)
    pageScrollPositions[key] = el.scrollTop
  }

  onMounted(restoreScroll)
  onActivated(restoreScroll)
  onBeforeUnmount(saveScroll)
  onDeactivated(saveScroll)

  return {
    pageScrollRef,
  }
}
