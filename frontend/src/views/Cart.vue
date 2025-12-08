<template>
  <div class="cart-container">
    <el-card class="cart-card">
      <template #header>
        <div class="card-header">
          <div class="header-content">
            <h2 class="card-title">购物车</h2>
            <div class="card-subtitle">精心挑选，轻松结算</div>
          </div>
          <el-button
            type="danger"
            :disabled="selectedItems.length === 0"
            @click="deleteSelected"
            class="delete-selected-btn"
          >
            <el-icon><Delete /></el-icon>
            删除选中
          </el-button>
        </div>
      </template>
      
      <el-table
        ref="cartTableRef"
        :data="cartList"
        class="cart-table"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :empty-text="emptyText"
      >
        <el-table-column type="selection" width="60" align="center" />
        
        <el-table-column label="商品信息" min-width="320">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-image-container">
                <el-image
                  :src="getImageFullUrl(row.productImage)"
                  fit="cover"
                  class="product-image"
                  @error="handleImageError"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="product-details">
                <div class="title">{{ row.productTitle }}</div>
                <div class="seller-info">
                  <el-icon><User /></el-icon>
                  卖家：{{ row.sellerName }}
                </div>
                <div class="add-time" v-if="row.createTime">
                  添加时间：{{ formatDate(row.createTime) }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            <div class="price-cell">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ row.price }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              link 
              @click="deleteItem(row)"
              class="delete-btn"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 结算栏 -->
      <div class="checkout-footer" v-if="cartList.length">
        <div class="checkout-info">
          <div class="selected-count">
            已选 <span class="count-number">{{ selectedItems.length }}</span> 件商品
          </div>
          <div class="total-price">
            总计：<span class="price-number">¥{{ totalPrice }}</span>
          </div>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          @click="handleCheckout" 
          :disabled="selectedItems.length === 0"
          class="checkout-btn"
        >
          <el-icon><ShoppingCart /></el-icon>
          去下单 ({{ selectedItems.length }})
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCartList, removeFromCart } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getImageFullUrl } from '@/api/upload'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Delete, User, ShoppingCart } from '@element-plus/icons-vue'

const cartList = ref([])
const loading = ref(false)
const selectedItems = ref([])
const cartTableRef = ref(null)

const emptyText = computed(() => 
  cartList.value.length === 0 && !loading.value ? '购物车空空如也，快去选购吧！' : '暂无数据'
)

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price, 0).toFixed(2)
})

const handleImageError = (event) => {
  console.error('图片加载失败:', event)
  const imgElement = event.target
  imgElement.style.display = 'none'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

const loadCart = async () => {
  loading.value = true
  try {
    cartList.value = await getCartList()
  } catch (error) {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const deleteItem = (item) => {
  ElMessageBox.confirm('确定删除该商品吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await removeFromCart(item.id)
    ElMessage.success('删除成功')
    loadCart()
  })
}

const deleteSelected = () => {
  if (!selectedItems.value.length) return
  ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await Promise.all(selectedItems.value.map(item => removeFromCart(item.id)))
    ElMessage.success('删除成功')
    loadCart()
  })
}

const handleCheckout = () => {
  ElMessageBox.confirm(`确定购买选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      // 创建订单
      await Promise.all(selectedItems.value.map(item => createOrder({
        productId: item.productId,
        address: '请填写收货地址',
        phone: '请填写联系方式',
        remark: ''
      })))
      
      // 下单成功后从购物车中删除已购买的商品
      await Promise.all(selectedItems.value.map(item => removeFromCart(item.id)))
      
      ElMessage.success('订单已创建，已从购物车中移除')
      loadCart()
    } catch (error) {
      ElMessage.error('下单失败')
    }
  })
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 100%);
  min-height: 100vh;
}

.cart-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.15);
  border: 1px solid #ffe0b2;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 2px solid #ffecb3;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  margin: -20px -20px 20px -20px;
  padding: 20px;
  border-radius: 16px 16px 0 0;
}

.header-content {
  flex: 1;
}

.card-title {
  color: #e65100;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(230, 81, 0, 0.1);
}

.card-subtitle {
  color: #ff9800;
  font-size: 16px;
  font-weight: 500;
}

.delete-selected-btn {
  background: linear-gradient(135deg, #f44336 0%, #e53935 100%);
  border: none;
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.delete-selected-btn:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(244, 67, 54, 0.4);
  background: linear-gradient(135deg, #e53935 0%, #d32f2f 100%);
}

.delete-selected-btn.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 表格样式 */
:deep(.cart-table) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(255, 152, 0, 0.1);
}

:deep(.cart-table .el-table__header) {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
}

:deep(.cart-table .el-table__header th) {
  background: transparent;
  color: #e65100;
  font-weight: 600;
  border-bottom: 2px solid #ffecb3;
}

:deep(.cart-table .el-table__row) {
  transition: all 0.3s ease;
}

:deep(.cart-table .el-table__row:hover) {
  background: #fffaf0;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.1);
}

:deep(.cart-table .el-table__cell) {
  border-bottom: 1px solid #ffe0b2;
  padding: 16px 8px;
}

:deep(.cart-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #ff9800;
  border-color: #ff9800;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-image-container {
  flex-shrink: 0;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  border: 2px solid #ffe0b2;
  transition: all 0.3s ease;
}

.product-image:hover {
  border-color: #ff9800;
  transform: scale(1.05);
}

.product-details {
  flex: 1;
  min-width: 0;
}

.title {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
  margin-bottom: 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #ff9800;
  background: #fff3e0;
  padding: 4px 8px;
  border-radius: 6px;
  width: fit-content;
  margin-bottom: 6px;
}

.add-time {
  font-size: 12px;
  color: #909399;
}

/* 价格样式 */
.price-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.price-symbol {
  color: #ff9800;
  font-weight: 600;
  font-size: 14px;
}

.price-value {
  color: #e65100;
  font-weight: 700;
  font-size: 18px;
}

/* 删除按钮样式 */
.delete-btn {
  color: #f44336;
  background: #ffebee;
  padding: 6px 10px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.delete-btn:hover {
  background: #ffcdd2;
  transform: translateY(-1px);
}

/* 图片加载失败样式 */
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

/* 结算栏样式 */
.checkout-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding: 20px;
  background: #fff3e0;
  border-radius: 12px;
  border: 2px solid #ffe0b2;
}

.checkout-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.selected-count {
  color: #606266;
  font-size: 16px;
}

.count-number {
  color: #ff9800;
  font-weight: 600;
  font-size: 18px;
}

.total-price {
  color: #606266;
  font-size: 16px;
}

.price-number {
  color: #e65100;
  font-weight: 700;
  font-size: 24px;
}

.checkout-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkout-btn:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #f57c00 0%, #ef6c00 100%);
}

.checkout-btn.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-container {
    padding: 20px 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .product-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .product-details {
    width: 100%;
  }
  
  .seller-info {
    align-self: center;
  }
  
  .checkout-footer {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .checkout-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .checkout-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .card-title {
    font-size: 24px;
  }
  
  .product-image {
    width: 60px;
    height: 60px;
  }
  
  .delete-selected-btn,
  .checkout-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>