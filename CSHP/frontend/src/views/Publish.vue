<template>
  <div class="publish-container">
    <el-card>
      <template #header>
        <h2>发布商品</h2>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="电子产品" value="电子产品" />
            <el-option label="图书教材" value="图书教材" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服装配饰" value="服装配饰" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
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
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="dialogVisible" title="图片预览" width="800px">
            <img :src="dialogImageUrl" alt="预览图片" style="width: 100%" />
          </el-dialog>
          <div class="tip">提示：最多上传9张图片，支持 jpg、png、gif 等格式，单张图片不超过5MB</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            发布商品
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createProduct } from '@/api/product'
import { uploadFiles } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const form = reactive({
  title: '',
  description: '',
  price: 0,
  category: '',
  images: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const handlePreview = (file) => {
  dialogImageUrl.value = file.url || file.response?.data || file.raw?.url
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
  fileList.value = files
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return false // 阻止自动上传，手动上传
}

const updateImages = () => {
  const urls = fileList.value
    .filter(file => file.url || file.response?.data)
    .map(file => file.url || file.response?.data)
  form.images = urls.join(',')
}

const uploadImages = async () => {
  const filesToUpload = fileList.value
    .filter(file => file.raw && !file.url && !file.response)
    .map(file => file.raw)

  if (filesToUpload.length === 0) {
    updateImages()
    return
  }

  try {
    const urls = await uploadFiles(filesToUpload)
    // 更新文件列表，添加已上传的URL
    fileList.value.forEach((file, index) => {
      if (file.raw && !file.url && !file.response) {
        const uploadedIndex = filesToUpload.indexOf(file.raw)
        if (uploadedIndex !== -1 && urls[uploadedIndex]) {
          file.url = urls[uploadedIndex]
          file.response = { data: urls[uploadedIndex] }
        }
      }
    })
    updateImages()
  } catch (error) {
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    throw error
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 验证至少有一张图片
      if (fileList.value.length === 0) {
        ElMessage.warning('请至少上传一张商品图片')
        return
      }

      loading.value = true
      try {
        // 先上传图片
        await uploadImages()
        
        // 再提交商品信息
        await createProduct(form)
        ElMessage.success('发布成功')
        router.push('/my-sell')
      } catch (error) {
        ElMessage.error(error.message || '发布失败')
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
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>

