<template>
  <div class="home-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-header">
          <h1 class="welcome-title">发现校园好物</h1>
          <p class="welcome-subtitle">淘你所爱，享你所想</p>
        </div>
        <div class="search-bar-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的商品..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
            class="custom-search-input"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #append>
              <el-button 
                @click="handleSearch" 
                class="search-btn"
                :icon="Search"
              >
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-section">
      <div class="category-container">
        <el-tabs 
          v-model="activeCategory" 
          @tab-change="handleCategoryChange"
          class="custom-tabs"
        >
          <el-tab-pane name="all">
            <template #label>
              <div class="tab-label">
                <el-icon><Collection /></el-icon>
                <span>全部商品</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="电子产品">
            <template #label>
              <div class="tab-label">
                <el-icon><Monitor /></el-icon>
                <span>电子产品</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="图书教材">
            <template #label>
              <div class="tab-label">
                <el-icon><Reading /></el-icon>
                <span>图书教材</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="生活用品">
            <template #label>
              <div class="tab-label">
                <el-icon><House /></el-icon>
                <span>生活用品</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="服装配饰">
            <template #label>
              <div class="tab-label">
                <el-icon><Present /></el-icon>
                <span>服装配饰</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="其他">
            <template #label>
              <div class="tab-label">
                <el-icon><More /></el-icon>
                <span>其他</span>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-container">
        <div class="filter-content">
          <!-- 价格筛选 -->
          <div class="filter-group">
            <span class="filter-label">价格范围</span>
            <div class="price-inputs">
              <el-input
                v-model="priceRange.min"
                placeholder="最低价"
                type="number"
                min="0"
                @change="handleFilterChange"
                class="price-input"
              >
                <template #prepend>¥</template>
              </el-input>
              <span class="price-separator">-</span>
              <el-input
                v-model="priceRange.max"
                placeholder="最高价"
                type="number"
                min="0"
                @change="handleFilterChange"
                class="price-input"
              >
                <template #prepend>¥</template>
              </el-input>
            </div>
          </div>

          <!-- 商品状态筛选 -->
          <div class="filter-group">
            <span class="filter-label">商品状态</span>
            <el-select
              v-model="filterCondition"
              @change="handleFilterChange"
              placeholder="选择商品状态"
              class="condition-select"
            >
              <el-option label="全部状态" value="all" />
              <el-option label="全新" value="全新" />
              <el-option label="几乎全新" value="几乎全新" />
              <el-option label="良好" value="良好" />
              <el-option label="一般" value="一般" />
              <el-option label="较差" value="较差" />
            </el-select>
          </div>

          <!-- 排序方式 -->
          <div class="filter-group">
            <span class="filter-label">排序方式</span>
            <el-select
              v-model="sortBy"
              @change="handleSortChange"
              placeholder="选择排序方式"
              class="sort-select"
            >
              <el-option label="默认排序" value="default" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
              <el-option label="最新发布" value="latest" />
            </el-select>
          </div>

          <!-- 重置按钮 -->
          <div class="filter-group">
            <el-button 
              @click="resetFilters" 
              class="reset-btn"
              :disabled="!hasActiveFilters"
            >
              重置筛选
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section">
      <div class="product-container">
        <div v-loading="loading" class="product-list">
          <!-- 商品统计 -->
          <div class="product-stats">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-tag type="warning" class="stats-tag">
                共找到 {{ filteredProducts.length }} 件商品
              </el-tag>
              <el-button 
                size="small" 
                @click="forceRefresh" 
                :loading="loading"
                type="primary"
                plain
              >
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
            <!-- 当前筛选条件显示 -->
            <div class="active-filters" v-if="hasActiveFilters">
              <el-tag
                v-if="priceRange.min || priceRange.max"
                closable
                @close="clearPriceFilter"
                class="filter-tag"
              >
                价格: {{ priceRange.min || '0' }} - {{ priceRange.max || '不限' }}
              </el-tag>
              <el-tag
                v-if="filterCondition !== 'all'"
                closable
                @close="clearConditionFilter"
                class="filter-tag"
              >
                状态: {{ filterCondition }}
              </el-tag>
              <el-tag
                v-if="sortBy !== 'default'"
                closable
                @close="clearSort"
                class="filter-tag"
              >
                排序: {{ getSortLabel(sortBy) }}
              </el-tag>
            </div>
          </div>
          
          <el-row :gutter="24">
            <el-col
              v-for="product in filteredProducts"
              :key="product.id"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="6"
            >
              <el-card
                class="product-card"
                shadow="hover"
                @click="goToDetail(product.id)"
              >
               
                <div class="product-image">
                  <el-image
                    :src="getImageUrl(product.images)"
                    :alt="product.title"
                    fit="cover"
                    class="product-img"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>图片加载失败</span>
                      </div>
                    </template>
                  </el-image>
                  <div class="image-overlay">
                    <el-button 
                      type="primary" 
                      size="small" 
                      class="quick-view-btn"
                      @click.stop="goToDetail(product.id)"
                    >
                      查看详情
                    </el-button>
                  </div>
                </div>
                <div class="product-info">
                  <h3 class="product-title">{{ product.title }}</h3>
                  <p class="product-description">{{ product.description || '暂无描述' }}</p>
                  <div class="product-meta">
                    <div class="price-section">
                      <span class="product-price">¥{{ product.price }}</span>
                      <span class="original-price" v-if="product.originalPrice">
                        ¥{{ product.originalPrice }}
                      </span>
                    </div>
                    <div class="seller-info">
                      <el-avatar 
                        :src="product.sellerAvatar" 
                        :size="24"
                        class="seller-avatar"
                      >
                        {{ product.sellerName?.charAt(0) }}
                      </el-avatar>
                      <span class="seller-name">{{ product.sellerName }}</span>
                    </div>
                  </div>
                  <div class="product-tags">
                    <el-tag 
                      v-if="product.category" 
                      size="small" 
                      class="category-tag"
                    >
                      {{ product.category }}
                    </el-tag>
                    <el-tag 
                      v-if="product.condition" 
                      size="small" 
                      :type="getConditionType(product.condition)"
                    >
                      {{ product.condition }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 空状态 -->
          <el-empty 
            v-if="!loading && filteredProducts.length === 0" 
            :description="emptyDescription"
            class="custom-empty"
          >
            <template #image>
              <div class="empty-image">
                <el-icon><Box /></el-icon>
              </div>
            </template>
            <el-button type="primary" @click="resetFilters">
              重置筛选条件
            </el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="footer-decoration">
      <div class="decoration-wave"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProductList, searchProducts, getProductsByCategory } from '@/api/product'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Collection, 
  Monitor, 
  Reading, 
  House, 
  Present, 
  More,
  Picture,
  Box,
  Refresh
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const productList = ref([])
const filteredProducts = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')

// 筛选相关状态
const priceRange = ref({
  min: '',
  max: ''
})
const filterCondition = ref('all')
const sortBy = ref('default')

// 计算是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return priceRange.value.min || priceRange.value.max || 
         filterCondition.value !== 'all' || 
         sortBy.value !== 'default'
})

// 计算空状态描述
const emptyDescription = computed(() => {
  if (searchKeyword.value) {
    return `没有找到"${searchKeyword.value}"相关的商品`
  }
  if (hasActiveFilters.value) {
    return '没有找到符合筛选条件的商品'
  }
  if (activeCategory.value !== 'all') {
    return `当前分类暂无商品`
  }
  return '暂无商品，快去发布第一个商品吧！'
})

// 检查用户是否已登录
const isUserLoggedIn = computed(() => {
  return !!userStore.token
})

// 图片处理函数
const getImageUrl = (images) => {
  if (!images || !images.trim()) {
    return 'https://via.placeholder.com/300x300?text=No+Image'
  }
  
  const trimmedPath = images.trim()
  const firstImage = trimmedPath.split(',')[0].trim()
  
  // 检查是否是完整URL
  if (firstImage.startsWith('http://') || firstImage.startsWith('https://')) {
    return firstImage
  }
  
  // 检查是否已经是正确的路径格式
  if (firstImage.startsWith('uploads/') || firstImage.startsWith('/uploads/')) {
    return firstImage.startsWith('/') ? firstImage : `/${firstImage}`
  }
  
  // 处理日期目录结构的路径，如 "2025/11/28/filename.jpg"
  // 直接添加 uploads/ 前缀
  return `/uploads/${firstImage}`
}

// 获取商品状态标签类型
const getConditionType = (condition) => {
  const typeMap = {
    '全新': 'success',
    '几乎全新': 'info',
    '良好': 'primary',
    '一般': 'warning',
    '较差': 'danger'
  }
  return typeMap[condition] || 'info'
}

// 获取排序方式标签
const getSortLabel = (value) => {
  const options = {
    'default': '默认排序',
    'price_asc': '价格从低到高',
    'price_desc': '价格从高到低',
    'latest': '最新发布'
  }
  return options[value] || '默认排序'
}

// 强制刷新商品数据
const forceRefresh = async () => {
  console.log('强制刷新商品数据...')
  await loadProducts(true)
}

// 应用筛选和排序
const applyFilters = () => {
  let filtered = [...productList.value]

  // 价格筛选
  if (priceRange.value.min) {
    filtered = filtered.filter(product => product.price >= parseFloat(priceRange.value.min))
  }
  if (priceRange.value.max) {
    filtered = filtered.filter(product => product.price <= parseFloat(priceRange.value.max))
  }

  // 商品状态筛选
  if (filterCondition.value !== 'all') {
    filtered = filtered.filter(product => product.condition === filterCondition.value)
  }

  // 排序
  switch (sortBy.value) {
    case 'price_asc':
      filtered.sort((a, b) => a.price - b.price)
      break
    case 'price_desc':
      filtered.sort((a, b) => b.price - a.price)
      break
    case 'latest':
      // 假设有 createTime 字段，按时间倒序
      filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      break
    default:
      // 默认排序保持原样
      break
  }

  filteredProducts.value = filtered
}

// 处理筛选条件变化
const handleFilterChange = () => {
  applyFilters()
}

// 处理排序变化
const handleSortChange = () => {
  applyFilters()
}

// 清除价格筛选
const clearPriceFilter = () => {
  priceRange.value = { min: '', max: '' }
  applyFilters()
}

// 清除状态筛选
const clearConditionFilter = () => {
  filterCondition.value = 'all'
  applyFilters()
}

// 清除排序
const clearSort = () => {
  sortBy.value = 'default'
  applyFilters()
}

// 重置所有筛选
const resetFilters = () => {
  priceRange.value = { min: '', max: '' }
  filterCondition.value = 'all'
  sortBy.value = 'default'
  applyFilters()
}

const loadProducts = async (forceRefresh = false) => {
  // 检查登录状态，未登录不加载
  if (!isUserLoggedIn.value) {
    console.log('用户未登录，跳过商品加载')
    productList.value = []
    filteredProducts.value = []
    return
  }
  
  loading.value = true
  try {
    let products = []
    if (activeCategory.value === 'all') {
      products = await getProductList()
    } else {
      products = await getProductsByCategory(activeCategory.value)
    }
    
    productList.value = products.map(product => ({
      ...product,
      isNew: Math.random() > 0.7,
      sellerAvatar: product.sellerAvatar || '',
      condition: product.condition || '良好',
      description: product.description || '这个商品很超值，快来购买吧！',
      createTime: product.createTime || new Date().toISOString()
    }))
    
    // 加载完成后应用筛选
    applyFilters()
    
    // if (forceRefresh) {
    //   ElMessage.success('商品数据已更新')
    // }
  } catch (error) {
    // 如果是未授权错误，不显示错误消息（用户可能已退出登录）
    if (error.response?.status === 401) {
      console.log('用户未授权，清除商品列表')
      productList.value = []
      filteredProducts.value = []
    } else {
      ElMessage.error('加载商品失败')
      console.error('加载商品失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    // 搜索关键词为空时，重新加载当前分类的商品
    loadProducts(true)
    return
  }
  
  // 搜索时检查登录状态
  if (!isUserLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  try {
    const products = await searchProducts(searchKeyword.value)
    productList.value = products
    // 搜索后应用筛选 - 保持当前的筛选条件
    applyFilters()
    if (products.length === 0) {
      ElMessage.info(`没有找到"${searchKeyword.value}"相关的商品`)
    } else {
      ElMessage.success(`找到 ${filteredProducts.value.length} 个相关商品`)
    }
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      productList.value = []
      filteredProducts.value = []
    } else {
      ElMessage.error('搜索失败')
      console.error('搜索失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  // 切换分类时检查登录状态
  if (!isUserLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  // 切换分类时重新加载商品
  loadProducts(true)
}

const goToDetail = (id) => {
  // 查看详情时检查登录状态
  if (!isUserLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  router.push(`/product/${id}`)
}

// 页面激活时刷新数据
onActivated(() => {
  console.log('首页激活，刷新商品数据...')
  // 每次进入页面都强制刷新
  loadProducts(true)
})

onMounted(() => {
  // 只有登录状态下才加载商品
  if (isUserLoggedIn.value) {
    loadProducts(true)
  }
})

// 监听登录状态变化
watch(() => userStore.token, (newToken, oldToken) => {
  if (newToken) {
    // 用户登录，加载商品
    loadProducts(true)
  } else {
    // 用户退出登录，清空商品列表
    productList.value = []
    filteredProducts.value = []
  }
})

// 监听路由变化，当从商品详情页返回时刷新数据
watch(() => route.name, (newRouteName, oldRouteName) => {
  if (newRouteName === 'Home' && oldRouteName === 'ProductDetail') {
    // 从商品详情页返回，刷新数据
    setTimeout(() => {
      loadProducts(true)
    }, 100)
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #fffaf0 0%, #fff9e6 50%, #fff3cc 100%);
}

.search-section {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  padding: 80px 0 60px;
  position: relative;
  overflow: hidden;
}

/* 保留橙色背景条纹 */
.search-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
  position: relative;
  z-index: 1;
}

.search-header {
  text-align: center;
  margin-bottom: 50px;
}

.welcome-title {
  font-size: 3rem;
  font-weight: 800;
  color: white;
  margin-bottom: 16px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  position: relative;
}

.welcome-title::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 2px;
}

.welcome-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.95);
  margin: 24px 0 0 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.search-bar-wrapper {
  max-width: 700px;
  margin: 0 auto;
  position: relative;
}

/* 搜索输入框整体样式优化 */
:deep(.custom-search-input) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 
    0 10px 30px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(255, 152, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

:deep(.custom-search-input:hover) {
  transform: translateY(-2px);
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.2),
    0 6px 15px rgba(255, 152, 0, 0.4);
}

:deep(.custom-search-input .el-input-group__prepend) {
  background: white;
  border: none;
  padding: 0;
  border-radius: 16px 0 0 16px;
}

:deep(.custom-search-input .el-input__wrapper) {
  background: white;
  border: none;
  border-radius: 0;
  box-shadow: none;
  padding: 18px 20px;
  height: 64px;
  font-size: 16px;
  transition: all 0.3s ease;
}

:deep(.custom-search-input .el-input__wrapper:hover) {
  background: #fafafa;
}

:deep(.custom-search-input .el-input__wrapper.is-focus) {
  background: white;
  box-shadow: none !important;
}

.search-icon {
  color: #ff9800;
  font-size: 20px;
  margin-right: 8px;
}

/* 搜索按钮样式全面优化 */
:deep(.search-btn) {
  background: linear-gradient(135deg, #e65100 0%, #ff5722 100%);
  border: none;
  color: white;
  border-radius: 0 16px 16px 0;
  padding: 0 32px;
  height: 64px;
  font-weight: 700;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 4px 12px rgba(230, 81, 0, 0.4);
  font-size: 16px;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

:deep(.search-btn::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.3), 
    transparent);
  transition: left 0.6s ease;
}

:deep(.search-btn:hover) {
  transform: translateY(-1px);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 8px 20px rgba(230, 81, 0, 0.5);
  background: linear-gradient(135deg, #d84315 0%, #f4511e 100%);
}

:deep(.search-btn:hover::before) {
  left: 100%;
}

:deep(.search-btn:active) {
  transform: translateY(0);
  box-shadow: 
    inset 0 2px 4px rgba(0, 0, 0, 0.2),
    0 2px 8px rgba(230, 81, 0, 0.4);
}

:deep(.search-btn .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}

/* 搜索区域装饰元素 */
.search-decoration {
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.6) 20%, 
    rgba(255, 255, 255, 0.8) 50%, 
    rgba(255, 255, 255, 0.6) 80%, 
    transparent 100%);
}

/* 以下保持其他样式不变 */
.category-section {
  background: white;
  padding: 20px 0;
  border-bottom: 1px solid #ffe699;
}

.category-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

:deep(.custom-tabs .el-tabs__header) {
  margin-bottom: 0;
}

:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  background-color: #ffe699;
}

:deep(.custom-tabs .el-tabs__item) {
  padding: 0 24px;
  height: 50px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

:deep(.custom-tabs .el-tabs__item:hover) {
  color: #ff9800;
}

:deep(.custom-tabs .el-tabs__item.is-active) {
  color: #e65100;
}

:deep(.custom-tabs .el-tabs__active-bar) {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  height: 3px;
  border-radius: 2px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-section {
  background: white;
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-input {
  flex: 1;
}

.price-separator {
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
}

.condition-select,
.sort-select {
  width: 100%;
}

.reset-btn {
  background: #f5f5f5;
  border: 1px solid #ddd;
  color: #666;
  width: 100%;
}

.reset-btn:hover {
  background: #e0e0e0;
  border-color: #ccc;
  color: #333;
}

.reset-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.product-section {
  padding: 40px 0;
}

.product-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.stats-tag {
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 20px;
  align-self: flex-start;
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid #ffc107;
  color: #e65100;
}

.product-card {
  margin-bottom: 24px;
  cursor: pointer;
  border-radius: 16px;
  border: 1px solid #ffe699;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(255, 152, 0, 0.2);
  border-color: #ffc107;
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.product-img {
  width: 100%;
  height: 100%;
  transition: transform 0.4s ease;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
  gap: 8px;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .image-overlay {
  opacity: 1;
}

.quick-view-btn {
  background: white;
  color: #e65100;
  border: none;
  font-weight: 600;
  padding: 8px 16px;
}

.quick-view-btn:hover {
  background: #ffc107;
  color: white;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
  white-space: nowrap;          /* 不换行 */
  overflow: hidden;             /* 超出部分隐藏 */
  text-overflow: ellipsis; 
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-price {
  font-size: 20px;
  font-weight: 800;
  color: #e65100;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-avatar {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: 2px solid #ffe699;
}

.seller-name {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.product-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.category-tag {
  background: rgba(255, 193, 7, 0.1);
  color: #e65100;
  border: 1px solid #ffc107;
}

.custom-empty {
  padding: 60px 0;
}

.empty-image {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #fff3cc 0%, #ffe699 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.empty-image .el-icon {
  font-size: 48px;
  color: #ff9800;
}

.footer-decoration {
  margin-top: 40px;
}

.decoration-wave {
  height: 4px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    #ffc107 20%, 
    #ff9800 50%, 
    #ffc107 80%, 
    transparent 100%);
  opacity: 0.6;
}

@media (max-width: 768px) {
  .welcome-title {
    font-size: 2.2rem;
  }
  
  .welcome-subtitle {
    font-size: 1.1rem;
  }
  
  .search-section {
    padding: 60px 0 40px;
  }
  
  :deep(.custom-tabs .el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
  }
  
  .product-image {
    height: 160px;
  }
  
  .filter-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .price-inputs {
    flex-direction: column;
    gap: 8px;
  }
  
  .price-separator {
    display: none;
  }
  
  /* 移动端搜索框优化 */
  :deep(.custom-search-input .el-input__wrapper) {
    height: 56px;
    padding: 16px;
  }
  
  :deep(.search-btn) {
    height: 56px;
    padding: 0 20px;
  }
}
</style>