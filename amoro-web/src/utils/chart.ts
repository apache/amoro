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

import { dateFormat } from '.'
import type { ECOption } from '@/components/echarts'
import i18n from '@/language/i18n'
import type { ILineChartOriginalData } from '@/types/common.type'

/**
 * Sort the line chart data in time order.
 * @param obj
 * @param sortCallback
 * @returns Echarts options
 */
function sortLineChartDataByKey(obj: ILineChartOriginalData = {}, sortCallback: ((a: string, b: string) => number) = (a, b) => (Number(a) - Number(b))): ILineChartOriginalData {
  const keys = Object.keys(obj)
  if (!keys.length) {
    return {}
  }
  const result: ILineChartOriginalData = {}
  keys.sort(sortCallback).forEach(key => (result[key] = obj[key]))
  return result
}

/**
 * Generate a line chart option based on the original data
 * @param titleText chart title text, needs to be internationalized
 * @param data
 * @returns Echarts options
 */
export function generateLineChartOption(_titleText: string, data: ILineChartOriginalData) {
  if (!data) {
    return {}
  }
  data = sortLineChartDataByKey(data)
  const dataKeys = Object.keys(data)
  const option: ECOption = {
    tooltip: {
      trigger: 'axis',
    },
    yAxis: {
      type: 'value',
      boundaryGap: [0, '1%'],
      splitNumber: 6,
    },
    xAxis: {
      type: 'category',
      data: dataKeys.map(d => dateFormat(d)),
      axisLabel: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLine: {
        show: false,
      },
    },
    grid: {
      left: 0,
      right: 0,
      top: 36,
      bottom: 6,
      containLabel: true,
    },
  }
  const legendMap: Record<string, number[]> = {}
  Object.values(data).forEach((val) => {
    const keys = Object.keys(val)
    keys.forEach((key) => {
      const tKey = i18n.global.t(key)
      if (!legendMap[tKey]) {
        legendMap[tKey] = []
      }
      legendMap[tKey].push(val[key])
    })
  })
  const legendKeys = Object.keys(legendMap)
  option.legend = {
    show: true,
    top: 0,
    left: 'center',
    orient: 'horizontal',
    itemWidth: 7,
    itemHeight: 7,
    icon: 'circle',
    itemGap: 6,
    textStyle: {
      fontSize: 13,
      lineHeight: 24,
    },
  }
  option.series = legendKeys.map(key => ({
    name: key,
    type: 'line',
    symbol: 'circle',
    data: legendMap[key],
  }))
  return option
}
