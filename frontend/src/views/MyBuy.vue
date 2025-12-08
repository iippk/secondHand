<template>
  <div class="my-buy-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-background">
        <div class="header-content">
          <h1 class="page-title">我的买入</h1>
          <p class="page-subtitle">管理您的购买订单，轻松追踪交易状态</p>
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
              <div class="stat-icon pending-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ pendingCount }}</div>
                <div class="stat-label">待付款</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon shipped-icon">
                <el-icon><Location /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ shippedCount }}</div>
                <div class="stat-label">待收货</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon completed-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ completedCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选区域 -->
        <div class="filter-section">
          <div class="filter-content">
            <div class="filter-left">
              <el-select 
                v-model="filterStatus" 
                placeholder="订单状态" 
                clearable 
                @change="handleFilterChange"
                class="status-filter"
              >
                <el-option label="全部订单" value=""></el-option>
                <el-option label="待付款" value="0"></el-option>
                <el-option label="已付款" value="1"></el-option>
                <el-option label="已发货" value="2"></el-option>
                <el-option label="已完成" value="3"></el-option>
                <el-option label="已取消" value="4"></el-option>
              </el-select>
            </div>
            <div class="filter-right">
              <el-button 
                @click="loadOrders" 
                :loading="loading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </div>

        <!-- 订单列表 -->
        <div class="order-list" v-loading="loading">
          <div v-if="orderList.length === 0 && !loading" class="empty-state">
            <div class="empty-image">
              <el-icon><Box /></el-icon>
            </div>
            <h3>暂无订单</h3>
            <p>您还没有买入订单，快去选购心仪的商品吧！</p>
            <el-button type="primary" @click="$router.push('/home')" class="browse-btn">
              <el-icon><ShoppingBag /></el-icon>
              去逛逛
            </el-button>
          </div>

          <div v-else class="orders-container">
            <div 
              v-for="order in orderList" 
              :key="order.id" 
              class="order-card"
            >
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatDate(order.createTime) }}</span>
                </div>
                <el-tag :type="getStatusType(order.status)" class="status-tag">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>

              <div class="order-body">
                <div class="product-info">
                  <div class="product-image">
                    <el-image
                      :src="getImageFullUrl(order.productImage)"
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
                    <h4 class="product-title">{{ order.productTitle }}</h4>
                    <div class="product-meta">
                      <!-- 卖家信息 -->
                      <div class="seller-info">
                        <span class="seller-label">卖家：</span>
                        <span class="seller-name">{{ order.sellerName || '未知卖家' }}</span>
                      </div>
                      
                      <div class="price-info">
                        <span class="price-label">实付：</span>
                        <span class="price-amount">¥{{ order.price }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="order-actions">
                  <el-button 
                    type="primary" 
                    link 
                    @click="viewDetail(order)"
                    class="detail-btn"
                  >
                    <el-icon><View /></el-icon>
                    订单详情
                  </el-button>
                  
                  <!-- 待付款状态操作 -->
                  <div v-if="order.status === 0" class="action-group">
                    <el-button
                      type="primary"
                      @click="showPaymentDialog(order)"
                      class="pay-btn"
                    >
                      <el-icon><Money /></el-icon>
                      立即支付
                    </el-button>
                    <el-button
                      @click="handleCancel(order)"
                      class="cancel-btn"
                    >
                      <el-icon><Close /></el-icon>
                      取消订单
                    </el-button>
                  </div>

                  <!-- 已付款状态显示（无操作按钮） -->
                  <div v-if="order.status === 1" class="status-message">
                    <span class="message-text">卖家处理中...</span>
                  </div>

                  <!-- 已发货状态操作 -->
                  <div v-if="order.status === 2" class="action-group">
                    <el-button
                      type="success"
                      @click="handleComplete(order)"
                      class="complete-btn"
                    >
                      <el-icon><Check /></el-icon>
                      确认收货
                    </el-button>
                  </div>

                  <!-- 已完成状态操作 -->
                  <div v-if="order.status === 3" class="action-group">
                    <el-button 
                      type="primary"
                      @click="goToChat(order)"
                      class="chat-btn"
                    >
                      <el-icon><ChatDotRound /></el-icon>
                      联系卖家
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页区域 -->
        <div class="pagination-section" v-if="orderList.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalOrders"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="custom-pagination"
          />
        </div>
      </el-card>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="600px"
      :close-on-click-modal="false"
      class="detail-dialog"
    >
      <div v-if="currentOrder" class="order-detail">
        <!-- 商品信息 -->
        <div class="detail-product">
          <div class="product-image">
            <el-image
              :src="getImageFullUrl(currentOrder.productImage)"
              fit="cover"
              class="detail-img"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="product-info">
            <h4 class="product-title">{{ currentOrder.productTitle }}</h4>
            <div class="price-section">
              <span class="price">¥{{ currentOrder.price }}</span>
            </div>
            <div class="seller-section">
              <el-avatar :size="32" class="seller-avatar">
                {{ currentOrder.sellerName?.charAt(0) || '卖' }}
              </el-avatar>
              <span class="seller-name">{{ currentOrder.sellerName || '未知卖家' }}</span>
            </div>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="detail-info">
          <el-descriptions :column="1" border class="info-table">
            <el-descriptions-item label="订单号">
              <span class="order-no">{{ currentOrder.orderNo }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(currentOrder.status)" class="status-tag">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ formatDate(currentOrder.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="currentOrder.payTime">
              {{ formatDate(currentOrder.payTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="发货时间" v-if="currentOrder.shipTime">
              {{ formatDate(currentOrder.shipTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="完成时间" v-if="currentOrder.completeTime">
              {{ formatDate(currentOrder.completeTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="买家备注">
              {{ currentOrder.remark || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="收货地址">
              {{ currentOrder.address || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ currentOrder.phone || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false" class="close-btn">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="订单支付"
      width="500px"
      :close-on-click-modal="false"
      class="payment-dialog"
    >
      <div v-if="currentOrder" class="payment-content">
        <!-- 订单信息 -->
        <div class="payment-order">
          <div class="order-preview">
            <el-image
              :src="getImageFullUrl(currentOrder.productImage)"
              fit="cover"
              class="payment-img"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="order-info">
              <h4 class="product-title">{{ currentOrder.productTitle }}</h4>
              <div class="payment-price">¥{{ currentOrder.price }}</div>
            </div>
          </div>
        </div>

        <!-- 支付表单 -->
        <div class="payment-form">
          <h4 class="form-title">支付信息</h4>
          <el-form
            ref="paymentFormRef"
            :model="paymentForm"
            :rules="paymentRules"
            label-width="80px"
          >
            <el-form-item label="支付方式" prop="paymentMethod">
              <el-radio-group v-model="paymentForm.paymentMethod" class="payment-methods">
                <el-radio label="alipay" class="payment-method">
                  <div class="method-content">
                    <el-icon><CreditCard /></el-icon>
                    <span>支付宝</span>
                  </div>
                </el-radio>
                <el-radio label="wechat" class="payment-method">
                  <div class="method-content">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>微信支付</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-divider />

            <el-form-item label="收货人" prop="receiverName">
              <el-input
                v-model="paymentForm.receiverName"
                size="large"
              />
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="paymentForm.phone"
                size="large"
              />
            </el-form-item>

            <el-form-item label="收货地址" prop="address">
              <el-input
                v-model="paymentForm.address"
                type="textarea"
                :rows="2"
                resize="none"
              />
            </el-form-item>

            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="paymentForm.remark"
                type="textarea"
                :rows="2"
                resize="none"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <div class="payment-footer">
          <div class="total-amount">
            总计：<span class="amount">¥{{ currentOrder?.price || 0 }}</span>
          </div>
          <div class="payment-actions">
            <el-button @click="paymentDialogVisible = false" class="cancel-btn">取消</el-button>
            <el-button 
              type="primary" 
              @click="handlePayment" 
              :loading="paying" 
              class="confirm-btn"
            >
              <el-icon><Money /></el-icon>
              立即支付
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyBuyOrders, payOrder, cancelOrder, completeOrder, updateOrder } from '@/api/order'
import { getImageFullUrl } from '@/api/upload'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Picture, 
  View, 
  Money, 
  Close, 
  Check,
  CreditCard,
  ChatDotRound,
  Refresh,
  Box,
  ShoppingBag,
  Clock,
  Location,
  CircleCheck
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const orderList = ref([])
const filterStatus = ref('')
const detailDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const currentOrder = ref(null)
const paymentFormRef = ref(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0)

// 存储所有订单数据（用于前端分页和筛选）
const allOrders = ref([])

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'primary' },
  2: { text: '已发货', type: 'success' }, // 状态2表示已发货
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

const paymentForm = reactive({
  paymentMethod: 'alipay',
  receiverName: '',
  phone: '',
  address: '',
  remark: ''
})

const paymentRules = {
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ],
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

// 计算属性
const pendingCount = computed(() => allOrders.value.filter(order => order.status === 0).length)
const shippedCount = computed(() => allOrders.value.filter(order => order.status === 2).length)
const completedCount = computed(() => allOrders.value.filter(order => order.status === 3).length)

// 图片处理方法
const handleImageError = (event) => {
  console.error('图片加载失败:', event)
  const imgElement = event.target
  imgElement.style.display = 'none'
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadOrders = async () => {
  loading.value = true
  try {
    // 获取所有订单数据
    const orders = await getMyBuyOrders()
    allOrders.value = orders
    totalOrders.value = orders.length
    
    // 调试：检查订单状态
    console.log('订单数据:', orders)
    orders.forEach(order => {
      console.log(`订单ID: ${order.id}, 状态: ${order.status} (${getStatusText(order.status)})`)
    })
    
    // 应用筛选和分页
    applyFilterAndPagination()
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 应用筛选和分页
const applyFilterAndPagination = () => {
  let filteredOrders = allOrders.value
  
  // 应用状态筛选
  if (filterStatus.value !== '') {
    filteredOrders = filteredOrders.filter(order => order.status === Number(filterStatus.value))
  }
  
  // 更新总数
  totalOrders.value = filteredOrders.length
  
  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  orderList.value = filteredOrders.slice(startIndex, endIndex)
}

// 筛选变化处理
const handleFilterChange = () => {
  currentPage.value = 1 // 重置到第一页
  applyFilterAndPagination()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const viewDetail = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const showPaymentDialog = (order) => {
  currentOrder.value = order
  // 重置表单
  Object.assign(paymentForm, {
    paymentMethod: 'alipay',
    receiverName: order.buyerName || '',
    phone: order.phone || '',
    address: order.address || '',
    remark: order.remark || ''
  })
  paymentDialogVisible.value = true
}

const handlePayment = () => {
  paymentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 确认支付弹窗
    ElMessageBox.confirm(
      `确定要支付订单 ${currentOrder.value.orderNo} 吗？<br>金额：¥${currentOrder.value.price}<br>支付方式：${getPaymentMethodText(paymentForm.paymentMethod)}`,
      '确认支付',
      {
        dangerouslyUseHTMLString: true,
        type: 'warning',
        confirmButtonText: '确认支付',
        cancelButtonText: '再想想'
      }
    ).then(async () => {
      paying.value = true
      try {
        // 先更新订单信息（收货地址等）
        await updateOrder(currentOrder.value.id, {
          address: paymentForm.address,
          phone: paymentForm.phone,
          remark: paymentForm.remark,
          buyerName: paymentForm.receiverName
        })
        
        // 然后支付订单
        await payOrder(currentOrder.value.id)
        
        ElMessage.success('支付成功')
        paymentDialogVisible.value = false
        loadOrders()
      } catch (error) {
        console.error('支付失败:', error)
        ElMessage.error('支付失败: ' + (error.message || '未知错误'))
      } finally {
        paying.value = false
      }
    })
  })
}

const getPaymentMethodText = (method) => {
  const methods = {
    alipay: '支付宝',
    wechat: '微信支付'
  }
  return methods[method] || '未知'
}

const handleCancel = (order) => {
  ElMessageBox.confirm('确定取消该订单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  })
}

const handleComplete = (order) => {
  ElMessageBox.confirm('确认已收到商品吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await completeOrder(order.id)
    ElMessage.success('订单已完成')
    loadOrders()
  })
}

const goToChat = (order) => {
  // 跳转到聊天页面
  router.push('/chat')
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
  loadOrders()
})
</script>

<style scoped>
.my-buy-container {
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

.pending-icon {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.shipped-icon {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
}

.completed-icon {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
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

/* 订单列表 */
.order-list {
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

.browse-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.browse-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.3);
}

/* 订单卡片 */
.orders-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: white;
  border: 2px solid #ffe699;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.1);
}

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.2);
  border-color: #ffc107;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ffe699;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-no {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.order-time {
  font-size: 13px;
  color: #666;
}

.status-tag {
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.2);
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.product-image {
  flex-shrink: 0;
}

.product-img {
  width: 80px;
  height: 80px;
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
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.seller-label {
  font-size: 13px;
  color: #666;
}

.seller-name {
  font-size: 13px;
  color: #2196f3;
  font-weight: 500;
  background: #e3f2fd;
  padding: 4px 8px;
  border-radius: 6px;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-label {
  font-size: 13px;
  color: #666;
}

.price-amount {
  font-size: 18px;
  font-weight: 700;
  color: #e65100;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 200px;
}

.detail-btn {
  color: #ff9800;
  font-weight: 500;
  padding: 8px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  transition: all 0.3s ease;
}

.detail-btn:hover {
  color: #e65100;
  transform: translateX(4px);
}

.status-message {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 8px;
}

.message-text {
  font-size: 13px;
  color: #666;
  font-style: italic;
}

.action-group {
  display: flex;
  gap: 8px;
}

.pay-btn,
.complete-btn,
.cancel-btn,
.chat-btn {
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  justify-content: center;
}

.pay-btn,
.complete-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.pay-btn:hover,
.complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
}

.chat-btn {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4);
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

/* 弹窗样式 */
:deep(.detail-dialog .el-dialog),
:deep(.payment-dialog .el-dialog) {
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(255, 152, 0, 0.2);
  border: 2px solid #ffe699;
  background: #fffaf0;
}

:deep(.detail-dialog .el-dialog__header),
:deep(.payment-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  margin: 0;
  padding: 24px;
  border-radius: 20px 20px 0 0;
  border-bottom: 2px solid #ffe699;
}

:deep(.detail-dialog .el-dialog__title),
:deep(.payment-dialog .el-dialog__title) {
  color: #e65100;
  font-weight: 700;
  font-size: 18px;
}

/* 详情弹窗内容 */
.order-detail {
  padding: 24px;
}

.detail-product {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #fff3e0;
  border-radius: 16px;
  margin-bottom: 24px;
  border: 2px solid #ffe699;
}

.detail-img {
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

.seller-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seller-section .seller-avatar {
  background: linear-gradient(135deg, #e65100 0%, #ff5722 100%);
}

.seller-section .seller-name {
  font-weight: 600;
  color: #333;
}

.detail-info {
  margin-top: 24px;
}

:deep(.info-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.info-table .el-descriptions__label) {
  background: #fff3e0;
  color: #e65100;
  font-weight: 600;
  width: 100px;
}

:deep(.info-table .el-descriptions__content) {
  background: #fffaf0;
  color: #333;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #e65100;
}

.close-btn {
  background: rgba(255, 193, 7, 0.1);
  border: 2px solid #ffe699;
  color: #e65100;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 193, 7, 0.2);
  border-color: #ffc107;
  transform: translateY(-2px);
}

/* 支付弹窗样式 */
.payment-content {
  padding: 24px;
}

.payment-order {
  margin-bottom: 24px;
}

.order-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff3e0;
  border-radius: 16px;
  border: 2px solid #ffe699;
}

.payment-img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  border: 2px solid #ffcc80;
}

.order-info .product-title {
  margin-bottom: 8px;
  font-size: 16px;
}

.payment-price {
  font-size: 20px;
  font-weight: 700;
  color: #e65100;
}

.payment-form {
  margin-top: 24px;
}

.form-title {
  color: #e65100;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ffe699;
}

:deep(.payment-form .el-form-item__label) {
  font-weight: 600;
  color: #e65100;
}

:deep(.payment-form .el-input__wrapper),
:deep(.payment-form .el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #ffe699;
  background: white;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.payment-form .el-input__wrapper:hover),
:deep(.payment-form .el-input__wrapper.is-focus),
:deep(.payment-form .el-textarea__inner:hover),
:deep(.payment-form .el-textarea__inner:focus) {
  border-color: #ffc107;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

/* 修复支付方式对齐问题 */
.payment-methods {
  display: flex;
  gap: 12px;
  width: 100%;
}

.payment-method {
  flex: 1;
  padding: 16px;
  border: 2px solid #ffe699;
  border-radius: 12px;
  background: white;
  transition: all 0.3s ease;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.method-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  justify-content: center;
  width: 100%;
}

:deep(.payment-method .el-radio__input) {
  display: none;
}

:deep(.payment-method .el-radio__label) {
  padding-left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

:deep(.payment-method.is-checked) {
  border-color: #ff9800;
  background: #fff3e0;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

.payment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 2px solid #ffe699;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #e65100;
}

.amount {
  font-size: 24px;
  font-weight: 700;
}

.payment-actions {
  display: flex;
  gap: 12px;
}

.cancel-btn,
.confirm-btn {
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

.confirm-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
}

.confirm-btn:hover {
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
  
  .order-body {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .product-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .product-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .order-actions {
    min-width: auto;
  }
  
  .action-group {
    flex-direction: column;
  }
  
  :deep(.detail-dialog),
  :deep(.payment-dialog) {
    width: 95% !important;
    margin: 20px auto;
  }
  
  .detail-product {
    flex-direction: column;
    text-align: center;
  }
  
  .payment-footer {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .payment-actions {
    width: 100%;
    justify-content: center;
  }
  
  .payment-methods {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.8rem;
  }
  
  .order-card {
    padding: 16px;
  }
  
  .product-img {
    width: 60px;
    height: 60px;
  }
  
  .stat-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .payment-methods {
    flex-direction: column;
  }
}
</style>