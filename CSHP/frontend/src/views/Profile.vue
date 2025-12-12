<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>个人信息</h3>
              <el-button type="primary" text @click="editInfo = !editInfo">
                {{ editInfo ? '取消' : '编辑' }}
              </el-button>
            </div>
          </template>
          <el-form :model="userForm" label-width="80px" v-loading="loading">
            <el-form-item label="学工号">
              <el-input v-model="userForm.studentId" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="userForm.name" :disabled="!editInfo" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" :disabled="!editInfo" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" :disabled="!editInfo" />
            </el-form-item>
            <el-form-item label="头像">
              <el-input v-model="userForm.avatar" :disabled="!editInfo" />
            </el-form-item>
            <el-form-item v-if="editInfo">
              <el-button type="primary" @click="saveUserInfo">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>修改密码</h3>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changeUserPassword">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="mt-20">
          <template #header>
            <h3>统计信息</h3>
          </template>
          <el-row>
            <el-col :span="12">
              <div class="stat-card">
                <div class="stat-value">{{ stats.publishCount }}</div>
                <div class="stat-label">发布数量</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-card">
                <div class="stat-value">{{ stats.orderCount }}</div>
                <div class="stat-label">购买数量</div>
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

const loading = ref(false)
const editInfo = ref(false)
const passwordFormRef = ref(null)

const userForm = reactive({
  studentId: '',
  name: '',
  phone: '',
  email: '',
  avatar: ''
})

const stats = reactive({
  publishCount: 0,
  orderCount: 0
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

const loadUserInfo = async () => {
  loading.value = true
  try {
    const info = await getUserInfo()
    Object.assign(userForm, info)
    localStorage.setItem('userName', info.name)
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
  } catch {
    ElMessage.error('加载统计信息失败')
  }
}

const saveUserInfo = async () => {
  try {
    await updateUserInfo(userForm)
    ElMessage.success('保存成功')
    editInfo.value = false
  } catch {
    ElMessage.error('保存失败')
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
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  color: #909399;
  margin-top: 8px;
}

.mt-20 {
  margin-top: 20px;
}
</style>

