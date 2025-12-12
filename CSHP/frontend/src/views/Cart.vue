<template>
  <div class="cart-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>购物车</h2>
          <el-button
            type="danger"
            :disabled="selectedItems.length === 0"
            @click="deleteSelected"
          >
            删除选中
          </el-button>
        </div>
      </template>
      <el-table
        ref="cartTableRef"
        :data="cartList"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="row.productImage"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px; margin-right: 16px"
              />
              <div>
                <div class="title">{{ row.productTitle }}</div>
                <div class="desc">卖家：{{ row.sellerName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="10"
              @change="(value) => updateQuantity(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="140">
          <template #default="{ row }">
            ¥{{ (row.price * row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="deleteItem(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer" v-if="cartList.length">
        <div class="total">已选 {{ selectedItems.length }} 件商品</div>
        <div class="total-price">总计：¥{{ totalPrice }}</div>
        <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedItems.length === 0">
          去下单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCartList, removeFromCart, updateCartQuantity } from '@/api/cart'
import { createOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const cartList = ref([])
const loading = ref(false)
const selectedItems = ref([])
const cartTableRef = ref(null)

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

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

const updateQuantity = async (item, quantity) => {
  try {
    await updateCartQuantity(item.id, quantity)
  } catch (error) {
    ElMessage.error('更新数量失败')
  }
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
  ElMessageBox.confirm('确定删除选中的商品吗？', '提示', {
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
      await Promise.all(selectedItems.value.map(item => createOrder({
        productId: item.productId,
        address: '请填写收货地址',
        phone: '请填写联系方式',
        remark: ''
      })))
      ElMessage.success('订单已创建，请前往我的买入查看')
      loadCart()
    } catch {
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
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-info {
  display: flex;
  align-items: center;
}

.title {
  font-weight: 500;
  color: #303133;
}

.desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20px;
  gap: 20px;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}
</style>

