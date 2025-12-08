<template>
  <el-container>
    <el-header class="header">
      <div class="header-content">
        <!-- Logo区域 -->
        <div class="logo" @click="$router.push('/home')">
          <div class="logo-icon">
            <el-icon><ShoppingBag /></el-icon>
          </div>
          <span class="logo-text">校园二手平台</span>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
          :ellipsis="false"
        >
          <el-menu-item index="/home">
            <template #title>
              <div class="menu-item-content">
                <el-icon><House /></el-icon>
                <span>首页</span>
              </div>
            </template>
          </el-menu-item>
          <el-menu-item index="/publish">
            <template #title>
              <div class="menu-item-content">
                <el-icon><Plus /></el-icon>
                <span>发布商品</span>
              </div>
            </template>
          </el-menu-item>
          <el-menu-item index="/my-sell">
            <template #title>
              <div class="menu-item-content">
                <el-icon><Sold /></el-icon>
                <span>我的卖出</span>
              </div>
            </template>
          </el-menu-item>
          <el-menu-item index="/my-buy">
            <template #title>
              <div class="menu-item-content">
                <el-icon><ShoppingCart /></el-icon>
                <span>我的买入</span>
              </div>
            </template>
          </el-menu-item>
          <el-menu-item index="/cart">
            <template #title>
              <div class="menu-item-content">
                <!-- 修改这里：移除 el-badge 组件 -->
                <div class="cart-wrapper">
                  <el-icon><ShoppingTrolley /></el-icon>
                  <span>购物车</span>
                </div>
              </div>
            </template>
          </el-menu-item>
        </el-menu>

        <!-- 用户信息区域 -->
        <div class="user-info">
          <!-- 消息通知 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge custom-badge">
            <el-button
              :icon="Bell"
              circle
              @click="showMessageList = true"
              class="message-btn"
            />
          </el-badge>
          
          <el-dropdown @command="handleCommand" class="user-dropdown-wrapper">
            <span class="user-dropdown">
              <!-- 修改头像显示逻辑 -->
              <el-avatar 
                v-if="hasAvatar"
                :src="avatarUrl"
                :size="36"
                class="user-avatar"
                @error="handleAvatarError"
              />
              <el-avatar 
                v-else
                :size="36"
                class="user-avatar text-avatar"
              >
                {{ avatarText }}
              </el-avatar>
              <div class="user-details">
                <span class="username">{{ userStore.userInfo?.name || '用户' }}</span>
                <span class="user-status">在线</span>
              </div>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="profile" class="dropdown-item">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout" class="dropdown-item logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <!-- 消息列表对话框 - 增加宽度 -->
        <el-drawer
          v-model="showMessageList"
          title="消息列表"
          :size="680"
          direction="rtl"
          class="message-drawer"
        >
          <template #header>
            <div class="drawer-header">
              <h3>我的消息</h3>
              <el-tag type="warning" v-if="unreadCount > 0">
                {{ unreadCount }}条未读
              </el-tag>
            </div>
          </template>
          <MessageList @close="showMessageList = false" />
        </el-drawer>
      </div>
      
      <!-- 装饰性元素 -->
      <div class="header-decoration"></div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    
    <!-- AI助手组件 - 新增 -->
    <AIAssistant v-if="showAIAssistant" />
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCartList } from '@/api/cart'
import { getUnreadCount } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { 
  Bell, 
  ShoppingBag, 
  ArrowDown, 
  House, 
  Plus, 
  ShoppingCart, 
  ShoppingTrolley,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import MessageList from '@/views/MessageList.vue'
import AIAssistant from '@/views/AIAssistant.vue' // 引入AI助手组件

// 自定义图标组件
const Sold = {
  template: `
    <svg viewBox="0 0 1024 1024" width="1em" height="1em">
      <path d="M704 288h128v128H704zM192 288h128v128H192zM320 512h384v128H320z" fill="currentColor"/>
      <path d="M896 192H128a32 32 0 0 0-32 32v576a32 32 0 0 0 32 32h768a32 32 0 0 0 32-32V224a32 32 0 0 0-32-32z m-64 576H192V288h640v480z" fill="currentColor"/>
    </svg>
  `
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const cartCount = ref(0) // 这个变量可以保留，但不再用于显示徽章
const unreadCount = ref(0)
const showMessageList = ref(false)
const avatarLoadError = ref(false) // 头像加载错误状态
let unreadCountTimer = null

// 计算属性：是否显示AI助手（在登录、注册页面不显示）
const showAIAssistant = computed(() => {
  const hiddenRoutes = ['/login', '/register']
  return !hiddenRoutes.includes(route.path) && userStore.token
})

// 计算属性：是否有头像
const hasAvatar = computed(() => {
  const avatar = userStore.userInfo?.avatar
  return avatar && avatar.trim() !== '' && !avatarLoadError.value
})

// 计算属性：头像URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return ''
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http') || avatar.startsWith('data:')) {
    return avatar
  }
  
  // 否则可能需要添加基础路径，根据你的实际情况调整
  return avatar
})

// 计算属性：头像文字（用户名的第一个字符）
const avatarText = computed(() => {
  const name = userStore.userInfo?.name
  if (name && name.length > 0) {
    return name.charAt(0).toUpperCase()
  }
  return '用'
})

// 头像加载错误处理
const handleAvatarError = () => {
  avatarLoadError.value = true
  console.warn('头像加载失败，使用文字头像')
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}

const loadUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const count = await getUnreadCount()
    unreadCount.value = count || 0
  } catch (error) {
    console.error('获取未读消息数失败', error)
  }
}

onMounted(async () => {
  if (userStore.token) {
    await userStore.fetchUserInfo()
    // 重置头像加载状态
    avatarLoadError.value = false
    
    try {
      const cartList = await getCartList()
      cartCount.value = cartList?.length || 0
    } catch (error) {
      console.error('获取购物车失败', error)
    }
    // 加载未读消息数
    await loadUnreadCount()
    // 每30秒刷新一次未读消息数
    unreadCountTimer = setInterval(loadUnreadCount, 30000)
  }
})

onUnmounted(() => {
  if (unreadCountTimer) {
    clearInterval(unreadCountTimer)
  }
})
</script>

<style scoped>
/* 主头部样式 */
.header {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  box-shadow: 0 4px 20px rgba(255, 179, 0, 0.15);
  padding: 0;
  height: 70px;
  position: relative;
  border-bottom: 1px solid #ffe699;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  position: relative;
  z-index: 2;
}

/* Logo样式 */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(255, 193, 7, 0.1);
}

.logo:hover {
  background: rgba(255, 193, 7, 0.2);
  transform: translateY(-1px);
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-radius: 10px;
  color: white;
  font-size: 20px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

/* 导航菜单样式 */
.header-menu {
  flex: 1;
  border-bottom: none;
  margin: 0 50px;
  background: transparent;
  display: flex;
  justify-content: center;
}

:deep(.header-menu .el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 0 8px;
  border-radius: 12px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.header-menu .el-menu-item:hover) {
  background: rgba(255, 193, 7, 0.1);
  color: #e65100;
  transform: translateY(-1px);
}

:deep(.header-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.message-badge {
  margin-right: 8px;
}

.message-btn {
  border: none;
  background: rgba(255, 193, 7, 0.1);
  color: #ff9800;
  width: 44px;
  height: 44px;
  transition: all 0.3s ease;
}

.message-btn:hover {
  background: #ffc107;
  color: white;
  transform: scale(1.05);
}

/* 用户下拉菜单 */
.user-dropdown-wrapper {
  margin-left: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 193, 7, 0.2);
}

.user-dropdown:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.2);
  transform: translateY(-1px);
}

.user-avatar {
  border: 2px solid #ffc107;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  font-weight: bold;
}

/* 文字头像的特殊样式 */
.text-avatar {
  color: white;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.username {
  font-size: 14px;
  font-weight: 700;
  color: #333;
}

.user-status {
  font-size: 12px;
  color: #4caf50;
  font-weight: 500;
}

.dropdown-arrow {
  color: #ff9800;
  transition: transform 0.3s ease;
}

.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 徽章样式 - 现在只用于消息通知 */
.custom-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #ff5722 0%, #e64a19 100%);
  border: 2px solid white;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(229, 74, 25, 0.3);
}

/* 下拉菜单样式 */
.user-dropdown-menu {
  border-radius: 12px;
  border: 1px solid #ffe699;
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.15);
  padding: 8px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background: rgba(255, 193, 7, 0.1);
  color: #e65100;
}

.logout-item {
  color: #ff5722;
}

.logout-item:hover {
  background: rgba(255, 87, 34, 0.1);
}

/* 购物车包装器 */
.cart-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 抽屉样式 - 增加宽度后的样式调整 */
:deep(.message-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 20px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  border-bottom: 1px solid #ffe699;
}

:deep(.message-drawer .el-drawer__body) {
  padding: 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.drawer-header h3 {
  margin: 0;
  color: #e65100;
  font-weight: 700;
}

/* 装饰性元素 */
.header-decoration {
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    #ffc107 20%, 
    #ff9800 50%, 
    #ffc107 80%, 
    transparent 100%);
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 15px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .header-menu {
    margin: 0 20px;
  }
  
  .user-details {
    display: none;
  }
  
  :deep(.header-menu .el-menu-item) {
    margin: 0 4px;
    font-size: 14px;
  }
  
  .menu-item-content {
    gap: 4px;
  }
  
  /* 移动端消息抽屉全屏 */
  :deep(.message-drawer) {
    width: 100% !important;
  }
}
</style>