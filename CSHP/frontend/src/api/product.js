import request from './request'

export const getProductList = () => {
  console.log('调用getProductList，将由请求拦截器自动添加认证信息')
  return request({
    url: '/product-service/product/list',
    method: 'get'
  })
}

export const getProductById = (id) => {
  console.log(`调用getProductById，ID: ${id}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/${id}`,
    method: 'get'
  })
}

export const createProduct = (data) => {
  console.log('调用createProduct，将由请求拦截器自动添加认证信息')
  return request({
    url: '/product-service/product',
    method: 'post',
    data
  })
}

export const updateProduct = (id, data) => {
  console.log(`调用updateProduct，ID: ${id}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/${id}`,
    method: 'put',
    data
  })
}

export const deleteProduct = (id) => {
  console.log(`调用deleteProduct，ID: ${id}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/${id}`,
    method: 'delete'
  })
}

export const getMySellProducts = () => {
  console.log('调用getMySellProducts，将由请求拦截器自动添加认证信息')
  return request({
    url: '/product-service/product/my-sell',
    method: 'get'
  })
}

export const updateProductStatus = (id, status) => {
  console.log(`调用updateProductStatus，ID: ${id}，Status: ${status}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const updateProductShipped = (id, shipped) => {
  console.log(`调用updateProductShipped，ID: ${id}，Shipped: ${shipped}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/${id}/shipped`,
    method: 'put',
    params: { shipped }
  })
}

export const searchProducts = (keyword) => {
  console.log(`调用searchProducts，Keyword: ${keyword}，将由请求拦截器自动添加认证信息`)
  return request({
    url: '/product-service/product/search',
    method: 'get',
    params: { keyword }
  })
}

export const getProductsByCategory = (category) => {
  console.log(`调用getProductsByCategory，Category: ${category}，将由请求拦截器自动添加认证信息`)
  return request({
    url: `/product-service/product/category/${category}`,
    method: 'get'
  })
}

