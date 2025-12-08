<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <div class="header-content">
                <h3 class="card-title">个人信息</h3>
                <div class="card-subtitle">管理您的账户信息</div>
              </div>
              <el-button 
                type="primary" 
                text 
                @click="editInfo = !editInfo"
                class="edit-btn"
              >
                <el-icon><Edit /></el-icon>
                {{ editInfo ? '取消' : '编辑' }}
              </el-button>
            </div>
          </template>
          <el-form :model="userForm" label-width="100px" v-loading="loading" class="profile-form">
            <el-form-item label="学工号">
              <el-input v-model="userForm.studentId" disabled class="disabled-input" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="userForm.name" :disabled="!editInfo" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" :disabled="!editInfo" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" :disabled="!editInfo" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-section">
                <div class="avatar-preview-container">
                  <el-avatar 
                    :size="100" 
                    :src="userForm.avatar || defaultAvatar" 
                    class="avatar-preview"
                    :class="{ 'avatar-editable': editInfo }"
                  />
                  <div class="avatar-overlay" v-if="editInfo">
                    <el-icon><Camera /></el-icon>
                  </div>
                </div>
                <div class="avatar-actions" v-if="editInfo">
                  <input
                    type="file"
                    ref="fileInput"
                    accept="image/jpeg,image/png,image/gif"
                    class="file-input"
                    @change="handleFileSelect"
                  />
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="$refs.fileInput.click()"
                    class="select-avatar-btn"
                  >
                    <el-icon><Upload /></el-icon>
                    选择头像
                  </el-button>
                  <el-button 
                    size="small" 
                    @click="userForm.avatar = ''"
                    v-if="userForm.avatar"
                    class="remove-avatar-btn"
                  >
                    <el-icon><Delete /></el-icon>
                    移除头像
                  </el-button>
                </div>
              </div>
              <div class="avatar-tip" v-if="editInfo">
                <el-icon><InfoFilled /></el-icon>
                支持 JPG、PNG、GIF 格式，大小不超过 5MB
              </div>
            </el-form-item>
            <el-form-item v-if="editInfo" class="form-actions">
              <el-button type="primary" @click="saveUserInfo" :loading="saving" class="save-btn">
                <el-icon><Check /></el-icon>
                保存
              </el-button>
              <el-button @click="cancelEdit" class="cancel-btn">
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="password-card">
          <template #header>
            <div class="card-header">
              <div class="header-content">
                <h3 class="card-title">修改密码</h3>
                <div class="card-subtitle">定期更改密码保护账户安全</div>
              </div>
            </div>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                placeholder="请确认新密码"
                show-password
              />
            </el-form-item>
            <el-form-item class="form-actions">
              <el-button type="primary" @click="changeUserPassword" class="change-password-btn">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm" class="reset-btn">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="stats-card mt-20">
          <template #header>
            <div class="card-header">
              <div class="header-content">
                <h3 class="card-title">统计信息</h3>
                <div class="card-subtitle">您的平台数据概览</div>
              </div>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-icon publish-icon">
                  <el-icon><Goods /></el-icon>
                </div>
                <div class="stat-value">{{ stats.publishCount }}</div>
                <div class="stat-label">发布数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-icon order-icon">
                  <el-icon><ShoppingBag /></el-icon>
                </div>
                <div class="stat-value">{{ stats.orderCount }}</div>
                <div class="stat-label">购买数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-icon income-icon">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="stat-value">¥{{ stats.income }}</div>
                <div class="stat-label">个人收入</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { getMySellProducts } from '@/api/product'
import { getMyBuyOrders } from '@/api/order'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  Edit, 
  Camera, 
  Upload, 
  Delete, 
  InfoFilled, 
  Check, 
  Close, 
  Lock, 
  Refresh,
  Goods,
  ShoppingBag,
  Money
} from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const editInfo = ref(false)
const passwordFormRef = ref(null)
const fileInput = ref(null)
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userForm = reactive({
  studentId: '',
  name: '',
  phone: '',
  email: '',
  avatar: ''
})

const stats = reactive({
  publishCount: 0,
  orderCount: 0,
  income: 0
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型和大小
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return
  }

  // 将图片转换为base64
  const reader = new FileReader()
  reader.onload = (e) => {
    userForm.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  
  // 清空文件输入，以便可以选择同一文件再次上传
  event.target.value = ''
}

const loadUserInfo = async () => {
  loading.value = true
  try {
    const info = await getUserInfo()
    Object.assign(userForm, info)
    localStorage.setItem('userName', info.name)
    // 如果有头像，也存储到localStorage
    if (info.avatar) {
      localStorage.setItem('userAvatar', info.avatar)
    }
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const [sellList, buyList] = await Promise.all([getMySellProducts(), getMyBuyOrders()])
    stats.publishCount = sellList.length
    stats.orderCount = buyList.length
    
    // 计算个人收入：从已发布的商品中筛选状态为1（已售出）的商品，累加价格
    const soldProducts = sellList.filter(product => product.status === 1)
    stats.income = soldProducts.reduce((total, product) => {
      return total + (parseFloat(product.price) || 0)
    }, 0)
    
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.error('加载统计信息失败')
  }
}

const saveUserInfo = async () => {
  saving.value = true
  try {
    // 创建只包含需要更新的字段的对象
    const updateData = {
      name: userForm.name || '',
      phone: userForm.phone || '',
      email: userForm.email || '',
      avatar: userForm.avatar || ''
    }
    
    console.log('发送的更新数据:', updateData)
    
    const result = await updateUserInfo(updateData)
    ElMessage.success('保存成功')
    editInfo.value = false
    
    // 更新本地存储的用户信息
    localStorage.setItem('userName', result.name)
    if (result.avatar) {
      localStorage.setItem('userAvatar', result.avatar)
    }
    
    // 强制刷新用户store中的信息，确保MainLayout立即更新
    await userStore.fetchUserInfo()
    
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const cancelEdit = () => {
  editInfo.value = false
  loadUserInfo()
}

const changeUserPassword = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await changePassword(passwordForm)
      ElMessage.success('密码修改成功')
      resetPasswordForm()
    } catch {
      ElMessage.error('密码修改失败')
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 100%);
  min-height: 100vh;
}

.profile-card,
.password-card,
.stats-card {
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
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.card-subtitle {
  color: #ff9800;
  font-size: 14px;
  font-weight: 500;
}

.edit-btn {
  color: #ff9800;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-btn:hover {
  color: #e65100;
}

.profile-form,
.password-form {
  padding: 20px;
}

:deep(.profile-form .el-form-item__label),
:deep(.password-form .el-form-item__label) {
  font-weight: 600;
  color: #e65100;
  font-size: 14px;
}

:deep(.profile-form .el-input__wrapper),
:deep(.password-form .el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #ffe0b2;
  background: #fffaf0;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.profile-form .el-input__wrapper:hover),
:deep(.profile-form .el-input__wrapper.is-focus),
:deep(.password-form .el-input__wrapper:hover),
:deep(.password-form .el-input__wrapper.is-focus) {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  background: #fffdf5;
}

:deep(.disabled-input .el-input__wrapper) {
  background: #f5f5f5;
  border-color: #e0e0e0;
}

:deep(.disabled-input .el-input__inner) {
  color: #999;
}

/* 头像区域样式 */
.avatar-section {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-preview-container {
  position: relative;
  display: inline-block;
}

.avatar-preview {
  border: 3px solid #ffe0b2;
  transition: all 0.3s ease;
}

.avatar-editable {
  cursor: pointer;
}

.avatar-editable:hover {
  border-color: #ff9800;
  transform: scale(1.05);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 152, 0, 0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-preview-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-input {
  display: none;
}

.select-avatar-btn,
.remove-avatar-btn {
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.select-avatar-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.select-avatar-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.remove-avatar-btn {
  border: 1px solid #ffcc80;
  color: #ff9800;
  background: #fffaf0;
}

.remove-avatar-btn:hover {
  border-color: #ff9800;
  background: #fff5e6;
}

.avatar-tip {
  margin-top: 12px;
  padding: 12px 16px;
  background: #fff3e0;
  border-radius: 8px;
  border-left: 4px solid #ff9800;
  color: #e65100;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表单操作按钮 */
.form-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 2px solid #ffecb3;
  text-align: center;
}

.save-btn,
.cancel-btn,
.change-password-btn,
.reset-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.save-btn,
.change-password-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
}

.save-btn:hover,
.change-password-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #f57c00 0%, #ef6c00 100%);
}

.cancel-btn,
.reset-btn {
  border: 2px solid #ffcc80;
  color: #ff9800;
  background: #fffaf0;
}

.cancel-btn:hover,
.reset-btn:hover {
  border-color: #ff9800;
  background: #fff5e6;
  transform: translateY(-1px);
}

/* 统计信息样式 */
.stats-card {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px 12px;
  background: #fffaf0;
  border-radius: 12px;
  border: 2px solid #ffe0b2;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  border-color: #ff9800;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 24px;
  color: white;
}

.publish-icon {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
}

.order-icon {
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
}

.income-icon {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #e65100;
  margin-bottom: 4px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.mt-20 {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 20px 15px;
  }
  
  .el-row {
    margin: 0 !important;
  }
  
  .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .avatar-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .avatar-actions {
    flex-direction: row;
    justify-content: center;
  }
  
  .form-actions {
    text-align: center;
  }
  
  .save-btn,
  .cancel-btn,
  .change-password-btn,
  .reset-btn {
    width: 100%;
    justify-content: center;
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .card-title {
    font-size: 18px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .avatar-preview {
    width: 80px !important;
    height: 80px !important;
  }
}
</style>