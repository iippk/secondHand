<template>
  <el-container>
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/home')">
          <el-icon><ShoppingBag /></el-icon>
          <span>校园二手平台</span>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/publish">发布商品</el-menu-item>
          <el-menu-item index="/my-sell">我的卖出</el-menu-item>
          <el-menu-item index="/my-buy">我的买入</el-menu-item>
          <el-menu-item index="/cart">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <span>购物车</span>
            </el-badge>
          </el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                {{ userStore.userInfo?.name?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.name || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCartList } from '@/api/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const cartCount = ref(0)

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}

onMounted(async () => {
  if (userStore.token) {
    await userStore.fetchUserInfo()
    try {
      const cartList = await getCartList()
      cartCount.value = cartList?.length || 0
    } catch (error) {
      console.error('获取购物车失败', error)
    }
  }
})
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.header-menu {
  flex: 1;
  border-bottom: none;
  margin: 0 40px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}
</style>

