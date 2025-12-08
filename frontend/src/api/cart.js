import request from './request'

export const getCartList = () => {
  return request({
    url: '/cart-service/cart',
    method: 'get'
  })
}

export const addToCart = (data) => {
  return request({
    url: '/cart-service/cart',
    method: 'post',
    data
  })
}

export const removeFromCart = (id) => {
  return request({
    url: `/cart-service/cart/${id}`,
    method: 'delete'
  })
}

export const updateCartQuantity = (id, quantity) => {
  return request({
    url: `/cart-service/cart/${id}`,
    method: 'put',
    params: { quantity }
  })
}

