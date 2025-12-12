<template>
  <div class="my-sell-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>我的卖出</h2>
          <div>
            <el-tag type="success">已售出：{{ soldCount }}</el-tag>
            <el-tag type="info" class="tag">待售：{{ availableCount }}</el-tag>
          </div>
        </div>
      </template>
      <el-table :data="productList" v-loading="loading" border stripe>
        <el-table-column prop="title" label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="getImageUrl(row.images)"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; margin-right: 12px"
                fallback="https://via.placeholder.com/60x60?text=No+Image"
              />
              <div>
                <div class="title">{{ row.title }}</div>
                <div class="desc">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
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
        <el-table-column prop="shipped" label="发货" width="120">
          <template #default="{ row }">
            <el-tag :type="row.shipped ? 'success' : 'info'">
              {{ row.shipped ? '已寄出' : '未寄出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editProduct(row)">编辑</el-button>
            <el-button type="success" link @click="updateStatus(row, 1)" :disabled="row.status === 1">
              标记售出
            </el-button>
            <el-button type="warning" link @click="toggleShipped(row)">
              {{ row.shipped ? '标记未寄出' : '标记已寄出' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑商品" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="editForm.description" :rows="4" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category">
            <el-option label="电子产品" value="电子产品" />
            <el-option label="图书教材" value="图书教材" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服装配饰" value="服装配饰" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getMySellProducts, updateProduct, updateProductStatus, updateProductShipped } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getImageFullUrl } from '@/api/upload'

const loading = ref(false)
const productList = ref([])
const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  title: '',
  price: 0,
  description: '',
  category: ''
})

const soldCount = computed(() => productList.value.filter(item => item.status === 1).length)
const availableCount = computed(() => productList.value.filter(item => item.status === 0).length)

const getStatusText = (status) => {
  const map = {
    0: '待售',
    1: '已售出',
    2: '已下架'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'success',
    1: 'info',
    2: 'warning'
  }
  return map[status] || 'info'
}

const loadProducts = async () => {
  loading.value = true
  try {
    productList.value = await getMySellProducts()
  } catch (error) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const editProduct = (product) => {
  Object.assign(editForm, product)
  editDialogVisible.value = true
}

const saveProduct = async () => {
  try {
    await updateProduct(editForm.id, editForm)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const updateStatus = async (product, status) => {
  try {
    await updateProductStatus(product.id, status)
    ElMessage.success('状态已更新')
    loadProducts()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const toggleShipped = async (product) => {
  try {
    await updateProductShipped(product.id, product.shipped ? 0 : 1)
    ElMessage.success('发货状态已更新')
    loadProducts()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 处理图片URL，确保正确显示
const getImageUrl = (images) => {
  if (!images || !images.trim()) return ''
  
  // 获取第一张图片
  const firstImage = images.split(',')[0].trim()
  
  // 使用全局getImageFullUrl函数处理图片URL
  return getImageFullUrl(firstImage)
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.my-sell-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag {
  margin-left: 10px;
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
</style>

