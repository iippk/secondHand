import request from './request'

export const login = (data) => {
  return request({
    url: '/user-service/api/user/login',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  // 确保使用标准的用户信息接口路径
  // 请求拦截器会自动添加Authorization和X-Student-Id等请求头
  console.log('调用getUserInfo，将由请求拦截器处理认证信息')
  return request({
    url: '/user-service/api/user/info',
    method: 'get'
  })
}

export const updateUserInfo = (data) => {
  return request({
    url: '/user-service/api/user/info',
    method: 'put',
    data
  })
}

export const changePassword = (data) => {
  return request({
    url: '/user-service/api/user/change-password',
    method: 'post',
    data
  })
}

export const register = (data) => {
  return request({
    url: '/user-service/api/user/register',
    method: 'post',
    data
  })
}

