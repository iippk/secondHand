<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <h2 class="card-title">发布商品</h2>
          <div class="card-subtitle">分享闲置，传递温暖</div>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="publish-form"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入商品标题，吸引更多买家"
            size="large"
          />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="详细描述商品的功能、使用情况、包含配件等信息"
            resize="none"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
            controls-position="right"
            class="price-input"
          />
          <span class="price-unit">元</span>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select 
            v-model="form.category" 
            placeholder="请选择分类"
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
            v-model="form.condition" 
            placeholder="请选择商品成色"
            class="condition-select"
          >
            <el-option label="全新" value="全新" />
            <el-option label="几乎全新" value="几乎全新" />
            <el-option label="良好" value="良好" />
            <el-option label="一般" value="一般" />
            <el-option label="较差" value="较差" />
          </el-select>
        </el-form-item>
         <el-form-item label="商品图片" prop="images">
          <div class="upload-section">
            <el-upload
              v-model:file-list="fileList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :on-change="handleChange"
              :before-upload="beforeUpload"
              :limit="9"
              accept="image/*"
              class="image-uploader"
            >
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">上传图片</div>
            </el-upload>
            <!-- 简约图片预览对话框 -->
            <el-dialog 
              v-model="dialogVisible" 
              width="auto"
              class="simple-preview-dialog"
              align-center
              :show-header="false"
            >
              <div class="simple-preview-container">
                <img 
                  :src="dialogImageUrl" 
                  alt="预览图片" 
                  class="simple-preview-image" 
                />
              </div>
            </el-dialog>
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              最多上传9张图片，支持 jpg、png、gif 等格式，单张图片不超过5MB
            </div>
          </div>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button 
            type="primary" 
            @click="submitForm" 
            :loading="loading"
            class="submit-btn"
          >
            <el-icon class="btn-icon"><CircleCheck /></el-icon>
            发布商品
          </el-button>
          <el-button @click="resetForm" class="reset-btn">
            <el-icon class="btn-icon"><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createProduct } from '@/api/product'
import { uploadFiles, getImageFullUrl } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { Plus, CircleCheck, Refresh, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')

// 使用 ref 来存储用户信息，确保响应式
const currentUserId = ref('')
const currentUserName = ref('')

// 初始化用户信息
const initUserInfo = () => {
  currentUserId.value = localStorage.getItem('studentId') || ''
  currentUserName.value = localStorage.getItem('userName') || '用户'
  
  console.log('获取到的用户信息:', {
    studentId: currentUserId.value,
    userName: currentUserName.value
  })
}

const form = reactive({
  title: '',
  description: '',
  price: 0,
  category: '',
  condition: '良好',
  images: '',
  sellerId: '',
  sellerName: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择商品成色', trigger: 'change' }]
}

const handlePreview = (file) => {
  if (file.storedPath) {
    dialogImageUrl.value = getImageFullUrl(file.storedPath)
  } else if (file.url && !file.url.startsWith('blob:')) {
    dialogImageUrl.value = file.url
  } else if (file.response?.data?.fullUrl || file.response?.data?.storedPath) {
    dialogImageUrl.value = file.response.data.fullUrl || getImageFullUrl(file.response.data.storedPath)
  } else if (file.raw) {
    dialogImageUrl.value = URL.createObjectURL(file.raw)
  } else {
    dialogImageUrl.value = file.url || ''
  }
  dialogVisible.value = true
}

const handleRemove = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  updateImages()
}

const handleChange = (file, files) => {
  if (file.raw && file.raw.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小超过5MB，请重新上传')
    const index = files.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      files.splice(index, 1)
    }
    fileList.value = files
    return
  }
  
  fileList.value = files
  files.forEach(f => {
    if (f.raw && !f.url && !f.storedPath) {
      f.url = URL.createObjectURL(f.raw)
    }
  })
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小超过5MB，请重新上传')
    return false
  }
  return false
}

const updateImages = () => {
  const storedPaths = fileList.value
    .filter(file => {
      return file.response?.data?.storedPath || file.storedPath
    })
    .map(file => {
      return file.storedPath || file.response?.data?.storedPath || ''
    })
    .filter(path => path)
  
  form.images = storedPaths.join(',')
}

const uploadImages = async () => {
  const filesToUpload = fileList.value
    .filter(file => file.raw && !file.storedPath && !file.response)
    .map(file => file.raw)

  if (filesToUpload.length === 0) {
    updateImages()
    return
  }

  try {
    console.log('开始上传图片，文件数量:', filesToUpload.length)
    
    const storedPaths = await uploadFiles(filesToUpload)
    
    if (storedPaths.length === 0) {
      throw new Error('没有文件上传成功')
    }

    console.log('图片上传成功，存储路径:', storedPaths)

    let uploadedIndex = 0
    fileList.value.forEach((file) => {
      if (file.raw && !file.storedPath && !file.response) {
        const fileIndex = filesToUpload.indexOf(file.raw)
        if (fileIndex !== -1 && uploadedIndex < storedPaths.length) {
          const storedPath = storedPaths[uploadedIndex]
          file.storedPath = storedPath
          file.url = getImageFullUrl(storedPath)
          file.response = { 
            data: { 
              storedPath: storedPath,
              fullUrl: getImageFullUrl(storedPath)
            } 
          }
          uploadedIndex++
        }
      }
    })
    
    updateImages()
  } catch (error) {
    console.error('图片上传失败:', error)
    if (error.message && error.message.includes('Maximum upload size exceeded')) {
      ElMessage.error('图片上传失败: 图片大小超过5MB，请重新上传')
    } else if (error.message && error.message.includes('Cannot delete')) {
      console.warn('临时文件清理失败，但图片可能已上传成功:', error.message)
      updateImages()
    } else {
      ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
      throw error
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (fileList.value.length === 0) {
        ElMessage.warning('请至少上传一张商品图片')
        return
      }

      initUserInfo()

      if (!currentUserId.value) {
        ElMessage.error('请先登录')
        router.push('/login')
        return
      }

      loading.value = true
      try {
        await uploadImages()
        
        form.sellerId = currentUserId.value
        form.sellerName = currentUserName.value
        
        console.log('提交的商品数据:', form)
        
        await createProduct(form)
        ElMessage.success('发布成功')
        router.push('/my-sell')
      } catch (error) {
        console.error('发布商品失败:', error)
        if (error.message && error.message.includes('Cannot delete')) {
          console.warn('临时文件清理失败，但商品可能已发布成功')
          ElMessage.success('发布成功（忽略临时文件清理错误）')
          router.push('/my-sell')
        } else {
          ElMessage.error('发布失败: ' + (error.message || '未知错误'))
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  fileList.value = []
  form.images = ''
  form.condition = '良好'
  initUserInfo()
  form.sellerId = currentUserId.value
  form.sellerName = currentUserName.value
}

onMounted(() => {
  initUserInfo()
  form.sellerId = currentUserId.value
  form.sellerName = currentUserName.value
})
</script>

<style scoped>
.publish-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
  background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 100%);
  min-height: 100vh;
}

.publish-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.15);
  border: 1px solid #ffe0b2;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  text-align: center;
  padding: 20px 0;
  border-bottom: 2px solid #ffecb3;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  margin: -20px -20px 20px -20px;
  border-radius: 16px 16px 0 0;
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

.publish-form {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #e65100;
  font-size: 15px;
}

/* 输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #ffe0b2;
  background: #fffaf0;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  background: #fffdf5;
}

/* 文本域样式 */
:deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 2px solid #ffe0b2;
  background: #fffaf0;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-textarea__inner:hover),
:deep(.el-textarea__inner:focus) {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  background: #fffdf5;
}

/* 选择框样式 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #ffe0b2;
  background: #fffaf0;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-select .el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  background: #fffdf5;
}

/* 选择框下拉菜单样式 */
:deep(.el-select-dropdown) {
  border: 2px solid #ffe0b2;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.2);
  background: #fffaf0;
}

:deep(.el-select-dropdown__item) {
  color: #e65100;
  padding: 12px 20px;
  transition: all 0.3s ease;
}

:deep(.el-select-dropdown__item:hover) {
  background: #fff3e0;
  color: #ff9800;
}

:deep(.el-select-dropdown__item.selected) {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #e65100;
  font-weight: 600;
}

:deep(.el-select-dropdown__item.hover) {
  background: #fff5e6;
}

/* 数字输入框样式 */
.price-input {
  width: 200px;
}

.price-unit {
  margin-left: 12px;
  color: #ff9800;
  font-weight: 600;
  font-size: 16px;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #ffe0b2;
  background: #fffaf0;
}

:deep(.el-input-number .el-input__wrapper:hover),
:deep(.el-input-number .el-input__wrapper.is-focus) {
  border-color: #ff9800;
  background: #fffdf5;
}

:deep(.el-input-number__increase),
:deep(.el-input-number__decrease) {
  background: #fff3e0;
  border-left: 1px solid #ffe0b2;
  color: #ff9800;
  transition: all 0.3s ease;
}

:deep(.el-input-number__increase:hover),
:deep(.el-input-number__decrease:hover) {
  background: #ffe0b2;
  color: #e65100;
}

.upload-section {
  width: 100%;
}

:deep(.image-uploader .el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border: 2px dashed #ffcc80;
  border-radius: 12px;
  background: #fffaf0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

:deep(.image-uploader .el-upload--picture-card:hover) {
  border-color: #ff9800;
  background: #fff5e6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

.upload-icon {
  font-size: 28px;
  color: #ff9800;
  margin-bottom: 8px;
}

.upload-text {
  color: #ff9800;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  border: 2px solid #ffe0b2;
  transition: all 0.3s ease;
}

:deep(.el-upload-list--picture-card .el-upload-list__item:hover) {
  border-color: #ff9800;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

.upload-tip {
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff3e0;
  border-radius: 8px;
  border-left: 4px solid #ff9800;
  color: #e65100;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-tip .el-icon {
  color: #ff9800;
}

/* 简约图片预览对话框样式 */
:deep(.simple-preview-dialog .el-dialog) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: white;
  max-width: 80vw;
  max-height: 80vh;
  margin: 0 auto;
}

:deep(.simple-preview-dialog .el-dialog__body) {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: white;
  border-radius: 8px;
}

.simple-preview-container {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 100%;
  max-height: 100%;
  background: white;
}

.simple-preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

/* 关闭按钮样式 */
:deep(.simple-preview-dialog .el-dialog__headerbtn) {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.simple-preview-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 14px;
}

:deep(.simple-preview-dialog .el-dialog__headerbtn:hover) {
  background: rgba(0, 0, 0, 0.7);
}

.form-actions {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 2px solid #ffecb3;
}

.submit-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  height: auto;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #f57c00 0%, #ef6c00 100%);
}

.reset-btn {
  border: 2px solid #ffcc80;
  border-radius: 12px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  height: auto;
  color: #ff9800;
  background: #fffaf0;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  border-color: #ff9800;
  background: #fff5e6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

.btn-icon {
  margin-right: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .publish-container {
    padding: 20px 15px;
  }
  
  .card-title {
    font-size: 24px;
  }
  
  .form-actions {
    text-align: center;
  }
  
  .submit-btn,
  .reset-btn {
    width: 100%;
    margin-bottom: 12px;
  }
  
  :deep(.simple-preview-dialog .el-dialog) {
    max-width: 95vw;
    max-height: 95vh;
    margin: 20px auto;
  }
  
  .simple-preview-container {
    max-height: calc(95vh - 20px);
  }
}

@media (max-width: 480px) {
  :deep(.simple-preview-dialog .el-dialog) {
    max-width: 98vw;
    max-height: 98vh;
  }
  
  .simple-preview-container {
    max-height: calc(98vh - 20px);
  }
  
  :deep(.image-uploader .el-upload--picture-card),
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
  }
  
  .upload-icon {
    font-size: 24px;
  }
  
  .upload-text {
    font-size: 12px;
  }
}
</style>