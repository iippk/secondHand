import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const studentId = ref(localStorage.getItem('studentId') || '')
  
  // 从JWT token中解析用户信息，与后端JwtUtil处理逻辑保持一致
  function parseToken(tokenString) {
    try {
      if (!tokenString) return null
      
      // 处理可能带有Bearer前缀的token
      const cleanToken = tokenString.startsWith('Bearer ') ? tokenString.substring(7) : tokenString
      
      // JWT格式: header.payload.signature
      const parts = cleanToken.split('.')
      if (parts.length !== 3) {
        console.error('无效的token格式，不是标准的JWT格式')
        return null
      }
      
      const payload = parts[1]
      // Base64解码 - 添加padding处理
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
      const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=')
      const decoded = decodeURIComponent(atob(padded).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      }).join(''))
      return JSON.parse(decoded)
    } catch (e) {
      console.error('解析token失败:', e)
      return null
    }
  }
  
  // 获取studentId（与后端JwtUtil.getStudentIdFromToken保持一致的逻辑）
  function getStudentIdFromToken(tokenString) {
    try {
      const tokenData = parseToken(tokenString)
      if (!tokenData) return null
      
      // 优先从claims中获取studentId字段
      if (tokenData.studentId) {
        return tokenData.studentId
      }
      // 其次尝试从subject获取
      if (tokenData.sub) {
        return tokenData.sub
      }
      // 最后尝试其他可能的字段
      return tokenData.username || tokenData.userId || null
    } catch (e) {
      console.error('从token获取studentId失败:', e)
      return null
    }
  }

  const loginUser = async (loginData) => {
    try {
      console.log('开始登录，登录数据:', loginData)
      const result = await login(loginData)
      console.log('登录成功，获取到token:', !!result)
      
      // 保存token到localStorage和状态
      token.value = result
      localStorage.setItem('token', result)
      
      // 解析并保存studentId（使用与后端一致的逻辑）
      const parsedStudentId = getStudentIdFromToken(result)
      if (parsedStudentId) {
        studentId.value = parsedStudentId
        localStorage.setItem('studentId', parsedStudentId)
        console.log('从token解析并保存studentId:', parsedStudentId)
      } else if (loginData.studentId) {
        // 如果token中没有，尝试从登录数据获取
        studentId.value = loginData.studentId
        localStorage.setItem('studentId', loginData.studentId)
        console.log('从登录数据获取studentId:', loginData.studentId)
      }
      
      // 直接获取用户信息
      console.log('准备获取用户信息...')
      await fetchUserInfo()
      return result
    } catch (error) {
      console.error('登录过程失败:', error)
      console.error('错误详情:', error.response?.data || error.message)
      // 清除状态
      logout()
      throw error
    }
  }

  const fetchUserInfo = async () => {
    try {
      // 检查是否有token，没有则不尝试获取用户信息
      if (!token.value) {
        console.warn('未找到token，无法获取用户信息')
        userInfo.value = null
        return null
      }
      
      console.log('开始获取用户信息...')
      const info = await getUserInfo()
      userInfo.value = info
      
      // 更新本地存储的用户信息
      if (info) {
        if (info.studentId) {
          studentId.value = info.studentId
          localStorage.setItem('studentId', info.studentId)
        }
        localStorage.setItem('userName', info.name || info.username || '')
        console.log('获取用户信息成功:', info)
      }
      return info
    } catch (error) {
      console.error('获取用户信息失败', error)
      console.error('错误详情:', error.response?.data || error.message)
      
      // 如果是401错误，清除token和登录状态
      if (error.response?.status === 401) {
        console.warn('用户信息获取失败，token可能已过期或无效')
        logout()
      }
      
      // 设置为null表示未登录状态
      userInfo.value = null
      return null
    }
  }

  const logout = () => {
    console.log('用户登出，清除所有认证信息')
    userInfo.value = null
    token.value = ''
    studentId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('studentId')
    localStorage.removeItem('userName')
    
    // 可选：清除其他可能的用户相关信息
    localStorage.removeItem('userInfo')
  }
  
  // 检查是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }
  
  // 检查token是否有效（简单检查是否存在和格式，实际验证由后端完成）
  const isTokenValid = () => {
    if (!token.value) return false
    
    try {
      const tokenData = parseToken(token.value)
      if (!tokenData || !tokenData.exp) return false
      
      // 检查token是否过期
      const expTime = tokenData.exp * 1000 // 转换为毫秒
      return expTime > Date.now()
    } catch (e) {
      console.error('验证token有效性失败:', e)
      return false
    }
  }

  return {
    userInfo,
    token,
    studentId,
    loginUser,
    fetchUserInfo,
    logout,
    isLoggedIn,
    isTokenValid,
    logout
  }
})

