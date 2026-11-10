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

import type { DOMWrapper, VueWrapper } from '@vue/test-utils'
import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Selector from './Selector.vue'

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

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key }),
}))

/** Minimal stand-ins for the ant-design-vue components used by the Selector. */
const stubs = {
  'a-dropdown': {
    template: '<div class="dropdown-stub"><slot /><slot name="overlay" /></div>',
  },
  'a-button': {
    template: '<button v-bind="$attrs"><slot /></button>',
  },
  'a-input': {
    props: ['value'],
    emits: ['update:value'],
    template:
      '<input class="input-stub" :value="value" @input="$emit(\'update:value\', $event.target.value)">',
  },
  'a-input-number': {
    props: ['value'],
    emits: ['update:value'],
    template:
      '<input class="input-number-stub" :value="value" @input="$emit(\'update:value\', $event.target.value === \'\' ? undefined : Number($event.target.value))">',
  },
  'a-tabs': {
    template: '<div class="tabs-stub"><slot /></div>',
  },
  'a-tab-pane': {
    props: ['key', 'tab'],
    template: '<div class="tab-pane-stub" :data-tab="tab"><slot /></div>',
  },
  'a-modal': {
    props: ['visible'],
    emits: ['ok', 'update:visible'],
    template: `
      <div v-if="visible" class="modal-stub">
        <div class="modal-body"><slot /></div>
        <button class="modal-ok" @click="$emit('ok')">ok</button>
      </div>
    `,
  },
  'a-form': { template: '<form><slot /></form>' },
  'a-form-item': { template: '<div><slot /></div>' },
  'a-select': { template: '<div><slot /></div>' },
  'a-select-option': { template: '<div><slot /></div>' },
  'svg-icon': { template: '<i class="svg-icon-stub" />' },
  'down-outlined': { template: '<i />' },
  'check-outlined': { template: '<i />' },
  'delete-outlined': { template: '<i />' },
  'plus-outlined': { template: '<i />' },
}

function mountSelector() {
  return mount(Selector, {
    props: { catalog: 'catalog', db: 'db', table: 'table' },
    global: {
      stubs,
      mocks: { $t: (key: string) => key },
    },
  })
}

function tagNames(wrapper: VueWrapper) {
  return tagPane(wrapper).findAll('.item-label').map(item => item.text())
}

/** Returns the tags tab pane, distinguishing it from the branches/consumers panes. */
function tagPane(wrapper: VueWrapper): DOMWrapper<Element> {
  return wrapper.find('[data-tab="tags"]')
}

async function fillCreateTagForm(wrapper: VueWrapper, tagName: string, snapshotId: string) {
  const inputs = wrapper.findAll('.modal-stub input')
  await inputs[0].setValue(tagName)
  await inputs[1].setValue(snapshotId)
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
    vi.clearAllMocks()
  })

  it('renders the create tag entry in the tags tab', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    const createButton = wrapper.find('.create-tag-btn-wrapper button')
    expect(createButton.exists()).toBe(true)
    expect(createButton.text()).toContain('createTag')
  })

  it('renders existing tags from the API', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    expect(serviceMock.getTags).toHaveBeenCalledWith(
      expect.objectContaining({ catalog: 'catalog', db: 'db', table: 'table' }),
    )
    expect(tagNames(wrapper)).toContain('existing_tag')
  })

  it('opens the create tag modal when the entry is clicked', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    expect(wrapper.find('.modal-stub').exists()).toBe(false)

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')

    expect(wrapper.find('.modal-stub').exists()).toBe(true)
  })

  it('creates a tag with the trimmed snapshot id and refreshes the list', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')
    await fillCreateTagForm(wrapper, 'new_tag', '  12345  ')
    await wrapper.find('.modal-ok').trigger('click')
    await flushPromises()

    expect(serviceMock.createTag).toHaveBeenCalledWith({
      catalog: 'catalog',
      db: 'db',
      table: 'table',
      tagName: 'new_tag',
      snapshotId: '12345',
      maxRefAgeMs: undefined,
    })
    expect(messageMock.success).toHaveBeenCalledWith('createTagSuccess')
    // modal closes and the tag list is reloaded
    expect(wrapper.find('.modal-stub').exists()).toBe(false)
    expect(serviceMock.getTags).toHaveBeenCalledTimes(2)
  })

  it('forwards maxRefAgeMs when provided', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')
    await fillCreateTagForm(wrapper, 'new_tag', '12345')
    await wrapper.find('.input-number-stub').setValue('60000')
    await wrapper.find('.modal-ok').trigger('click')
    await flushPromises()

    expect(serviceMock.createTag).toHaveBeenCalledWith(
      expect.objectContaining({ tagName: 'new_tag', snapshotId: '12345', maxRefAgeMs: 60000 }),
    )
  })

  it('warns and skips the request when the tag name is empty', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')
    await fillCreateTagForm(wrapper, '', '12345')
    await wrapper.find('.modal-ok').trigger('click')
    await flushPromises()

    expect(messageMock.warning).toHaveBeenCalledWith('tagNameRequired')
    expect(serviceMock.createTag).not.toHaveBeenCalled()
  })

  it('warns and skips the request when the snapshot id is blank', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')
    await fillCreateTagForm(wrapper, 'new_tag', '   ')
    await wrapper.find('.modal-ok').trigger('click')
    await flushPromises()

    expect(messageMock.warning).toHaveBeenCalledWith('snapshotIdRequired')
    expect(serviceMock.createTag).not.toHaveBeenCalled()
  })

  it('keeps the modal open and shows no success message when creation fails', async () => {
    serviceMock.createTag.mockRejectedValue(new Error('Ref new_tag already exists in table'))
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.create-tag-btn-wrapper button').trigger('click')
    await fillCreateTagForm(wrapper, 'new_tag', '12345')
    await wrapper.find('.modal-ok').trigger('click')
    await flushPromises()

    expect(messageMock.success).not.toHaveBeenCalled()
    expect(wrapper.find('.modal-stub').exists()).toBe(true)
    // the list is not reloaded on failure
    expect(serviceMock.getTags).toHaveBeenCalledTimes(1)
  })

  it('asks for confirmation before deleting a tag', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.tag-delete-btn').trigger('click')

    expect(modalMock.confirm).toHaveBeenCalledTimes(1)
    const options = modalMock.confirm.mock.calls[0][0]
    expect(options.title).toBe('deleteTag')
    expect(options.content).toBe('deleteTagConfirm')
    // deletion only happens after the user confirms
    expect(serviceMock.deleteTag).not.toHaveBeenCalled()
  })

  it('deletes the tag after confirmation and refreshes the list', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.tag-delete-btn').trigger('click')
    const options = modalMock.confirm.mock.calls[0][0]
    await options.onOk()
    await flushPromises()

    expect(serviceMock.deleteTag).toHaveBeenCalledWith({
      catalog: 'catalog',
      db: 'db',
      table: 'table',
      tagName: 'existing_tag',
    })
    expect(messageMock.success).toHaveBeenCalledWith('deleteTagSuccess')
    expect(serviceMock.getTags).toHaveBeenCalledTimes(2)
  })

  it('shows no success message when deletion fails', async () => {
    serviceMock.deleteTag.mockRejectedValue(new Error('Tag existing_tag not found in table'))
    const wrapper = mountSelector()
    await flushPromises()

    await wrapper.find('.tag-delete-btn').trigger('click')
    const options = modalMock.confirm.mock.calls[0][0]
    await options.onOk()
    await flushPromises()

    expect(messageMock.success).not.toHaveBeenCalled()
    expect(serviceMock.getTags).toHaveBeenCalledTimes(1)
  })

  it('falls back to the main branch when the selected tag is deleted', async () => {
    const wrapper = mountSelector()
    await flushPromises()

    // Select the tag itself so the fallback branch is actually reachable.
    await tagPane(wrapper).findAll('.branch-selector-item')[0].trigger('click')
    await flushPromises()
    expect(wrapper.find('.branch-btn-label').text()).toBe('existing_tag')

    await tagPane(wrapper).find('.tag-delete-btn').trigger('click')
    const options = modalMock.confirm.mock.calls[0][0]
    await options.onOk()
    await flushPromises()

    // The deleted tag must no longer be the active ref.
    expect(wrapper.find('.branch-btn-label').text()).toBe('main')
    const refChanges = wrapper.emitted('refChange') as unknown[][]
    const lastRef = (refChanges[refChanges.length - 1][0] as { ref: string }).ref
    expect(lastRef).toBe('main')
  })

  it('keeps the current selection when a different tag is deleted', async () => {
    serviceMock.getTags.mockResolvedValue({
      list: [{ name: 'selected_tag' }, { name: 'other_tag' }],
    })
    const wrapper = mountSelector()
    await flushPromises()

    // Select the first tag, then delete the second one.
    await tagPane(wrapper).findAll('.branch-selector-item')[0].trigger('click')
    await flushPromises()
    expect(wrapper.find('.branch-btn-label').text()).toBe('selected_tag')

    await tagPane(wrapper).findAll('.tag-delete-btn')[1].trigger('click')
    const options = modalMock.confirm.mock.calls[0][0]
    await options.onOk()
    await flushPromises()

    expect(wrapper.find('.branch-btn-label').text()).toBe('selected_tag')
  })
})
