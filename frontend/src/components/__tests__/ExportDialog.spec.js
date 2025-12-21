import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElDialog, ElButton, ElRadioGroup, ElMessage } from 'element-plus'
import ExportDialog from '../ExportDialog.vue'
import * as contentApi from '@/api/content'

// Mock the content API
vi.mock('@/api/content', () => ({
  exportAsJsonLd: vi.fn(),
  exportAsHtml: vi.fn(),
  exportAsMarkdown: vi.fn(),
  exportAsCsv: vi.fn()
}))

// Mock ElMessage
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

describe('ExportDialog', () => {
  let wrapper

  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mount(ExportDialog, {
      props: {
        modelValue: true,
        contentId: 1,
        ...props
      },
      global: {
        components: {
          ElDialog,
          ElButton,
          ElRadioGroup
        },
        stubs: {
          ElDialog: {
            template: '<div><slot /></div>'
          },
          ElButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          ElRadio: {
            template: '<div><slot /></div>'
          },
          ElRadioGroup: {
            template: '<div><slot /></div>',
            props: ['modelValue']
          }
        }
      }
    })
  }

  it('renders export dialog with format options', () => {
    wrapper = createWrapper()
    
    expect(wrapper.find('.export-options').exists()).toBe(true)
    expect(wrapper.text()).toContain('JSON-LD')
    expect(wrapper.text()).toContain('HTML')
    expect(wrapper.text()).toContain('Markdown')
    expect(wrapper.text()).toContain('CSV')
  })

  it('exports content as JSON-LD when selected', async () => {
    const mockBlob = new Blob(['test'], { type: 'application/json' })
    contentApi.exportAsJsonLd.mockResolvedValue({ data: mockBlob })
    
    // Mock URL.createObjectURL and related functions
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()
    
    // Mock document.createElement and appendChild
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn()
    }
    const originalCreateElement = document.createElement
    document.createElement = vi.fn((tag) => {
      if (tag === 'a') return mockLink
      return originalCreateElement.call(document, tag)
    })
    document.body.appendChild = vi.fn()
    document.body.removeChild = vi.fn()

    wrapper = createWrapper()
    
    // Set format to jsonld
    await wrapper.vm.$nextTick()
    wrapper.vm.selectedFormat = 'jsonld'
    await wrapper.vm.$nextTick()
    
    // Trigger export
    await wrapper.vm.handleExport()
    
    expect(contentApi.exportAsJsonLd).toHaveBeenCalledWith(1)
    expect(mockLink.download).toBe('content-1.jsonld')
    expect(mockLink.click).toHaveBeenCalled()
    
    // Cleanup
    document.createElement = originalCreateElement
  })

  it('exports content as HTML when selected', async () => {
    const mockBlob = new Blob(['<html></html>'], { type: 'text/html' })
    contentApi.exportAsHtml.mockResolvedValue({ data: mockBlob })
    
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()
    
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn()
    }
    const originalCreateElement = document.createElement
    document.createElement = vi.fn((tag) => {
      if (tag === 'a') return mockLink
      return originalCreateElement.call(document, tag)
    })
    document.body.appendChild = vi.fn()
    document.body.removeChild = vi.fn()

    wrapper = createWrapper()
    
    wrapper.vm.selectedFormat = 'html'
    await wrapper.vm.$nextTick()
    
    await wrapper.vm.handleExport()
    
    expect(contentApi.exportAsHtml).toHaveBeenCalledWith(1)
    expect(mockLink.download).toBe('content-1.html')
    
    document.createElement = originalCreateElement
  })

  it('exports content as Markdown when selected', async () => {
    const mockBlob = new Blob(['# Test'], { type: 'text/plain' })
    contentApi.exportAsMarkdown.mockResolvedValue({ data: mockBlob })
    
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()
    
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn()
    }
    const originalCreateElement = document.createElement
    document.createElement = vi.fn((tag) => {
      if (tag === 'a') return mockLink
      return originalCreateElement.call(document, tag)
    })
    document.body.appendChild = vi.fn()
    document.body.removeChild = vi.fn()

    wrapper = createWrapper()
    
    wrapper.vm.selectedFormat = 'markdown'
    await wrapper.vm.$nextTick()
    
    await wrapper.vm.handleExport()
    
    expect(contentApi.exportAsMarkdown).toHaveBeenCalledWith(1)
    expect(mockLink.download).toBe('content-1.md')
    
    document.createElement = originalCreateElement
  })

  it('exports content as CSV when selected', async () => {
    const mockBlob = new Blob(['col1,col2'], { type: 'text/csv' })
    contentApi.exportAsCsv.mockResolvedValue({ data: mockBlob })
    
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()
    
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn()
    }
    const originalCreateElement = document.createElement
    document.createElement = vi.fn((tag) => {
      if (tag === 'a') return mockLink
      return originalCreateElement.call(document, tag)
    })
    document.body.appendChild = vi.fn()
    document.body.removeChild = vi.fn()

    wrapper = createWrapper()
    
    wrapper.vm.selectedFormat = 'csv'
    await wrapper.vm.$nextTick()
    
    await wrapper.vm.handleExport()
    
    expect(contentApi.exportAsCsv).toHaveBeenCalledWith(1)
    expect(mockLink.download).toBe('content-1.csv')
    
    document.createElement = originalCreateElement
  })

  it('shows error message when export fails', async () => {
    contentApi.exportAsJsonLd.mockRejectedValue(new Error('Export failed'))
    
    wrapper = createWrapper()
    
    wrapper.vm.selectedFormat = 'jsonld'
    await wrapper.vm.$nextTick()
    
    await wrapper.vm.handleExport()
    
    expect(ElMessage.error).toHaveBeenCalled()
  })

  it('emits exported event after successful export', async () => {
    const mockBlob = new Blob(['test'], { type: 'application/json' })
    contentApi.exportAsJsonLd.mockResolvedValue({ data: mockBlob })
    
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()
    
    const mockLink = {
      href: '',
      download: '',
      click: vi.fn()
    }
    const originalCreateElement = document.createElement
    document.createElement = vi.fn((tag) => {
      if (tag === 'a') return mockLink
      return originalCreateElement.call(document, tag)
    })
    document.body.appendChild = vi.fn()
    document.body.removeChild = vi.fn()

    wrapper = createWrapper()
    
    wrapper.vm.selectedFormat = 'jsonld'
    await wrapper.vm.$nextTick()
    
    await wrapper.vm.handleExport()
    
    expect(wrapper.emitted('exported')).toBeTruthy()
    expect(wrapper.emitted('exported')[0]).toEqual(['jsonld'])
    
    document.createElement = originalCreateElement
  })
})
