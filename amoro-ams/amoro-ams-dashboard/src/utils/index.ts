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

/**
 * Convert units to B KB MB G T
 */
export function bytesToSize(size: number | null): string {
  if (size === 0)
    return '0'
  if (size === null || size === undefined)
    return 'unknown'

  const num = 1024 // byte
  if (size < num)
    return `${size} B`
  if (size < num ** 2)
    return `${(size / num).toFixed(2)} KB`
  if (size < num ** 3)
    return `${(size / num ** 2).toFixed(2)} MB`
  if (size < num ** 4)
    return `${(size / num ** 3).toFixed(2)} G`

  return `${(size / num ** 4).toFixed(2)} T` // T
}

/**
 * Convert MB to MB G T
 */
export function mbToSize(size: number): string {
  if (size === 0)
    return '0'

  const num = 1024 // byte
  if (size < num)
    return `${size} MB`

  if (size < num ** 2)
    return `${(size / num).toFixed()} G`

  return `${(size / num ** 2).toFixed()} T` // T
}
/**
 * Convert ms to d h min s
 */
export function formatMS2Time(time: number, fromHour?: boolean): string {
  if (time === null || time === undefined || Number.isNaN(time)) {
    return '-'
  }
  // 3h 34min 12s
  const Second = 1000
  const Minute = Second * 60
  const Hour = Minute * 60
  const Day = Hour * 24
  if (time === 0) {
    return '0 ms'
  }
  if (time < Second) {
    return fromHour ? '0 min ' : `${time} ms`
  }
  if (time >= Second && time < Minute) {
    const s = Math.floor(time / Second)
    if (fromHour) {
      return '0 min'
    }
    return s ? `${s} s` : ''
  }
  if (time >= Minute && time < Hour) {
    const calcMin = Math.floor(time / Minute)
    const s = Math.floor((time - calcMin * Minute) / Second)
    if (fromHour) {
      return `${calcMin} min`
    }
    else {
      return time % Minute === 0 ? `${time / Minute} min` : `${calcMin} min ${s ? `${s} s` : ''}`
    }
  }
  if (time % Hour === 0) {
    return `${time / Hour} h`
  }
  if (time >= Hour && time < Day) {
    const calcHour = Math.floor(time / Hour)
    return `${calcHour} h ${formatMS2Time(time - calcHour * Hour, true)}`
  }
  if (time % Day === 0) {
    return `${time / Day} d`
  }

  const calcDay = Math.floor(time / Day)
  return `${calcDay} d ${formatMS2Time(time - calcDay * Day, true)}`
}
/**
 * Convert milliseconds to d h min s format
 * Less than or equal to one hour Display the exact second value，like 723s；
 * 3600s<x≤1440min display minute-level values，like 234min；
 * 1440min<x≤7200h display hourly values, like 45h；
 * more than 30d display >30d
 */
export function formatMS2DisplayTime(time: number): string {
  if (time === null || time === undefined || Number.isNaN(time)) {
    return ''
  }
  const Second = 1000
  const Minute = Second * 60
  const Hour = Minute * 60
  const Day = Hour * 24
  if (time === 0) {
    return '0 ms'
  }
  if (time <= Hour) {
    return `${Math.floor(time / Second)} s`
  }
  if (time > Hour && time <= Day) {
    return `${Math.floor(time / Minute)} min`
  }
  if (time > Day && time <= (30 * Day)) {
    return `${Math.floor(time / Hour)} h`
  }
  return '>30 d'
}

export function timeConversion(millisec: number) {
  const seconds = (millisec / 1000).toFixed(1)

  const minutes = (millisec / (1000 * 60)).toFixed(1)

  const hours = (millisec / (1000 * 60 * 60)).toFixed(1)

  const days = (millisec / (1000 * 60 * 60 * 24)).toFixed(1)

  if (+seconds < 60) {
    return `${seconds} s`
  }
  else if (+minutes < 60) {
    return `${minutes} min`
  }
  else if (+hours < 24) {
    return `${hours} h`
  }
  else {
    return `${days} day`
  }
}

export const dateFormat = (() => {
  const padZero = function (val: string) {
    const value = val || ''
    return value.length < 2 ? `0${value}` : value
  }
  const MAPS: any = {
    yyyy: (date: { getFullYear: () => number }) => date.getFullYear(),
    MM: (date: { getMonth: () => number }) => padZero(String(date.getMonth() + 1)),
    dd: (date: { getDate: () => number }) => padZero(String(date.getDate())),
    HH: (date: { getHours: () => number }) => padZero(String(date.getHours())),
    mm: (date: { getMinutes: () => number }) => padZero(String(date.getMinutes())),
    ss: (date: { getSeconds: () => number }) => padZero(String(date.getSeconds())),
  }

  const trunk = new RegExp(Object.keys(MAPS).join('|'), 'g')

  return function (val: string | number, format = 'yyyy-MM-dd  HH:mm:ss') {
    if (!val) {
      return ''
    }
    let value: number | Date = +val
    value = new Date(value)
    return format.replace(trunk, capture => MAPS[capture](value))
  }
})()

export function debounce(func: any, timeout = 300) {
  let timer: number | undefined
  return (...args: any) => {
    clearTimeout(timer)
    timer = setTimeout(() => {
      func && func(args)
    }, timeout)
  }
}

export function getUUid() {
  return Math.random().toString(36).substr(2)
}

/**
 * get url query
 */
export function getQueryString(name: string, url?: string) {
  const reg = new RegExp(`(^|&)${name}=([^&]*)(&|$)`, 'i')
  const paramsUrl = url ? new URL(url) : window.location
  const r = paramsUrl.search.substr(1).match(reg)
  if (r != null) {
    return decodeURIComponent(r[2])
  }
  return null
}
