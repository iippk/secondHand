import request from './request'

export const createOrder = (data) => {
  return request({
    url: '/order-service/order',
    method: 'post',
    data
  })
}

export const getMyBuyOrders = () => {
  return request({
    url: '/order-service/order/my-buy',
    method: 'get'
  })
}

export const getOrderById = (id) => {
  return request({
    url: `/order-service/order/${id}`,
    method: 'get'
  })
}

export const payOrder = (id) => {
  return request({
    url: `/order-service/order/${id}/pay`,
    method: 'put'
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/order-service/order/${id}/cancel`,
    method: 'put'
  })
}

export const refundOrder = (id) => {
  return request({
    url: `/order-service/order/${id}/refund`,
    method: 'put'
  })
}

export const completeOrder = (id) => {
  return request({
    url: `/order-service/order/${id}/complete`,
    method: 'put'
  })
}

