<template>
  <div class="my-buy-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>我的买入</h2>
          <el-select v-model="filterStatus" placeholder="订单状态" clearable @change="loadOrders">
            <el-option label="全部" value=""></el-option>
            <el-option label="待付款" value="0"></el-option>
            <el-option label="已付款" value="1"></el-option>
            <el-option label="已发货" value="2"></el-option>
            <el-option label="已完成" value="3"></el-option>
            <el-option label="已取消" value="4"></el-option>
            <el-option label="退款中" value="5"></el-option>
            <el-option label="已退款" value="6"></el-option>
          </el-select>
        </div>
      </template>

      <el-table :data="orderList" v-loading="loading" border stripe>
        <el-table-column label="订单信息" min-width="300">
          <template #default="{ row }">
            <div class="order-info">
              <div class="order-product">
                <img 
                  v-if="row.productImages && row.productImages.trim()"
                  :src="getImageUrl(row.productImages)"
                  :alt="row.productTitle"
                  class="product-thumb"
                />
                <div class="product-details">
                  <div class="order-title">{{ row.productTitle }}</div>
                  <div class="order-no">订单号：{{ row.orderNo }}</div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button
              type="success"
              link
              v-if="row.status === 0"
              @click="handlePay(row)"
            >支付</el-button>
            <el-button
              type="warning"
              link
              v-if="row.status === 0"
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button
              type="danger"
              link
              v-if="[1,2].includes(row.status)"
              @click="handleRefund(row)"
            >退款</el-button>
            <el-button
              type="success"
              link
              v-if="row.status === 2"
              @click="handleComplete(row)"
            >确认收货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyBuyOrders, payOrder, cancelOrder, refundOrder, completeOrder } from '@/api/order'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getImageFullUrl } from '@/api/upload'

const loading = ref(false)
const orderList = ref([])
const filterStatus = ref('')

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'info' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '退款中', type: 'warning' },
  6: { text: '已退款', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadOrders = async () => {
  loading.value = true
  try {
    const orders = await getMyBuyOrders()
    orderList.value = filterStatus.value
      ? orders.filter(order => order.status === Number(filterStatus.value))
      : orders
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const viewDetail = (order) => {
  ElMessage.info('订单详情功能开发中')
}

const handlePay = (order) => {
  ElMessageBox.confirm('确定要支付该订单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  })
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

const handleRefund = (order) => {
  ElMessageBox.confirm('确定申请退款吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await refundOrder(order.id)
    ElMessage.success('已提交退款申请')
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

// 处理图片URL，确保正确显示
const getImageUrl = (images) => {
  if (!images || !images.trim()) return ''
  // 获取第一张图片
  const firstImage = images.split(',')[0].trim()
  return getImageFullUrl(firstImage)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-buy-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  line-height: 1.6;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-details {
  flex: 1;
}

.order-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.order-no {
  font-size: 12px;
  color: #909399;
}
</style>

