import { useRouter } from 'vue-router'

/**
 * 导航辅助函数
 */
export function useNavigation() {
  const router = useRouter()

  /**
   * 导航到首页
   */
  function goHome() {
    router.push({ name: 'home' })
  }

  /**
   * 导航到登录页
   */
  function goLogin(redirect) {
    router.push({ 
      name: 'login', 
      query: redirect ? { redirect } : undefined 
    })
  }

  /**
   * 导航到注册页
   */
  function goRegister() {
    router.push({ name: 'register' })
  }

  /**
   * 导航到内容详情页
   */
  function goContentDetail(contentId) {
    router.push({ 
      name: 'content-detail', 
      params: { id: contentId } 
    })
  }

  /**
   * 导航到编辑器
   */
  function goEditor(contentId) {
    if (contentId) {
      router.push({ 
        name: 'editor-edit', 
        params: { id: contentId } 
      })
    } else {
      router.push({ name: 'editor' })
    }
  }

  /**
   * 导航到收藏页
   */
  function goFavorites() {
    router.push({ name: 'favorites' })
  }

  /**
   * 导航到通知页
   */
  function goNotifications() {
    router.push({ name: 'notifications' })
  }

  /**
   * 导航到管理后台
   */
  function goAdmin(section = 'users') {
    router.push({ name: `admin-${section}` })
  }

  /**
   * 返回上一页
   */
  function goBack() {
    router.back()
  }

  return {
    goHome,
    goLogin,
    goRegister,
    goContentDetail,
    goEditor,
    goFavorites,
    goNotifications,
    goAdmin,
    goBack
  }
}
