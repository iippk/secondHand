<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="decoration-elements">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="decoration-circle circle-4"></div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="login-content">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-background">
          <div class="brand-content">
            <h1 class="brand-title">校园二手平台</h1>
            <p class="brand-subtitle">闲置物品 · 共享价值 · 绿色校园</p>
            <div class="brand-features">
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="feature-text">
                  <h4>安全交易</h4>
                  <p>平台担保，交易无忧</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="feature-text">
                  <h4>即时沟通</h4>
                  <p>快速联系，高效交流</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="feature-text">
                  <h4>便捷管理</h4>
                  <p>轻松上架，智能管理</p>
                </div>
              </div>
            </div>
          </div>
          <div class="brand-decoration">
            <div class="brand-circle brand-circle-1"></div>
            <div class="brand-circle brand-circle-2"></div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单区 -->
      <div class="form-section">
        <div class="login-card">
          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="studentId">
              <el-input
                v-model="loginForm.studentId"
                placeholder="请输入学号/账号"
                size="large"
                :prefix-icon="User"
                class="login-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
                class="login-input"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-btn"
              >
                <el-icon><CircleCheck /></el-icon>
                登录
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <el-button 
                link 
                type="primary" 
                @click="goRegister" 
                class="register-link"
              >
                <el-icon><UserFilled /></el-icon>
                还没有账号？立即注册
              </el-button>
            </div>
          </el-form>
          
          <!-- 分隔线 -->
          <div class="divider">
            <span class="divider-text">或使用以下方式</span>
          </div>
          
          <!-- 快捷登录 -->
          <div class="quick-login">
            <el-button 
              circle 
              class="quick-btn wechat-btn"
              @click="showWechatLogin"
            >
              <el-icon><ChatRound /></el-icon>
            </el-button>
            <el-button 
              circle 
              class="quick-btn qq-btn"
              @click="showQQLogin"
            >
              <el-icon><Iphone /></el-icon>
            </el-button>
            <el-button 
              circle 
              class="quick-btn phone-btn"
              @click="showPhoneLogin"
            >
              <el-icon><Message /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部信息 -->
    <div class="login-footer">
      <p>校园二手平台 © 2024 致力于打造绿色校园生态</p>
      <p class="footer-links">
        <a href="#" @click.prevent>关于我们</a>
        <span>|</span>
        <a href="#" @click.prevent>服务条款</a>
        <span>|</span>
        <a href="#" @click.prevent>隐私政策</a>
        <span>|</span>
        <a href="#" @click.prevent>帮助中心</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  CircleCheck,
  Clock,
  Box,
  UserFilled,
  ChatRound,
  Iphone,
  Message
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  studentId: '',
  password: ''
})

const loginRules = {
  studentId: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.loginUser(loginForm)
        ElMessage.success({
          message: '登录成功',
          type: 'success',
          customClass: 'login-message'
        })
        router.push('/home')
      } catch (error) {
        ElMessage.error({
          message: error.message || '登录失败，请检查账号和密码',
          type: 'error',
          customClass: 'login-message'
        })
      } finally {
        loading.value = false
      }
    }
  })
}

const goRegister = () => {
  router.push('/register')
}

const showWechatLogin = () => {
  ElMessage.info('微信登录功能即将上线，敬请期待！')
}

const showQQLogin = () => {
  ElMessage.info('QQ登录功能即将上线，敬请期待！')
}

const showPhoneLogin = () => {
  ElMessage.info('手机验证码登录功能即将上线，敬请期待！')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fffaf0 0%, #fff9e6 50%, #fff3cc 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* 装饰元素 */
.decoration-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 193, 7, 0.1);
}

.circle-1 {
  width: 150px;
  height: 150px;
  top: 10%;
  left: 5%;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 100px;
  height: 100px;
  top: 60%;
  left: 10%;
  animation: float 10s ease-in-out infinite reverse;
}

.circle-3 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  right: 8%;
  animation: float 12s ease-in-out infinite;
}

.circle-4 {
  width: 120px;
  height: 120px;
  top: 20%;
  right: 5%;
  animation: float 9s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要内容区域 */
.login-content {
  display: flex;
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
  z-index: 2;
  width: 100%;
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 40px;
}

.brand-background {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-radius: 24px;
  padding: 40px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(255, 152, 0, 0.2);
  border: 3px solid #ffe699;
  width: 100%;
  max-width: 500px;
}

.brand-content {
  position: relative;
  z-index: 2;
  color: white;
}

.brand-title {
  font-size: 2.8rem;
  font-weight: 800;
  margin-bottom: 16px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 500;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateX(8px);
}

.feature-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-text p {
  font-size: 0.9rem;
  opacity: 0.9;
  margin: 0;
}

.brand-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.brand-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.brand-circle-1 {
  width: 120px;
  height: 120px;
  top: -40px;
  right: -40px;
}

.brand-circle-2 {
  width: 80px;
  height: 80px;
  bottom: -20px;
  left: -20px;
}

/* 右侧登录表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 40px;
}

.login-card {
  width: 100%;
  max-width: 450px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 15px 40px rgba(255, 152, 0, 0.15);
  border: 3px solid #ffe699;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #e65100;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

.login-form {
  margin-top: 30px;
}

/* 输入框样式 */
:deep(.login-input .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #ffe699;
  background: white;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.1);
  transition: all 0.3s ease;
  height: 52px;
  padding: 0 15px;
}

:deep(.login-input .el-input__wrapper:hover),
:deep(.login-input .el-input__wrapper.is-focus) {
  border-color: #ffc107;
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.2);
}

:deep(.login-input .el-input__inner) {
  font-size: 16px;
  color: #333;
}

:deep(.login-input .el-icon) {
  color: #ff9800;
  font-size: 18px;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  color: white;
  border-radius: 12px;
  padding: 14px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

:deep(.login-btn .el-icon) {
  font-size: 18px;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
}

.register-link {
  color: #ff9800 !important;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #e65100 !important;
  transform: translateX(4px);
}

:deep(.register-link .el-icon) {
  font-size: 16px;
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  margin: 30px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #ffe699;
}

.divider-text {
  padding: 0 15px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

/* 快捷登录 */
.quick-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.quick-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  font-size: 24px;
  border: 2px solid #ffe699;
  background: white;
  transition: all 0.3s ease;
}

.quick-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 15px rgba(255, 152, 0, 0.2);
}

.wechat-btn {
  color: #07c160;
}

.wechat-btn:hover {
  border-color: #07c160;
  background: rgba(7, 193, 96, 0.05);
}

.qq-btn {
  color: #12b7f5;
}

.qq-btn:hover {
  border-color: #12b7f5;
  background: rgba(18, 183, 245, 0.05);
}

.phone-btn {
  color: #ff9800;
}

.phone-btn:hover {
  border-color: #ff9800;
  background: rgba(255, 152, 0, 0.05);
}

/* 底部信息 */
.login-footer {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
  position: relative;
  z-index: 2;
  border-top: 1px solid #ffe699;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.footer-links {
  margin-top: 8px;
}

.footer-links a {
  color: #ff9800;
  text-decoration: none;
  margin: 0 10px;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #e65100;
  text-decoration: underline;
}

.footer-links span {
  color: #ccc;
  margin: 0 10px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .login-content {
    flex-direction: column;
    padding: 20px;
  }
  
  .brand-section,
  .form-section {
    padding: 0;
    width: 100%;
  }
  
  .brand-section {
    margin-bottom: 40px;
  }
  
  .brand-title {
    font-size: 2.2rem;
  }
  
  .login-card {
    max-width: 500px;
  }
}

@media (max-width: 768px) {
  .brand-title {
    font-size: 1.8rem;
  }
  
  .brand-subtitle {
    font-size: 1rem;
  }
  
  .feature-item {
    padding: 12px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 1.6rem;
  }
}

@media (max-width: 480px) {
  .brand-background {
    padding: 30px 20px;
  }
  
  .brand-title {
    font-size: 1.6rem;
  }
  
  .feature-item {
    flex-direction: column;
    text-align: center;
  }
  
  .quick-login {
    gap: 15px;
  }
  
  .quick-btn {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
}
</style>

<style>
/* 全局消息提示样式 */
.login-message.el-message {
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.2);
  border: 2px solid #ffe699;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-message.el-message--success .el-message__content {
  color: #e65100;
  font-weight: 500;
}

.login-message.el-message--error .el-message__content {
  color: #f44336;
  font-weight: 500;
}

.login-message.el-message--info .el-message__content {
  color: #2196f3;
  font-weight: 500;
}
</style>