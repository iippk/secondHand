<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>校园二手平台</h2>
        <p>欢迎注册账号</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <el-form-item label="学工号" prop="studentId">
          <el-input v-model="registerForm.studentId" placeholder="请输入学工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            placeholder="请输入身份证后六位"
            type="password"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister">
            注册
          </el-button>
          <el-button link type="primary" @click="goLogin">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register as registerApi } from '@/api/user'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  studentId: '',
  name: '',
  password: '',
  phone: '',
  email: ''
})

const registerRules = {
  studentId: [{ required: true, message: '请输入学工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 18, message: '密码长度需在 6-18 位', trigger: 'blur' }
  ]
}

const handleRegister = () => {
  if (!registerFormRef.value) return
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await registerApi(registerForm)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      // 后端已统一返回 message
    } finally {
      loading.value = false
    }
  })
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
}

.register-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h2 {
  color: #303133;
  margin-bottom: 8px;
}

.register-header p {
  color: #909399;
}
</style>

