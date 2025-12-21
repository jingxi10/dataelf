import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NotificationBadge from '../NotificationBadge.vue'
import NotificationItem from '../NotificationItem.vue'
import NotificationDropdown from '../NotificationDropdown.vue'
import { useNotificationStore } from '@/stores/notification'
import { ElButton, ElBadge, ElIcon, ElPopover, ElDivider, ElEmpty } from 'element-plus'

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  Bell: { name: 'Bell' },
  CircleFilled: { name: 'CircleFilled' },
  SuccessFilled: { name: 'SuccessFilled' },
  WarningFilled: { name: 'WarningFilled' },
  InfoFilled: { name: 'InfoFilled' },
  Clock: { name: 'Clock' }
}))

// Mock router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('NotificationBadge', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders notification badge', () => {
    const wrapper = mount(NotificationBadge, {
      global: {
        components: {
          ElBadge,
          ElButton,
          ElIcon
        }
      }
    })
    
    expect(wrapper.find('.notification-badge').exists()).toBe(true)
  })

  it('displays unread count', async () => {
    const wrapper = mount(NotificationBadge, {
      global: {
        components: {
          ElBadge,
          ElButton,
          ElIcon
        }
      }
    })
    
    const store = useNotificationStore()
    store.unreadCount = 5
    
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.unreadCount).toBe(5)
  })

  it('emits click event when clicked', async () => {
    const wrapper = mount(NotificationBadge, {
      global: {
        components: {
          ElBadge,
          ElButton,
          ElIcon
        }
      }
    })
    
    await wrapper.find('.el-button').trigger('click')
    
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})

describe('NotificationItem', () => {
  const mockNotification = {
    id: 1,
    type: 'CONTENT_APPROVED',
    title: '内容已批准',
    message: '您的内容已通过审核',
    isRead: false,
    createdAt: new Date().toISOString(),
    relatedContentId: 123
  }

  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders notification item', () => {
    const wrapper = mount(NotificationItem, {
      props: {
        notification: mockNotification
      },
      global: {
        components: {
          ElIcon
        }
      }
    })
    
    expect(wrapper.find('.notification-item').exists()).toBe(true)
    expect(wrapper.text()).toContain('内容已批准')
    expect(wrapper.text()).toContain('您的内容已通过审核')
  })

  it('shows unread indicator for unread notifications', () => {
    const wrapper = mount(NotificationItem, {
      props: {
        notification: mockNotification
      },
      global: {
        components: {
          ElIcon
        }
      }
    })
    
    expect(wrapper.find('.notification-item').classes()).toContain('unread')
  })

  it('does not show unread indicator for read notifications', () => {
    const readNotification = { ...mockNotification, isRead: true }
    const wrapper = mount(NotificationItem, {
      props: {
        notification: readNotification
      },
      global: {
        components: {
          ElIcon
        }
      }
    })
    
    expect(wrapper.find('.notification-item').classes()).not.toContain('unread')
  })

  it('formats time correctly', () => {
    const wrapper = mount(NotificationItem, {
      props: {
        notification: mockNotification
      },
      global: {
        components: {
          ElIcon
        }
      }
    })
    
    const timeText = wrapper.find('.notification-time').text()
    expect(timeText).toBeTruthy()
  })
})

describe('NotificationDropdown', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders notification badge component', () => {
    const wrapper = mount(NotificationDropdown, {
      global: {
        components: {
          ElPopover,
          ElButton,
          ElBadge,
          ElIcon,
          ElDivider,
          ElEmpty,
          NotificationBadge,
          NotificationItem
        },
        stubs: {
          NotificationBadge: true,
          NotificationItem: true
        }
      }
    })
    
    expect(wrapper.findComponent({ name: 'NotificationBadge' }).exists()).toBe(true)
  })

  it('has notification dropdown structure', () => {
    const wrapper = mount(NotificationDropdown, {
      global: {
        components: {
          ElPopover,
          ElButton,
          ElBadge,
          ElIcon,
          ElDivider,
          ElEmpty,
          NotificationBadge,
          NotificationItem
        },
        stubs: {
          NotificationBadge: true,
          NotificationItem: true
        },
        directives: {
          loading: () => {}
        }
      }
    })
    
    // Check that the component has the expected structure
    expect(wrapper.vm).toBeDefined()
    expect(wrapper.vm.notifications).toBeDefined()
  })
})
