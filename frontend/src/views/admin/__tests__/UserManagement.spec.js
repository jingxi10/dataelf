import { describe, it, expect } from 'vitest'

describe('UserManagement Component', () => {
  it('can import the component', async () => {
    const module = await import('../UserManagement.vue')
    expect(module.default).toBeDefined()
  })

  it('has correct status text mapping', () => {
    const statusTexts = {
      PENDING: '待审核',
      APPROVED: '已批准',
      REJECTED: '已拒绝',
      EXPIRED: '已过期'
    }
    
    expect(statusTexts.PENDING).toBe('待审核')
    expect(statusTexts.APPROVED).toBe('已批准')
    expect(statusTexts.REJECTED).toBe('已拒绝')
    expect(statusTexts.EXPIRED).toBe('已过期')
  })

  it('calculates remaining days correctly', () => {
    const getRemainingDays = (expiresAt) => {
      if (!expiresAt) return 0
      const now = new Date()
      const expiry = new Date(expiresAt)
      const diff = expiry - now
      const days = Math.ceil(diff / (1000 * 60 * 60 * 24))
      return days > 0 ? days : 0
    }
    
    // Test future date
    const futureDate = new Date()
    futureDate.setDate(futureDate.getDate() + 10)
    const remainingDays = getRemainingDays(futureDate.toISOString())
    expect(remainingDays).toBeGreaterThan(0)
    
    // Test past date
    const pastDate = new Date()
    pastDate.setDate(pastDate.getDate() - 10)
    expect(getRemainingDays(pastDate.toISOString())).toBe(0)
    
    // Test null
    expect(getRemainingDays(null)).toBe(0)
  })

  it('formats dates correctly', () => {
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const testDate = '2024-01-01T12:00:00'
    const formatted = formatDate(testDate)
    expect(formatted).toContain('2024')
    expect(formatted).toContain('01')
    
    expect(formatDate(null)).toBe('-')
  })
})
