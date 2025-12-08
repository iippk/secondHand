<template>
  <div class="register-container">
    <div class="register-content">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-logo">
          <el-icon class="logo-icon"><ShoppingBag /></el-icon>
          <h1>校园二手平台</h1>
        </div>
        <div class="brand-info">
          <h2>加入校园二手社区</h2>
          <p>安全交易 · 便捷发布 · 绿色校园</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>安全可靠</span>
          </div>
          <div class="feature-item">
            <el-icon><Clock /></el-icon>
            <span>快速便捷</span>
          </div>
          <div class="feature-item">
            <el-icon><User /></el-icon>
            <span>学生专属</span>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h3>创建账户</h3>
            <p>开启你的二手交易之旅</p>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
          >
            <el-form-item prop="studentId">
              <el-input
                v-model="registerForm.studentId"
                placeholder="学号/账号"
                size="large"
                :prefix-icon="User"
                class="form-input"
              />
            </el-form-item>

            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="用户名"
                size="large"
                :prefix-icon="Edit"
                class="form-input"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="设置密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="form-input"
              />
            </el-form-item>

            <el-form-item>
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号（选填）"
                size="large"
                :prefix-icon="Iphone"
                class="form-input"
              />
            </el-form-item>

            <el-form-item>
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱（选填）"
                size="large"
                :prefix-icon="Message"
                class="form-input"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                @click="handleRegister"
                class="submit-btn"
                size="large"
              >
                <template #default>
                  <el-icon><CircleCheck /></el-icon>
                  立即注册
                </template>
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <span>已有账号？</span>
              <el-button link type="primary" @click="goLogin">
                立即登录
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="footer">
      <p>校园二手平台 © 2024</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register as registerApi } from '@/api/user'
import {
  ShoppingBag,
  CircleCheck,
  User,
  Edit,
  Lock,
  Iphone,
  Message,
  Clock
} from '@element-plus/icons-vue'

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
  studentId: [
    { required: true, message: '请输入学号账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 10, message: '用户名长度在2-10个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, max: 18, message: '密码长度需在6-18位', trigger: 'blur' }
  ]
}

const handleRegister = () => {
  if (!registerFormRef.value) return
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await registerApi(registerForm)
      ElMessage.success('注册成功，即将跳转到登录页面')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } catch (error) {
      // 错误处理
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
  min-height: 100vh;
  background: linear-gradient(135deg, #fffaf0 0%, #fff9e6 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.register-content {
  display: flex;
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(255, 152, 0, 0.08);
  overflow: hidden;
  min-height: 600px;
}

/* 品牌区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 36px;
}

.brand-logo h1 {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
}

.brand-info {
  margin-bottom: 40px;
}

.brand-info h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 10px;
  line-height: 1.3;
}

.brand-info p {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
}

.feature-item .el-icon {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 10px;
  font-size: 20px;
}

/* 表单区 */
.form-section {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
}

.form-card {
  width: 100%;
  max-width: 380px;
  margin: 0 auto;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h3 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #e65100;
  margin-bottom: 8px;
}

.form-header p {
  color: #666;
  margin: 0;
}

.register-form {
  margin-top: 20px;
}

/* 输入框样式 */
:deep(.form-input .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #ffe699;
  background: #fffaf0;
  box-shadow: none;
  height: 50px;
  transition: all 0.3s ease;
}

:deep(.form-input .el-input__wrapper:hover),
:deep(.form-input .el-input__wrapper.is-focus) {
  border-color: #ffc107;
  background: white;
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.1);
}

:deep(.form-input .el-input__inner) {
  font-size: 15px;
  color: #333;
}

:deep(.form-input .el-icon) {
  color: #ff9800;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  border-radius: 12px;
  padding: 14px;
  font-weight: 600;
  color: white;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.2);
}

.submit-btn .el-icon {
  margin-right: 8px;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.form-footer .el-button {
  margin-left: 5px;
}

/* 底部信息 */
.footer {
  margin-top: 30px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    flex-direction: column;
    min-height: auto;
  }
  
  .brand-section {
    padding: 40px 20px;
  }
  
  .brand-logo h1 {
    font-size: 1.5rem;
  }
  
  .brand-info h2 {
    font-size: 1.6rem;
  }
  
  .form-section {
    padding: 40px 20px;
  }
  
  .form-card {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 10px;
  }
  
  .brand-logo {
    margin-bottom: 30px;
  }
  
  .features {
    gap: 12px;
  }
  
  .feature-item {
    font-size: 0.9rem;
  }
}
</style>