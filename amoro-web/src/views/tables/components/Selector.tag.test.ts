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

import { page } from 'vitest/browser'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, nextTick } from 'vue'
import Antd from 'ant-design-vue'
import { CheckOutlined, DeleteOutlined, DownOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { createI18n } from 'vue-i18n'
import Selector from './Selector.vue'
import SvgIcon from '@/components/svg-icon.vue'
import en from '@/language/en'
import zh from '@/language/zh'

const { serviceMock, messageMock, modalMock } = vi.hoisted(() => ({
  serviceMock: {
    getBranches: vi.fn(),
    getConsumers: vi.fn(),
    getTags: vi.fn(),
    createTag: vi.fn(),
    deleteTag: vi.fn(),
  },
  messageMock: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn(),
    loading: vi.fn(),
  },
  modalMock: {
    confirm: vi.fn(),
  },
}))

vi.mock('@/services/table.service', () => ({
  getBranches: serviceMock.getBranches,
  getConsumers: serviceMock.getConsumers,
  getTags: serviceMock.getTags,
  createTag: serviceMock.createTag,
  deleteTag: serviceMock.deleteTag,
}))

vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual<typeof import('ant-design-vue')>('ant-design-vue')
  return {
    ...actual,
    message: messageMock,
    Modal: { ...actual.Modal, confirm: modalMock.confirm },
  }
})

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: 'en',
  fallbackLocale: 'en',
  messages: { en, zh },
})

let app: ReturnType<typeof createApp> | undefined

/**
 * Mounts the Selector into a container that fills the viewport so screenshots capture the
 * real ant-design-vue markup produced by the component.
 */
async function mountSelector() {
  const container = document.createElement('div')
  container.id = 'app'
  container.style.width = '900px'
  container.style.padding = '24px'
  container.style.background = '#fff'
  document.body.appendChild(container)

  app = createApp(Selector, {
    catalog: 'catalog',
    db: 'db',
    table: 'table',
  })
  app.use(Antd)
  app.use(i18n)
  // The app normally resolves these through unplugin-vue-components; register them explicitly.
  app.component('svg-icon', SvgIcon)
  app.component('DownOutlined', DownOutlined)
  app.component('CheckOutlined', CheckOutlined)
  app.component('DeleteOutlined', DeleteOutlined)
  app.component('PlusOutlined', PlusOutlined)
  app.mount(container)
  await settle()
  return container
}

/** Waits for pending promises and Vue re-renders so the DOM is stable before asserting. */
async function settle() {
  await vi.waitFor(async () => {
    await nextTick()
  })
  await new Promise(resolve => setTimeout(resolve, 60))
  await nextTick()
}

function q<T extends Element = HTMLElement>(selector: string): T | null {
  return document.querySelector<T>(selector)
}

function qa<T extends Element = HTMLElement>(selector: string): T[] {
  return Array.from(document.querySelectorAll<T>(selector))
}

/** Opens the tags tab inside the dropdown overlay. */
async function openTagsTab() {
  // The dropdown overlay is only mounted after the trigger button is clicked.
  const trigger = q<HTMLElement>('.branch-btn')
  expect(trigger, 'the branch selector button should be rendered').toBeTruthy()
  trigger!.click()
  await settle()

  const tabs = qa<HTMLElement>('.ant-tabs-tab')
  const tagsTab = tabs.find(tab => tab.textContent?.toLowerCase().includes('tag'))
  expect(tagsTab, 'the tags tab should be rendered').toBeTruthy()
  tagsTab!.click()
  await settle()
}

/** Opens the create-tag modal through the visible footer entry. */
async function openCreateTagModal() {
  const entry = q<HTMLElement>('.create-tag-btn-wrapper button')
  expect(entry, 'the create tag entry should be rendered').toBeTruthy()
  entry!.click()
  await settle()
  expect(q('.ant-modal'), 'the create tag modal should be visible').toBeTruthy()
}

/** Types into an ant-design-vue input and fires the events Vue listens to. */
async function typeInto(selector: string, value: string) {
  const input = q<HTMLInputElement>(selector)
  expect(input, `input ${selector} should exist`).toBeTruthy()
  input!.value = value
  input!.dispatchEvent(new Event('input', { bubbles: true }))
  input!.dispatchEvent(new Event('change', { bubbles: true }))
  await settle()
}

async function clickModalOk() {
  const buttons = qa<HTMLElement>('.ant-modal-footer button')
  const ok = buttons[buttons.length - 1]
  expect(ok, 'the modal ok button should exist').toBeTruthy()
  ok.click()
  await settle()
}

describe('selector tag management', () => {
  beforeEach(() => {
    serviceMock.getBranches.mockResolvedValue({ list: [{ name: 'main' }] })
    serviceMock.getConsumers.mockResolvedValue({ list: [] })
    serviceMock.getTags.mockResolvedValue({ list: [{ name: 'existing_tag' }] })
    serviceMock.createTag.mockResolvedValue({})
    serviceMock.deleteTag.mockResolvedValue({})
  })

  afterEach(() => {
    app?.unmount()
    app = undefined
    document.body.innerHTML = ''
    vi.clearAllMocks()
  })

  it('renders the create tag entry and existing tags', async () => {
    await mountSelector()
    await openTagsTab()

    expect(q('.create-tag-btn-wrapper')).toBeTruthy()
    expect(document.body.textContent).toContain('existing_tag')

    await page.screenshot({ path: '__screenshots__/tags-tab-list.png' })
  })

  it('opens the create tag modal', async () => {
    await mountSelector()
    await openTagsTab()
    await openCreateTagModal()

    const modalText = q('.ant-modal')!.textContent || ''
    expect(modalText).toContain('Snapshot ID')

    await page.screenshot({ path: '__screenshots__/create-tag-modal-open.png' })
  })

  it('creates a tag and shows a success message', async () => {
    await mountSelector()
    await openTagsTab()
    await openCreateTagModal()

    const inputs = qa<HTMLInputElement>('.ant-modal input')
    expect(inputs.length, 'the modal should render tag name and snapshot id inputs').toBeGreaterThan(1)
    await typeInto('.ant-modal input', 'new_tag')

    const snapshotInput = qa<HTMLInputElement>('.ant-modal input')[1]
    snapshotInput.value = '  12345  '
    snapshotInput.dispatchEvent(new Event('input', { bubbles: true }))
    await settle()

    await page.screenshot({ path: '__screenshots__/create-tag-form-filled.png' })

    await clickModalOk()

    expect(serviceMock.createTag).toHaveBeenCalledWith({
      catalog: 'catalog',
      db: 'db',
      table: 'table',
      tagName: 'new_tag',
      snapshotId: '12345',
      maxRefAgeMs: undefined,
    })
    expect(messageMock.success).toHaveBeenCalledWith('Tag created successfully')
    // The list is refreshed after a successful creation.
    expect(serviceMock.getTags).toHaveBeenCalledTimes(2)
  })

  it('warns when the tag name is missing', async () => {
    await mountSelector()
    await openTagsTab()
    await openCreateTagModal()

    const snapshotInput = qa<HTMLInputElement>('.ant-modal input')[1]
    snapshotInput.value = '12345'
    snapshotInput.dispatchEvent(new Event('input', { bubbles: true }))
    await settle()

    await clickModalOk()

    expect(messageMock.warning).toHaveBeenCalledWith('Tag name is required')
    expect(serviceMock.createTag).not.toHaveBeenCalled()

    await page.screenshot({ path: '__screenshots__/create-tag-missing-name.png' })
  })

  it('keeps the tag list unchanged when creation fails', async () => {
    serviceMock.createTag.mockRejectedValue(new Error('Ref new_tag already exists in table'))
    await mountSelector()
    await openTagsTab()
    await openCreateTagModal()

    await typeInto('.ant-modal input', 'new_tag')
    const snapshotInput = qa<HTMLInputElement>('.ant-modal input')[1]
    snapshotInput.value = '12345'
    snapshotInput.dispatchEvent(new Event('input', { bubbles: true }))
    await settle()

    await clickModalOk()

    expect(messageMock.success).not.toHaveBeenCalled()
    expect(serviceMock.getTags).toHaveBeenCalledTimes(1)
  })

  it('asks for confirmation before deleting a tag', async () => {
    await mountSelector()
    await openTagsTab()

    const deleteButton = q<HTMLElement>('.tag-delete-btn')
    expect(deleteButton, 'a delete button should be rendered for the tag').toBeTruthy()
    deleteButton!.click()
    await settle()

    expect(modalMock.confirm).toHaveBeenCalledTimes(1)
    const options = modalMock.confirm.mock.calls[0][0]
    expect(options.title).toBe('Delete Tag')
    // Deletion must not happen before the user confirms.
    expect(serviceMock.deleteTag).not.toHaveBeenCalled()
  })

  it('deletes the tag after confirmation and refreshes the list', async () => {
    await mountSelector()
    await openTagsTab()

    q<HTMLElement>('.tag-delete-btn')!.click()
    await settle()

    const options = modalMock.confirm.mock.calls[0][0]
    // The backend list is now empty, so the refreshed view should not contain the tag.
    serviceMock.getTags.mockResolvedValue({ list: [] })
    await options.onOk()
    await settle()

    expect(serviceMock.deleteTag).toHaveBeenCalledWith({
      catalog: 'catalog',
      db: 'db',
      table: 'table',
      tagName: 'existing_tag',
    })
    expect(messageMock.success).toHaveBeenCalledWith('Tag deleted successfully')
    expect(serviceMock.getTags).toHaveBeenCalledTimes(2)
    expect(document.body.textContent).not.toContain('existing_tag')

    await openTagsTab()
    await page.screenshot({ path: '__screenshots__/delete-tag-call-success.png' })
  })

  it('falls back to the main branch when the selected tag is deleted', async () => {
    await mountSelector()
    await openTagsTab()

    // Select the tag itself so the fallback path is reachable.
    const tagItem = q<HTMLElement>('.ant-tabs-tabpane-active .branch-selector-item')
    expect(tagItem, 'the tag item should be selectable').toBeTruthy()
    tagItem!.click()
    await settle()
    expect(q('.branch-btn-label')!.textContent).toBe('existing_tag')
    await page.screenshot({ path: '__screenshots__/delete-tag-selected-tag.png' })

    q<HTMLElement>('.tag-delete-btn')!.click()
    await settle()
    const options = modalMock.confirm.mock.calls[0][0]
    // After deletion the tag list is empty and the active ref falls back to main.
    serviceMock.getTags.mockResolvedValue({ list: [] })
    await options.onOk()
    await settle()

    expect(q('.branch-btn-label')!.textContent).toBe('main')
    expect(document.body.textContent).not.toContain('existing_tag')

    // Re-open the dropdown so the screenshot shows the tags list after the deletion.
    await openTagsTab()
    expect(qa<HTMLElement>('.ant-tabs-tabpane-active .branch-selector-item').length).toBe(0)
    await page.screenshot({ path: '__screenshots__/delete-tag-fallback-to-main.png' })
  })
})
