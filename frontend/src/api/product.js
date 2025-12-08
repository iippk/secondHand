import request from './request'

export const getProductList = () => {
  return request({
    url: '/product-service/product/list',
    method: 'get'
  })
}

export const getProductById = (id) => {
  return request({
    url: `/product-service/product/${id}`,
    method: 'get'
  })
}

export const createProduct = (data) => {
  return request({
    url: '/product-service/product',
    method: 'post',
    data
  })
}

export const updateProduct = (id, data) => {
  return request({
    url: `/product-service/product/${id}`,
    method: 'put',
    data
  })
}

export const deleteProduct = (id) => {
  return request({
    url: `/product-service/product/${id}`,
    method: 'delete'
  })
}

export const getMySellProducts = () => {
  return request({
    url: '/product-service/product/my-sell',
    method: 'get'
  })
}

export const updateProductStatus = (id, status) => {
  return request({
    url: `/product-service/product/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const updateProductShipped = (id, shipped) => {
  return request({
    url: `/product-service/product/${id}/shipped`,
    method: 'put',
    params: { shipped }
  })
}

export const searchProducts = (keyword) => {
  return request({
    url: '/product-service/product/search',
    method: 'get',
    params: { keyword }
  })
}

export const getProductsByCategory = (category) => {
  return request({
    url: `/product-service/product/category/${category}`,
    method: 'get'
  })
}

