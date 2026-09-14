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

import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'

const html = readFileSync(new URL('../index.html', import.meta.url), 'utf8')
const inlineScripts = [...html.matchAll(/<script(?![^>]*type=["']module["'])[^>]*>([\s\S]*?)<\/script>/gi)]
const recoveryScripts = inlineScripts.filter(match => match[1].includes('vite:preloadError'))

assert.equal(recoveryScripts.length, 1, 'index.html should contain one preload recovery script')
assert.ok(
  html.indexOf('vite:preloadError') < html.indexOf('type="module"'),
  'the preload recovery listener must be registered before the application module',
)

const listeners = new Map()
const storage = new Map()
let reloadCount = 0
const window = {
  addEventListener(type, listener) {
    listeners.set(type, listener)
  },
  location: {
    reload() {
      reloadCount += 1
    },
  },
  sessionStorage: {
    getItem(key) {
      return storage.get(key) ?? null
    },
    removeItem(key) {
      storage.delete(key)
    },
    setItem(key, value) {
      storage.set(key, value)
    },
  },
}

vm.runInNewContext(recoveryScripts[0][1], { window })

const preloadListener = listeners.get('vite:preloadError')
assert.equal(typeof preloadListener, 'function')

const firstError = {
  defaultPrevented: false,
  payload: new Error('Unable to preload CSS for /assets/old.css'),
  preventDefault() {
    this.defaultPrevented = true
  },
}
preloadListener(firstError)
assert.equal(firstError.defaultPrevented, true)
assert.equal(reloadCount, 1)

const repeatedError = {
  defaultPrevented: false,
  payload: new Error('Unable to preload CSS for /assets/old.css'),
  preventDefault() {
    this.defaultPrevented = true
  },
}
preloadListener(repeatedError)
assert.equal(repeatedError.defaultPrevented, false, 'a failed recovery must not enter a reload loop')
assert.equal(reloadCount, 1)

preloadListener({
  payload: new Error('Unable to preload CSS for /assets/new.css'),
  preventDefault() {},
})
assert.equal(reloadCount, 2, 'a different missing asset should trigger a new recovery')
