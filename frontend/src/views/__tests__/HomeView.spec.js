import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import HomeView from '../HomeView.vue'
import ContentCard from '@/components/ContentCard.vue'
import CategoryNav from '@/components/CategoryNav.vue'

// Mock API calls
vi.mock('@/api/content', () => ({
  getPublishedContents: vi.fn(() => Promise.resolve({
    data: {
      success: true,
      data: {
        content: [],
        number: 0,
        totalPages: 0,
        totalElements: 0,
        size: 12
      }
    }
  })),
  getContentsByCategory: vi.fn(() => Promise.resolve({
    data: {
      success: true,
      data: {
        content: [],
        number: 0,
        totalPages: 0,
        totalElements: 0,
        size: 12
      }
    }
  }))
}))

vi.mock('@/api/category', () => ({
  getAllCategories: vi.fn(() => Promise.resolve({
    data: {
      success: true,
      data: []
    }
  })),
  getCategory: vi.fn(() => Promise.resolve({
    data: {
      success: true,
      data: { id: 1, name: 'Test Category' }
    }
  }))
}))

describe('HomeView', () => {
  let router
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/register', component: { template: '<div>Register</div>' } }
      ]
    })
  })

  it('renders the homepage', async () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    expect(wrapper.find('.home').exists()).toBe(true)
    expect(wrapper.find('.hero-title').text()).toBe('去伪存真、建立AI秩序')
    expect(wrapper.find('.hero-subtitle').text()).toBe('专为AI优化的结构化数据平台')
  })

  it('displays logo text when no logo URL is provided', () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    expect(wrapper.find('.logo-text').exists()).toBe(true)
    expect(wrapper.find('.logo-text').text()).toBe('数流精灵')
  })

  it('shows login and register links when not authenticated', () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    const links = wrapper.findAll('.nav-link')
    expect(links.length).toBeGreaterThan(0)
  })

  it('displays empty state when no contents are available', async () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    // Wait for component to mount and load data
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('includes CategoryNav component', () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    expect(wrapper.findComponent({ name: 'CategoryNav' }).exists()).toBe(true)
  })

  it('has responsive container', () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          ContentCard: true,
          CategoryNav: true
        }
      }
    })

    expect(wrapper.find('.container').exists()).toBe(true)
  })
})
