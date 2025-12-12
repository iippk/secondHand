import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

// 从JWT token中解析用户信息
function parseToken(token) {
  try {
    // JWT格式: header.payload.signature
    const payload = token.split('.')[1]
    // Base64解码 - 添加padding处理
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=')
    const decoded = decodeURIComponent(atob(padded).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(decoded)
  } catch (e) {
    return null
  }
}

request.interceptors.request.use(
  config => {
    // 初始化headers对象，防止undefined错误
    config.headers = config.headers || {}
    
    // 详细调试日志
    console.log('=== 请求拦截器开始 ===')
    console.log('请求URL:', config.url)
    console.log('请求方法:', config.method)
    
    // 检查当前请求是否为登录/注册接口
    const isLoginOrRegister = config.url?.includes('/login') || config.url?.includes('/register')
    console.log('是否为登录/注册API:', isLoginOrRegister)
    
    // 获取token
    const token = localStorage.getItem('token')
    console.log('从localStorage获取到token:', !!token)
    console.log('token长度:', token ? token.length : 0)
    
    // 只对非登录/注册请求设置Authorization头
    if (token && !isLoginOrRegister) {
      // 为非登录请求设置认证头
      config.headers['Authorization'] = token
      console.log('设置Authorization头:', token.substring(0, 20) + '...')
    } else if (isLoginOrRegister) {
      console.log('登录/注册请求，不设置Authorization头')
    }
    
    // 优先从请求体获取studentId（针对登录请求）
    let studentId = null
    if (isLoginOrRegister && config.data) {
      studentId = config.data.studentId || config.data.username || config.data.userId
      console.log('从登录请求体获取到studentId:', studentId)
    }
    
    // 如果请求体中没有，尝试从localStorage获取
    if (!studentId) {
      studentId = localStorage.getItem('studentId')
      console.log('从localStorage获取到studentId:', studentId)
    }
    
    // 如果localStorage中没有，尝试从token中解析
    if (!studentId && token) {
      const tokenData = parseToken(token)
      console.log('解析token结果:', !!tokenData)
      if (tokenData) {
        console.log('token数据:', tokenData)
        studentId = tokenData.studentId || tokenData.sub || tokenData.username || tokenData.userId
        console.log('从token解析的studentId:', studentId)
        if (studentId) {
          localStorage.setItem('studentId', studentId)
        }
      }
    }
    
    // 如果仍然没有studentId，使用默认值作为后备
    if (!studentId) {
      studentId = '123456'
      console.log('使用默认studentId:', studentId)
    }
    
    // 确保始终设置X-Student-Id和X-User-Id请求头
    config.headers['X-Student-Id'] = studentId
    config.headers['X-User-Id'] = studentId
    console.log('已设置X-Student-Id请求头:', studentId)
    console.log('已设置X-User-Id请求头:', studentId)
    
    // 添加默认Content-Type
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    console.log('最终请求头:', config.headers)
    
    console.log('=== 请求拦截器结束 ===')
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 简化401错误处理，不再针对不同接口做特殊逻辑
    if (error.response?.status === 401) {
      // 检查是否是注册/登录接口的401错误
      const isAuthApi = error.config?.url?.includes('/login') || error.config?.url?.includes('/register')
      
      if (isAuthApi) {
        ElMessage.error('认证失败，请检查用户名或密码')
      } else {
        // 对于所有其他401错误，清除token但不立即跳转，让前端自行处理
        // 这样可以让用户信息接口等在获取失败时保持当前页面状态
        localStorage.removeItem('token')
        ElMessage.error('登录已过期或认证失败')
        // 移除强制跳转，让组件自行决定下一步操作
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

