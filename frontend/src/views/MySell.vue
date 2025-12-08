<template>
  <div class="my-sell-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-background">
        <div class="header-content">
          <h1 class="page-title">我的卖出</h1>
          <p class="page-subtitle">管理您发布的商品，追踪销售状态</p>
        </div>
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-card class="content-card">
        <!-- 统计信息区域 -->
        <div class="stats-section">
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-icon sold-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ soldCount }}</div>
                <div class="stat-label">已售出</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon available-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ availableCount }}</div>
                <div class="stat-label">待售中</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon shipped-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ shippedCount }}</div>
                <div class="stat-label">已发货</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选区域 -->
        <div class="filter-section">
          <div class="filter-content">
            <div class="filter-left">
              <el-select 
                v-model="filterProductStatus" 
                placeholder="商品状态" 
                clearable 
                @change="handleFilterChange"
                class="status-filter"
              >
                <el-option label="全部商品" value=""></el-option>
                <el-option label="待售" value="0"></el-option>
                <el-option label="已售出" value="1"></el-option>
                <el-option label="已发货" value="2"></el-option>
                <el-option label="已下架" value="3"></el-option>
              </el-select>
            </div>
            <div class="filter-right">
              <el-button 
                @click="loadProductsAndOrders" 
                :loading="loading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-list" v-loading="loading">
          <div v-if="productList.length === 0 && !loading" class="empty-state">
            <div class="empty-image">
              <el-icon><Box /></el-icon>
            </div>
            <h3>暂无商品</h3>
            <p>您还没有发布任何商品，快去发布您的闲置物品吧！</p>
            <el-button type="primary" @click="goToPublish" class="publish-btn">
              <el-icon><Plus /></el-icon>
              发布商品
            </el-button>
          </div>

          <div v-else class="products-container">
            <div 
              v-for="product in productList" 
              :key="product.id" 
              class="product-card"
              :class="{
                'sold-card': product.orderStatus === 1,
                'shipped-card': product.orderStatus === 2
              }"
            >
              <div class="product-header">
                <div class="product-info">
                  <span class="product-id">商品ID：{{ product.id }}</span>
                  <span class="product-time">{{ formatDate(product.createTime) }}</span>
                </div>
                <div class="status-container">
                  <!-- 商品状态标签 -->
                  <el-tag :type="getProductStatusType(product.productStatus)" class="status-tag">
                    {{ getProductStatusText(product.productStatus) }}
                  </el-tag>
                  
                  <!-- 订单状态标签 -->
                  <el-tag 
                    v-if="product.orderStatus !== null" 
                    :type="getOrderStatusType(product.orderStatus)" 
                    class="order-tag"
                  >
                    {{ getOrderStatusText(product.orderStatus) }}
                  </el-tag>
                </div>
              </div>

              <div class="product-body">
                <div class="product-content">
                  <div class="product-image">
                    <el-image
                      :src="getProductImage(product.images)"
                      fit="cover"
                      class="product-img"
                      @error="handleImageError"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                  </div>
                  <div class="product-details">
                    <h4 class="product-title">{{ product.title }}</h4>
                    <p class="product-desc">{{ product.description || '暂无描述' }}</p>
                    <div class="product-meta">
                      <div class="category-info">
                        <span class="category-label">分类：</span>
                        <span class="category-value">{{ product.category || '未分类' }}</span>
                      </div>
                      <div class="condition-info">
                        <span class="condition-label">成色：</span>
                        <span class="condition-value">{{ product.condition || '良好' }}</span>
                      </div>
                      <div class="price-info">
                        <span class="price-label">价格：</span>
                        <span class="price-amount">¥{{ product.price }}</span>
                      </div>
                    </div>
                    
                    <!-- 浏览量 -->
                    <div class="view-info">
                      <el-icon><View /></el-icon>
                      <span>浏览量：{{ product.viewCount || 0 }}</span>
                    </div>
                    
                    <!-- 最后更新时间 -->
                    <div class="update-time">
                      <el-icon><Clock /></el-icon>
                      <span>最后更新：{{ formatDate(product.updateTime) }}</span>
                    </div>
                    
               
                  </div>
                </div>

                <!-- 操作按钮区域 -->
                <div class="product-actions">
                  <!-- 编辑商品按钮 -->
                  <el-button 
                    type="primary" 
                    link 
                    @click="editProduct(product)"
                    :disabled="product.productStatus !== 0"
                    class="edit-btn"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑商品
                  </el-button>
                  
                  <!-- 按钮组：标记发货和删除按钮在同一行 -->
                  <div class="button-group">
                    <!-- 标记发货按钮 -->
                    <el-button
                      type="primary"
                      @click="markAsShipped(product)"
                      :disabled="!canMarkAsShipped(product)"
                      :loading="shippingOrderId === product.id"
                      class="shipped-btn"
                    >
                      <template v-if="product.orderStatus === 2">
                        <el-icon><Check /></el-icon>
                        已发货
                      </template>
                      <template v-else>
                        <el-icon><Ship /></el-icon>
                        标记发货
                      </template>
                    </el-button>

                    <!-- 删除按钮 -->
                    <el-button
                      @click="deleteProduct(product)"
                      :disabled="product.productStatus !== 0"
                      class="delete-btn"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页区域 -->
        <div class="pagination-section" v-if="productList.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalProducts"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="custom-pagination"
          />
        </div>
      </el-card>
    </div>

    <!-- 编辑商品弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑商品"
      width="600px"
      :close-on-click-modal="false"
      class="edit-dialog"
    >
      <div v-if="currentProduct" class="product-edit">
        <!-- 商品信息 -->
        <div class="edit-product">
          <div class="product-image">
            <el-image
              :src="getProductImage(currentProduct.images)"
              fit="cover"
              class="edit-img"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="product-info">
            <h4 class="product-title">{{ currentProduct.title }}</h4>
            <div class="price-section">
              <span class="price">¥{{ currentProduct.price }}</span>
            </div>
            <div class="status-section">
              <el-tag :type="getProductStatusType(currentProduct.productStatus)" class="status-tag">
                {{ getProductStatusText(currentProduct.productStatus) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 编辑表单 -->
        <div class="edit-form">
          <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editRules"
            label-width="100px"
          >
            <el-form-item label="商品标题" prop="title">
              <el-input
                v-model="editForm.title"
                size="large"
                placeholder="请输入商品标题"
              />
            </el-form-item>

            <el-form-item label="商品描述" prop="description">
              <el-input
                v-model="editForm.description"
                type="textarea"
                :rows="3"
                resize="none"
                placeholder="请输入商品描述"
              />
            </el-form-item>

            <el-form-item label="商品价格" prop="price">
              <el-input-number
                v-model="editForm.price"
                :min="0"
                :precision="2"
                :step="0.1"
                size="large"
                class="price-input"
              />
            </el-form-item>

            <el-form-item label="商品分类" prop="category">
              <el-select
                v-model="editForm.category"
                placeholder="请选择商品分类"
                size="large"
                class="category-select"
              >
                <el-option label="电子产品" value="电子产品" />
                <el-option label="图书教材" value="图书教材" />
                <el-option label="生活用品" value="生活用品" />
                <el-option label="服装配饰" value="服装配饰" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>

            <el-form-item label="商品成色" prop="condition">
              <el-select
                v-model="editForm.condition"
                placeholder="请选择商品成色"
                size="large"
                class="condition-select"
              >
                <el-option label="全新" value="全新" />
                <el-option label="几乎全新" value="几乎全新" />
                <el-option label="良好" value="良好" />
                <el-option label="一般" value="一般" />
                <el-option label="较差" value="较差" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <div class="edit-footer">
          <el-button @click="editDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveProduct" 
            :loading="saving" 
            class="save-btn"
          >
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  getMySellProducts, 
  updateProduct, 
  deleteProduct as deleteProductApi
} from '@/api/product'
import { shipOrder } from '@/api/order'
import { getImageFullUrl } from '@/api/upload'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Picture, 
  Edit, 
  Delete, 
  Check,
  Refresh,
  Box,
  Plus,
  CircleCheck,
  Clock,
  View,
  Tickets,
  Ship
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const productList = ref([])
const filterProductStatus = ref('')
const editDialogVisible = ref(false)
const currentProduct = ref(null)
const editFormRef = ref(null)
const shippingOrderId = ref(null) // 正在发货的商品ID

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalProducts = ref(0)

// 存储所有商品数据（用于前端分页和筛选）
const allProducts = ref([])

// 存储订单数据
const orders = ref([])

const editForm = reactive({
  id: null,
  title: '',
  description: '',
  price: 0,
  category: '',
  condition: '良好'
})

const editRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ]
}

// 商品状态映射
const productStatusMap = {
  0: { text: '待售', type: 'success' },
  1: { text: '已售出', type: 'warning' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已下架', type: 'info' }
}

// 订单状态映射 - 使用user_order表的status
const orderStatusMap = {
  1: { text: '待付款', type: 'warning' },
  2: { text: '已发货', type: 'success' },
  3: { text: '已完成', type: 'info' }
}

// 计算属性 - 修改为基于订单状态的统计
const soldCount = computed(() => {
  // 已售出包括：订单状态为1（待付款）的商品
  return allProducts.value.filter(item => item.orderStatus === 1).length
})

const availableCount = computed(() => {
  // 待售中：商品状态为0且没有订单
  return allProducts.value.filter(item => item.productStatus === 0 && item.orderStatus === null).length
})

const shippedCount = computed(() => {
  // 已发货：订单状态为2（已发货）的商品
  return allProducts.value.filter(item => item.orderStatus === 2).length
})

// 图片处理方法
const getProductImage = (images) => {
  if (!images) {
    return ''
  }
  
  try {
    const imageArray = typeof images === 'string' ? images.split(',') : images
    const firstImage = imageArray[0]?.trim()
    
    if (!firstImage) {
      return ''
    }
    
    return getImageFullUrl(firstImage)
  } catch (error) {
    console.error('处理商品图片出错:', error)
    return ''
  }
}

const handleImageError = (event) => {
  console.error('图片加载失败:', event)
  const imgElement = event.target
  imgElement.style.display = 'none'
}

// 获取商品状态文本
const getProductStatusText = (status) => {
  return productStatusMap[status]?.text || '未知'
}

// 获取商品状态类型
const getProductStatusType = (status) => {
  return productStatusMap[status]?.type || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  if (status === null || status === undefined) return '无订单'
  return orderStatusMap[status]?.text || '未知'
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  if (status === null || status === undefined) return 'info'
  return orderStatusMap[status]?.type || 'info'
}

// 判断是否可以标记发货
const canMarkAsShipped = (product) => {
  // 可以标记发货的条件：有订单且订单状态为1（待付款）
  return product.orderStatus === 1 && !!product.orderId
}

// 加载商品和订单数据
const loadProductsAndOrders = async () => {
  loading.value = true
  try {
    // 获取所有商品数据
    const products = await getMySellProducts()
    
    // 尝试获取卖家订单（需要后端支持）
    try {
      // 注意：这里假设 getMySellOrders 函数已经在 order.js 中添加
      const { getMySellOrders } = await import('@/api/order')
      orders.value = await getMySellOrders()
      console.log('卖家订单数据:', orders.value)
    } catch (error) {
      console.warn('获取卖家订单失败，可能后端未提供此接口:', error)
      orders.value = []
    }
    
    // 处理商品数据，匹配订单信息
    const processedProducts = products.map(product => {
      const processedProduct = { 
        ...product,
        productStatus: product.status, // 保存原始的商品状态
        orderStatus: null, // 默认没有订单
        orderId: null // 默认没有订单ID
      }
      
      // 查找与该商品相关的订单
      if (orders.value.length > 0) {
        // 根据商品ID查找对应的订单
        const matchedOrder = orders.value.find(order => {
          // 根据不同可能的订单结构进行匹配
          if (order.productId === processedProduct.id) {
            return true
          }
          if (order.product && order.product.id === processedProduct.id) {
            return true
          }
          if (order.items && Array.isArray(order.items)) {
            return order.items.some(item => item.productId === processedProduct.id)
          }
          return false
        })
        
        if (matchedOrder) {
          processedProduct.orderId = matchedOrder.id
          processedProduct.orderStatus = matchedOrder.status // 使用user_order表的status
        }
      }
      
      return processedProduct
    })
    
    allProducts.value = processedProducts
    totalProducts.value = processedProducts.length
    
    // 应用筛选和分页
    applyFilterAndPagination()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 应用筛选和分页
const applyFilterAndPagination = () => {
  let filteredProducts = allProducts.value
  
  // 应用商品状态筛选
  if (filterProductStatus.value !== '') {
    filteredProducts = filteredProducts.filter(product => 
      product.productStatus === Number(filterProductStatus.value)
    )
  }
  
  // 更新总数
  totalProducts.value = filteredProducts.length
  
  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  productList.value = filteredProducts.slice(startIndex, endIndex)
}

// 筛选变化处理
const handleFilterChange = () => {
  currentPage.value = 1 // 重置到第一页
  applyFilterAndPagination()
}

const formatDate = (date) => {
  if (!date) return ''
  try {
    return new Date(date).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return date
  }
}

const editProduct = (product) => {
  if (product.productStatus !== 0) return // 只有待售商品可以编辑
  
  currentProduct.value = product
  Object.assign(editForm, {
    id: product.id,
    title: product.title,
    description: product.description,
    price: product.price,
    category: product.category,
    condition: product.condition || '良好'
  })
  editDialogVisible.value = true
}

const saveProduct = () => {
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      await updateProduct(editForm.id, editForm)
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      loadProductsAndOrders()
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败: ' + (error.message || '未知错误'))
    } finally {
      saving.value = false
    }
  })
}

const markAsShipped = async (product) => {
  // 检查条件：订单状态为待付款且有订单ID
  if (product.orderStatus !== 1 || !product.orderId) {
    console.warn('无法发货：订单状态不是待付款或没有订单ID', {
      productId: product.id,
      orderStatus: product.orderStatus,
      orderId: product.orderId
    })
    ElMessage.warning('无法发货：订单状态不正确')
    return
  }
  
  ElMessageBox.confirm(
    '确认标记该商品为已发货吗？此操作将通知买家商品已发出，且不能撤销。', 
    '标记发货', 
    {
      type: 'warning',
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      confirmButtonClass: 'confirm-btn',
      cancelButtonClass: 'cancel-btn'
    }
  ).then(async () => {
    shippingOrderId.value = product.id
    try {
      console.log('调用发货API，订单ID:', product.orderId)
      // 调用发货API，传入订单ID
      await shipOrder(product.orderId)
      
      ElMessage.success('已成功标记为已发货')
      
      // 更新本地数据 - 将订单状态改为2（已发货）
      const index = allProducts.value.findIndex(p => p.id === product.id)
      if (index !== -1) {
        allProducts.value[index] = {
          ...allProducts.value[index],
          orderStatus: 2, // 订单状态变为已发货
          updateTime: new Date().toISOString()
        }
        
        // 重新应用筛选和分页
        applyFilterAndPagination()
      }
    } catch (error) {
      console.error('标记发货失败:', error)
      ElMessage.error(error.message || '标记发货失败')
    } finally {
      shippingOrderId.value = null
    }
  }).catch(() => {
    // 用户取消操作
  })
}

const goToPublish = () => {
  router.push('/publish')
}

const deleteProduct = async (product) => {
  if (product.productStatus !== 0) return // 只有待售商品可以删除
  
  ElMessageBox.confirm(
    '确定删除该商品吗？此操作不可恢复。', 
    '删除商品', 
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      await deleteProductApi(product.id)
      ElMessage.success('商品已删除')
      loadProductsAndOrders()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  })
}

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  applyFilterAndPagination()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  applyFilterAndPagination()
}

onMounted(() => {
  loadProductsAndOrders()
})
</script>

<style scoped>
/* 样式保持不变，与之前的代码相同 */
.my-sell-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fffaf0 0%, #fff9e6 50%, #fff3cc 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  padding: 60px 0 40px;
  position: relative;
  overflow: hidden;
}

.header-background {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
  position: relative;
  z-index: 2;
}

.header-content {
  text-align: center;
  color: white;
}

.page-title {
  font-size: 2.8rem;
  font-weight: 800;
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 500;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: 20%;
  left: 10%;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 80px;
  height: 80px;
  top: 60%;
  right: 15%;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要内容区域 */
.main-content {
  max-width: 1200px;
  margin: -40px auto 0;
  padding: 0 30px 40px;
  position: relative;
  z-index: 3;
}

.content-card {
  border-radius: 20px;
  box-shadow: 0 15px 40px rgba(255, 152, 0, 0.15);
  border: 2px solid #ffe699;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

/* 统计区域 */
.stats-section {
  padding: 30px;
  border-bottom: 1px solid #ffe699;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
}

.stats-content {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.1);
  border: 2px solid #ffe699;
  transition: all 0.3s ease;
  flex: 1;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.2);
  border-color: #ffc107;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.sold-icon {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
}

.available-icon {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.shipped-icon {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #e65100;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 筛选区域 */
.filter-section {
  padding: 30px;
  border-bottom: 1px solid #ffe699;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

:deep(.status-filter .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #ffe699;
  background: white;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
  height: 48px;
}

:deep(.status-filter .el-input__wrapper:hover),
:deep(.status-filter .el-input__wrapper.is-focus) {
  border-color: #ffc107;
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.2);
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #ffe699;
  color: #e65100;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: white;
  border-color: #ffc107;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(255, 152, 0, 0.2);
}

/* 商品列表 */
.product-list {
  padding: 30px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
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
  border: 3px solid #ffc107;
}

.empty-image .el-icon {
  font-size: 48px;
  color: #ff9800;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: #e65100;
}

.empty-state p {
  margin-bottom: 24px;
  color: #666;
}

.publish-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.3);
}

/* 商品卡片 */
.products-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-card {
  background: white;
  border: 2px solid #ffe699;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.1);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.2);
  border-color: #ffc107;
}

.sold-card {
  border-left: 4px solid #ff9800;
}

.shipped-card {
  border-left: 4px solid #2196f3;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ffe699;
  flex-wrap: wrap;
  gap: 12px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-id {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.product-time {
  font-size: 13px;
  color: #666;
}

.status-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.status-tag {
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.2);
}

.shipped-tag {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 11px;
  background: #e8f5e9;
  color: #4caf50;
  border: 1px solid #4caf50;
}

.product-body {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  min-height: 140px;
}

.product-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
}

.product-image {
  flex-shrink: 0;
}

.product-img {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  border: 2px solid #ffe699;
  transition: all 0.3s ease;
}

.product-img:hover {
  border-color: #ffc107;
  transform: scale(1.05);
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.product-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
}

.category-info,
.condition-info,
.price-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.category-label,
.condition-label,
.price-label {
  font-size: 13px;
  color: #666;
}

.category-value {
  font-size: 13px;
  color: #e65100;
  font-weight: 500;
  background: #fff3e0;
  padding: 4px 8px;
  border-radius: 6px;
}

.condition-value {
  font-size: 13px;
  color: #2196f3;
  font-weight: 500;
  background: #e3f2fd;
  padding: 4px 8px;
  border-radius: 6px;
}

.price-amount {
  font-size: 20px;
  font-weight: 700;
  color: #e65100;
  line-height: 1;
}

.view-info,
.update-time,


.view-info .el-icon,
.update-time .el-icon,
.order-info .el-icon {
  font-size: 12px;
}



/* 操作按钮区域 - 新布局 */
.product-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  min-width: 200px;
  padding: 10px 0;
  margin-left: 20px;
}

/* 编辑商品按钮 - 单独一行，居中 */
.edit-btn {
  width: 100%;
  color: #ff9800;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.edit-btn:hover:not(:disabled) {
  color: #e65100;
  transform: translateX(4px);
}

.edit-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 按钮组 - 标记发货和删除按钮在同一行 */
.button-group {
  display: flex;
  width: 100%;
  gap: 12px;
  justify-content: center;
}

/* 标记发货按钮 */
.shipped-btn {
  flex: 1;
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.shipped-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4);
}

.shipped-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
}

/* 删除按钮 */
.delete-btn {
  flex: 1;
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: rgba(244, 67, 54, 0.1);
  border: 2px solid #ffcdd2;
  color: #f44336;
}

.delete-btn:hover:not(:disabled) {
  background: rgba(244, 67, 54, 0.2);
  border-color: #f44336;
  transform: translateY(-2px);
}

.delete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  color: #ccc;
  border-color: #e0e0e0;
  background: rgba(224, 224, 224, 0.2);
}

/* 分页区域 */
.pagination-section {
  padding: 20px 30px;
  border-top: 1px solid #ffe699;
  background: #fffaf0;
}

:deep(.custom-pagination .el-pagination) {
  justify-content: center;
}

:deep(.custom-pagination .el-pager li) {
  border-radius: 8px;
  border: 1px solid #ffe699;
  margin: 0 4px;
  background: white;
  transition: all 0.3s ease;
}

:deep(.custom-pagination .el-pager li:hover) {
  border-color: #ffc107;
  color: #ff9800;
}

:deep(.custom-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-color: #ff9800;
  color: white;
}

:deep(.custom-pagination .btn-prev),
:deep(.custom-pagination .btn-next) {
  border-radius: 8px;
  border: 1px solid #ffe699;
  background: white;
  transition: all 0.3s ease;
}

:deep(.custom-pagination .btn-prev:hover),
:deep(.custom-pagination .btn-next:hover) {
  border-color: #ffc107;
  color: #ff9800;
}

/* 编辑弹窗样式 */
:deep(.edit-dialog .el-dialog) {
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(255, 152, 0, 0.2);
  border: 2px solid #ffe699;
  background: #fffaf0;
}

:deep(.edit-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  margin: 0;
  padding: 24px;
  border-radius: 20px 20px 0 0;
  border-bottom: 2px solid #ffe699;
}

:deep(.edit-dialog .el-dialog__title) {
  color: #e65100;
  font-weight: 700;
  font-size: 18px;
}

/* 编辑弹窗内容 */
.product-edit {
  padding: 24px;
}

.edit-product {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #fff3e0;
  border-radius: 16px;
  margin-bottom: 24px;
  border: 2px solid #ffe699;
}

.edit-img {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  border: 2px solid #ffcc80;
}

.product-info .product-title {
  margin-bottom: 8px;
  font-size: 18px;
}

.price-section .price {
  font-size: 24px;
  font-weight: 700;
  color: #e65100;
  margin-bottom: 12px;
  display: block;
}

.status-section {
  margin-top: 8px;
}

.edit-form {
  margin-top: 24px;
}

:deep(.edit-form .el-form-item__label) {
  font-weight: 600;
  color: #e65100;
}

:deep(.edit-form .el-input__wrapper),
:deep(.edit-form .el-textarea__inner),
:deep(.edit-form .el-select .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #ffe699;
  background: white;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.edit-form .el-input__wrapper:hover),
:deep(.edit-form .el-input__wrapper.is-focus),
:deep(.edit-form .el-textarea__inner:hover),
:deep(.edit-form .el-textarea__inner:focus),
:deep(.edit-form .el-select .el-input__wrapper:hover),
:deep(.edit-form .el-select .el-input__wrapper.is-focus) {
  border-color: #ffc107;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

:deep(.price-input .el-input__wrapper) {
  width: 200px;
}

:deep(.category-select),
:deep(.condition-select) {
  width: 100%;
}

.edit-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 2px solid #ffe699;
}

.cancel-btn,
.save-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cancel-btn {
  background: rgba(255, 193, 7, 0.1);
  border: 2px solid #ffe699;
  color: #e65100;
}

.cancel-btn:hover {
  background: rgba(255, 193, 7, 0.2);
  border-color: #ffc107;
  transform: translateY(-2px);
}

.save-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 40px 0 30px;
  }
  
  .header-background,
  .main-content {
    padding: 0 20px;
  }
  
  .page-title {
    font-size: 2.2rem;
  }
  
  .page-subtitle {
    font-size: 1.1rem;
  }
  
  .stats-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-left,
  .filter-right {
    width: 100%;
  }
  
  .product-body {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .product-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .product-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .product-actions {
    min-width: auto;
    margin-left: 0;
    border-left: none;
    border-top: 1px solid #ffe699;
    padding-top: 20px;
  }
  
  .button-group {
    width: 100%;
  }
  
  :deep(.edit-dialog) {
    width: 95% !important;
    margin: 20px auto;
  }
  
  .edit-product {
    flex-direction: column;
    text-align: center;
  }
  
  .edit-footer {
    flex-direction: column;
  }
  
  .cancel-btn,
  .save-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.8rem;
  }
  
  .product-card {
    padding: 16px;
  }
  
  .product-img {
    width: 80px;
    height: 80px;
  }
  
  .stat-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .shipped-btn,
  .delete-btn {
    width: 100%;
  }
}
.order-tag {
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(33, 150, 243, 0.2);
}
.sold-card {
  border-left: 4px solid #ff9800;
}

.shipped-card {
  border-left: 4px solid #2196f3;
}

/* 标记发货按钮状态 */
.shipped-btn[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
}

.shipped-btn:not(:disabled) {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.shipped-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4);
}

/* 商品状态标签和订单状态标签的间距 */
.status-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}





/* 商品状态标签 */
.status-tag {
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.2);
}
</style>