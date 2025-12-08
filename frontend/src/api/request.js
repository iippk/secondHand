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
    
    // 检查当前请求是否为公开接口
    const isPublicApi = config.url?.includes('/login') || 
                       config.url?.includes('/register') ||
                       config.url?.includes('/uploads/') ||
                       config.url?.includes('/public/')
    console.log('是否为公开API:', isPublicApi)
    
    // 获取token
    const token = localStorage.getItem('token')
    console.log('从localStorage获取到token:', !!token)
    console.log('token长度:', token ? token.length : 0)
    
    // 只对需要认证的请求设置Authorization头
    if (token && !isPublicApi) {
      // 为需要认证的请求设置认证头
      config.headers['Authorization'] = token
      console.log('设置Authorization头:', token.substring(0, 20) + '...')
    } else if (isPublicApi) {
      console.log('公开API请求，不设置Authorization头')
    } else {
      console.log('无token且非公开API，不设置Authorization头')
    }
    
    // 优先从请求体获取studentId（针对登录请求）
    let studentId = null
    if (config.url?.includes('/login') && config.data) {
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
      // 对于业务逻辑错误，只在非静默模式下显示错误
      if (!response.config?.silent) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    const currentPath = window.location.pathname
    
    // 如果是401错误
    if (error.response?.status === 401) {
      // 检查是否是认证相关的API（登录/注册）
      const isAuthApi = error.config?.url?.includes('/login') || error.config?.url?.includes('/register')
      
      if (isAuthApi) {
        // 登录/注册接口的401错误，显示具体错误信息
        ElMessage.error('认证失败，请检查用户名或密码')
      } else {
        // 对于其他API的401错误，清除用户状态
        localStorage.removeItem('token')
        localStorage.removeItem('studentId')
        localStorage.removeItem('userName')
        
        // 如果当前不在登录页，显示提示但不强制跳转
        // 让组件自行处理状态清理
        if (!currentPath.includes('/login')) {
          console.log('用户认证失败，已清除本地状态')
          // 这里不显示消息，让组件决定是否显示
        }
      }
    } 
    // 其他网络错误
    else if (error.code === 'NETWORK_ERROR' || error.code === 'ECONNABORTED') {
      if (!error.config?.silent) {
        ElMessage.error('网络连接失败，请检查网络设置')
      }
    }
    // 服务器错误
    else if (error.response?.status >= 500) {
      if (!error.config?.silent) {
        ElMessage.error('服务器错误，请稍后重试')
      }
    }
    // 其他错误
    else if (!error.config?.silent) {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

// 添加静默请求的辅助方法
request.silent = {
  get: (url, config = {}) => {
    config.silent = true
    return request.get(url, config)
  },
  post: (url, data, config = {}) => {
    config.silent = true
    return request.post(url, data, config)
  },
  put: (url, data, config = {}) => {
    config.silent = true
    return request.put(url, data, config)
  },
  delete: (url, config = {}) => {
    config.silent = true
    return request.delete(url, config)
  }
}

export default request