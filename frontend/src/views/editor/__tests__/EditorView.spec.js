import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import EditorView from '../EditorView.vue'
import * as templateApi from '@/api/template'
import * as contentApi from '@/api/content'

// Mock the APIs
vi.mock('@/api/template')
vi.mock('@/api/content')

// Mock Element Plus components
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

describe('EditorView', () => {
  let wrapper
  let router

  const mockTemplates = [
    {
      id: 1,
      name: '技术文章',
      type: 'Article',
      schemaOrgType: 'Article',
      schemaDefinition: JSON.stringify({
        fields: [
          { name: 'headline', label: '标题', type: 'text', required: true },
          { name: 'articleBody', label: '正文', type: 'textarea', required: true },
          { name: 'datePublished', label: '发布日期', type: 'date', required: false }
        ]
      })
    }
  ]

  beforeEach(() => {
    // Setup router
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/editor', name: 'editor', component: EditorView }
      ]
    })

    // Mock API responses
    vi.mocked(templateApi.getTemplateList).mockResolvedValue({
      data: { success: true, data: mockTemplates }
    })

    vi.mocked(templateApi.getTemplateDetail).mockResolvedValue({
      data: { success: true, data: mockTemplates[0] }
    })
  })

  it('renders the editor view', async () => {
    wrapper = mount(EditorView, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot name="header"></slot><slot></slot></div>' },
          'el-row': { template: '<div><slot></slot></div>' },
          'el-col': { template: '<div><slot></slot></div>' },
          'el-select': { template: '<select><slot></slot></select>' },
          'el-option': { template: '<option><slot></slot></option>' },
          'el-form': { template: '<form><slot></slot></form>' },
          'el-form-item': { template: '<div><slot></slot></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot></slot></button>' },
          'el-tabs': { template: '<div><slot></slot></div>' },
          'el-tab-pane': { template: '<div><slot></slot></div>' },
          'el-alert': { template: '<div><slot></slot></div>' },
          'el-progress': { template: '<div></div>' },
          'el-tooltip': { template: '<div><slot></slot></div>' },
          'el-icon': { template: '<i><slot></slot></i>' },
          'el-date-picker': { template: '<input type="date" />' },
          'el-input-number': { template: '<input type="number" />' },
          'el-radio-group': { template: '<div><slot></slot></div>' },
          'el-radio': { template: '<input type="radio" />' },
          'draggable': { template: '<div><slot></slot></div>' }
        }
      }
    })

    await wrapper.vm.$nextTick()
    expect(wrapper.find('.editor-view').exists()).toBe(true)
  })

  it('loads templates on mount', async () => {
    wrapper = mount(EditorView, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot></slot></div>' },
          'el-row': { template: '<div><slot></slot></div>' },
          'el-col': { template: '<div><slot></slot></div>' },
          'el-select': { template: '<select><slot></slot></select>' },
          'el-button': { template: '<button><slot></slot></button>' }
        }
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(templateApi.getTemplateList).toHaveBeenCalled()
  })

  it('displays template selector', async () => {
    wrapper = mount(EditorView, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot></slot></div>' },
          'el-row': { template: '<div><slot></slot></div>' },
          'el-col': { template: '<div><slot></slot></div>' },
          'el-select': { template: '<select><slot></slot></select>' },
          'el-option': { template: '<option><slot></slot></option>' }
        }
      }
    })

    await wrapper.vm.$nextTick()
    expect(wrapper.find('.template-selector').exists()).toBe(true)
  })

  it('displays copyright section', async () => {
    wrapper = mount(EditorView, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot></slot></div>' },
          'el-row': { template: '<div><slot></slot></div>' },
          'el-col': { template: '<div><slot></slot></div>' },
          'el-form': { template: '<form><slot></slot></form>' },
          'el-form-item': { template: '<div><slot></slot></div>' }
        }
      }
    })

    await wrapper.vm.$nextTick()
    
    // Set a template to show copyright section
    wrapper.vm.selectedTemplateId = 1
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.copyright-section').exists()).toBe(true)
  })

  it('displays preview panel with tabs', async () => {
    wrapper = mount(EditorView, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot></slot></div>' },
          'el-row': { template: '<div><slot></slot></div>' },
          'el-col': { template: '<div><slot></slot></div>' },
          'el-tabs': { template: '<div><slot></slot></div>' },
          'el-tab-pane': { template: '<div><slot></slot></div>' }
        }
      }
    })

    await wrapper.vm.$nextTick()
    
    // Set a template to show preview
    wrapper.vm.selectedTemplateId = 1
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.preview-panel').exists()).toBe(true)
  })
})
