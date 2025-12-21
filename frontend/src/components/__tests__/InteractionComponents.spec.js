import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LikeButton from '../LikeButton.vue'
import FavoriteButton from '../FavoriteButton.vue'
import ShareButton from '../ShareButton.vue'
import CommentSection from '../CommentSection.vue'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElButton: { name: 'ElButton', template: '<button><slot /></button>' },
  ElIcon: { name: 'ElIcon', template: '<i><slot /></i>' },
  ElDialog: { name: 'ElDialog', template: '<div><slot /></div>' },
  ElInput: { name: 'ElInput', template: '<input />' },
  ElForm: { name: 'ElForm', template: '<form><slot /></form>' },
  ElFormItem: { name: 'ElFormItem', template: '<div><slot /></div>' },
  ElText: { name: 'ElText', template: '<span><slot /></span>' },
  ElCard: { name: 'ElCard', template: '<div><slot name="header" /><slot /></div>' },
  ElEmpty: { name: 'ElEmpty', template: '<div />' },
  ElAvatar: { name: 'ElAvatar', template: '<div />' },
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  }
}))

// Mock icons
vi.mock('@element-plus/icons-vue', () => ({
  Star: { name: 'Star', template: '<i />' },
  Collection: { name: 'Collection', template: '<i />' },
  Share: { name: 'Share', template: '<i />' }
}))

// Mock API
vi.mock('@/api/interaction', () => ({
  interact: vi.fn(() => Promise.resolve({ success: true })),
  removeInteraction: vi.fn(() => Promise.resolve({ success: true })),
  generateShareLink: vi.fn(() => Promise.resolve({
    data: {
      url: 'http://localhost:8080/content/1',
      htmlLink: '<a href="http://localhost:8080/content/1" rel="nofollow">Test</a>'
    }
  })),
  getComments: vi.fn(() => Promise.resolve({
    data: [],
    pagination: {
      currentPage: 0,
      totalPages: 1,
      totalElements: 0
    }
  })),
  createComment: vi.fn(() => Promise.resolve({
    data: {
      id: 1,
      commentText: 'Test comment',
      userName: 'Test User',
      createdAt: new Date().toISOString()
    }
  }))
}))

describe('Interaction Components', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('LikeButton', () => {
    it('renders with initial state', () => {
      const wrapper = mount(LikeButton, {
        props: {
          contentId: 1,
          initialLiked: false,
          initialCount: 5
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('5')
    })

    it('renders liked state', () => {
      const wrapper = mount(LikeButton, {
        props: {
          contentId: 1,
          initialLiked: true,
          initialCount: 10
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('10')
    })
  })

  describe('FavoriteButton', () => {
    it('renders with initial state', () => {
      const wrapper = mount(FavoriteButton, {
        props: {
          contentId: 1,
          initialFavorited: false,
          initialCount: 3
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('3')
    })

    it('renders favorited state', () => {
      const wrapper = mount(FavoriteButton, {
        props: {
          contentId: 1,
          initialFavorited: true,
          initialCount: 8
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('8')
    })
  })

  describe('ShareButton', () => {
    it('renders share button', () => {
      const wrapper = mount(ShareButton, {
        props: {
          contentId: 1
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('分享')
    })
  })

  describe('CommentSection', () => {
    it('renders comment section', () => {
      const wrapper = mount(CommentSection, {
        props: {
          contentId: 1,
          lazy: false
        }
      })
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('评论')
    })

    it('shows login prompt when not authenticated', () => {
      const wrapper = mount(CommentSection, {
        props: {
          contentId: 1,
          lazy: false
        }
      })
      
      expect(wrapper.text()).toContain('请先登录后发表评论')
    })
  })
})
