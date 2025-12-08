import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
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
      console.error('解析token失败:', e)
      return null
    }
  }

  const loginUser = async (loginData) => {
    try {
      console.log('开始登录，登录数据:', loginData)
      const result = await login(loginData)
      console.log('登录成功，获取到token:', !!result)
      
      // 保存token
      token.value = result
      localStorage.setItem('token', result)
      
      // 简化studentId处理，不再进行多次重复保存
      const tokenData = parseToken(result)
      if (tokenData) {
        const studentId = tokenData.studentId || tokenData.sub || tokenData.username || tokenData.userId
        if (studentId) {
          localStorage.setItem('studentId', studentId)
          console.log('从token解析并保存studentId:', studentId)
        }
      }
      
      // 如果token中没有，尝试从登录数据获取
      if (loginData.studentId) {
        localStorage.setItem('studentId', loginData.studentId)
      }
      
      // 直接获取用户信息，移除不必要的延迟
      console.log('准备获取用户信息...')
      await fetchUserInfo()
      return result
    } catch (error) {
      console.error('登录过程失败:', error)
      console.error('错误详情:', error.response?.data || error.message)
      // 清除状态
      token.value = ''
      localStorage.removeItem('token')
      localStorage.removeItem('studentId')
      throw error
    }
  }

  const fetchUserInfo = async () => {
    try {
      // 简化处理，直接调用getUserInfo接口
      console.log('开始获取用户信息...')
      const info = await getUserInfo()
      userInfo.value = info
      
      // 保存必要的用户信息
      if (info) {
        localStorage.setItem('studentId', info.studentId || '')
        localStorage.setItem('userName', info.name || '')
        console.log('获取用户信息成功:', info)
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
      console.error('错误详情:', error.response?.data || error.message)
      // 设置为null表示未登录状态
      userInfo.value = null
    }
  }

  const logout = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('studentId')
    localStorage.removeItem('userName')
  }

  return {
    userInfo,
    token,
    loginUser,
    fetchUserInfo,
    logout
  }
})

