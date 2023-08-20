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

import { ECOption } from '@/components/echarts'
import i18n from '@/language/i18n'
import { ILineChartOriginalData } from '@/types/common.type'
import { dateFormat } from '.'

/**
 * Sort the line chart data in time order.
 * @param obj
 * @param sortCallback
 * @returns
 */
const sortLineChartDataByKey = (obj: ILineChartOriginalData = {}, sortCallback: ((a: string, b: string) => number) = (a, b) => (Number(a) - Number(b))): ILineChartOriginalData => {
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
 * @returns
 */
export const generateLineChartOption = (titleText: string, data: ILineChartOriginalData) => {
  if (!data) {
    return {}
  }
  data = sortLineChartDataByKey(data)
  const option: ECOption = {
    tooltip: {
      trigger: 'axis'
    },
    yAxis: {
      type: 'value'
    },
    xAxis: {
      type: 'category',
      data: Object.keys(data).map(d => dateFormat(d))
    }
  }
  titleText && (option.title = {
    left: 'center',
    text: titleText
  })
  const legendMap: Record<string, number[]> = {}
  Object.values(data).forEach(val => {
    const keys = Object.keys(val)
    keys.forEach(key => {
      const tKey = i18n.global.t(key)
      if (!legendMap[tKey]) {
        legendMap[tKey] = []
      }
      legendMap[tKey].push(val[key])
    })
  })
  option.legend = { orient: 'vertical', left: 'right', top: 'middle', data: Object.keys(legendMap) }
  option.series = Object.keys(legendMap).map(key => ({
    name: key,
    type: 'line',
    data: legendMap[key]
  }))
  return option
}
