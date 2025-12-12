import request from './request'

export const getChatSessions = () => {
  return request({
    url: '/chat-service/api/chat/sessions',
    method: 'get'
  })
}

export const getChatMessages = (sessionId) => {
  return request({
    url: '/chat-service/api/chat/messages',
    method: 'get',
    params: { sessionId }
  })
}

export const markChatRead = (sessionId) => {
  return request({
    url: `/chat-service/api/chat/read/${sessionId}`,
    method: 'put'
  })
}


