import request from './request'
import axios from 'axios'

const uploadRequest = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true
})

// 上传请求拦截器
uploadRequest.interceptors.request.use(
  config => {
    config.headers = config.headers || {}
    const token = localStorage.getItem('token')
    if (token) {
      const normalizedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
      config.headers['Authorization'] = normalizedToken
    }
    const studentId = localStorage.getItem('studentId')
    if (studentId) {
      config.headers['X-Student-Id'] = studentId
      config.headers['X-User-Id'] = studentId
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 上传响应拦截器
uploadRequest.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      return Promise.reject(new Error(res.message || '上传失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 上传单个文件
 * @param {File} file 文件对象
 * @returns {Promise<string>} 返回文件存储路径
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return uploadRequest({
    url: '/product-service/product/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(data => {
    // 返回storedPath用于数据库存储
    return data
  })
}

/**
 * 批量上传文件
 * @param {File[]} files 文件数组
 * @returns {Promise<string[]>} 返回文件存储路径数组
 */
export const uploadFiles = (files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return uploadRequest({
    url: '/product-service/product/file/upload/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(data => {
    // 从响应中提取storedPath数组用于数据库存储
    if (data && data.success) {
      return data.success
    }
    return []
  })
}

/**
 * 删除文件
 * @param {string} storedPath 文件存储路径
 * @returns {Promise<void>}
 */
export const deleteFile = (storedPath) => {
  return uploadRequest({
    url: '/product-service/product/file/delete',
    method: 'delete',
    params: { filePath: storedPath }
  })
}

/**
 * 获取图片完整URL
 * @param {string} storedPath 存储路径
 * @returns {string} 完整的图片访问URL
 */
export const getImageFullUrl = (storedPath) => {
  if (!storedPath) return ''
  // 移除首尾空格
  const trimmedPath = storedPath.trim()
  // 已经是完整URL则直接返回
  if (trimmedPath.startsWith('http://') || trimmedPath.startsWith('https://')) {
    return trimmedPath
  }
  // 如果已经包含/uploads前缀，直接返回
  if (trimmedPath.startsWith('/uploads/')) {
    return trimmedPath
  }
  // 如果以uploads/开头，添加斜杠前缀
  if (trimmedPath.startsWith('uploads/')) {
    return `/${trimmedPath}`
  }
  // 添加/uploads前缀
  return `/uploads/${trimmedPath}`
}

