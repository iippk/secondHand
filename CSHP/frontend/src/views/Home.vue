<template>
  <div class="home-container">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="电子产品" name="电子产品"></el-tab-pane>
      <el-tab-pane label="图书教材" name="图书教材"></el-tab-pane>
      <el-tab-pane label="生活用品" name="生活用品"></el-tab-pane>
      <el-tab-pane label="服装配饰" name="服装配饰"></el-tab-pane>
      <el-tab-pane label="其他" name="其他"></el-tab-pane>
    </el-tabs>

    <div v-loading="loading" class="product-list">
      <el-row :gutter="20">
        <el-col
          v-for="product in productList"
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
                v-if="product.images && product.images.trim()"
                :src="getImageUrl(product.images.split(',')[0])"
                :fallback="'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'"
                fit="cover"
                :alt="product.title"
              />
              <el-image
                v-else
                :src="'https://via.placeholder.com/200x200?text=No+Image'"
                fit="cover"
              />
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <p class="product-seller">卖家：{{ product.sellerName }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProductList, searchProducts, getProductsByCategory } from '@/api/product'
import { ElMessage } from 'element-plus'
import { getImageFullUrl } from '@/api/upload'

const router = useRouter()
const productList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')

const loadProducts = async () => {
  loading.value = true
  try {
    if (activeCategory.value === 'all') {
      productList.value = await getProductList()
    } else {
      productList.value = await getProductsByCategory(activeCategory.value)
    }
  } catch (error) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadProducts()
    return
  }
  loading.value = true
  try {
    productList.value = await searchProducts(searchKeyword.value)
    if (productList.value.length === 0) {
      ElMessage.info('未找到相关商品')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  loadProducts()
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

// 处理图片URL
const getImageUrl = (url) => {
  return getImageFullUrl(url)
}

onMounted(() => {
  loadProducts()
  // 自动刷新
  setInterval(() => {
    loadProducts()
  }, 30000) // 30秒刷新一次
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.product-list {
  margin-top: 20px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 0 4px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 4px;
}

.product-seller {
  font-size: 12px;
  color: #909399;
}
</style>

