<template>
  <div class="message-list-container">
    <!-- 左侧会话列表 -->
    <div class="session-section">
      <div class="section-header">
        <h2 class="section-title">我的消息</h2>
        <el-button 
          type="primary" 
          link 
          @click="refreshSessions"
          :loading="loading"
          class="refresh-btn"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div v-loading="loading" class="session-list">
        <div
          v-for="session in sessionList"
          :key="session.sessionId"
          class="session-item"
          :class="{ 
            active: selectedSession?.sessionId === session.sessionId,
            unread: session.unreadCount > 0
          }"
          @click="selectSession(session)"
        >
          <div class="session-avatar">
            <el-avatar :size="52" class="user-avatar">
              {{ session.otherUserName?.charAt(0) || 'U' }}
            </el-avatar>
            <div v-if="session.unreadCount > 0" class="unread-badge">
              {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
            </div>
          </div>
          
          <div class="session-content">
            <div class="session-header">
              <span class="session-name">{{ session.otherUserName }}</span>
              <span class="session-time">{{ formatTime(session.lastTime) }}</span>
            </div>
            
            <div class="session-last-message">
              <span class="message-text">{{ session.lastMessage }}</span>
            </div>
            
            <div v-if="session.productId" class="session-product">
              <div class="product-preview">
                <el-image
                  :src="getImageUrl(session.productImage)"
                  fit="cover"
                  class="product-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="product-info">
                  <div class="product-title">{{ session.productTitle }}</div>
                  <div v-if="session.productPrice" class="product-price">¥{{ session.productPrice }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty 
          v-if="!loading && sessionList.length === 0" 
          description="暂无消息会话"
          class="custom-empty"
        >
          <template #image>
            <div class="empty-image">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </template>
          <p class="empty-text">快去和卖家/买家聊聊吧！</p>
        </el-empty>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div v-if="selectedSession" class="chat-section">
      <div class="chat-header">
        <div class="chat-user-info">
          <el-avatar :size="44" class="chat-avatar">
            {{ selectedSession.otherUserName?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-details">
            <div class="user-name">{{ selectedSession.otherUserName }}</div>
            <div class="user-status">正在输入...</div>
          </div>
        </div>
        
        <div class="chat-actions">
          <el-button 
            v-if="selectedSession.productId" 
            type="primary" 
            link 
            @click="goToProduct(selectedSession.productId)"
            class="product-link-btn"
          >
            <el-icon><View /></el-icon>
            查看商品
          </el-button>
          <el-button text @click="closeChat" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
      
      <div class="chat-messages" ref="chatMessagesRef">
        <div class="messages-container">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-bubble', msg.senderId === currentUserId ? 'sent' : 'received']"
          >
            <div class="message-avatar">
              <el-avatar :size="32">
                {{ msg.senderName?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="message-content-wrapper">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatDetailedTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>
        
        <div v-if="messages.length === 0" class="no-messages">
          <el-icon class="no-messages-icon"><ChatLineRound /></el-icon>
          <p>还没有消息，开始对话吧！</p>
        </div>
      </div>
      
      <div class="chat-input-section">
        <div class="input-container">
          <el-input
            v-model="messageInput"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
            class="message-input"
            size="large"
            :maxlength="500"
            show-word-limit
          >
            <template #prefix>
              <el-button text class="emoji-btn">
                <el-icon><Sunny /></el-icon>
              </el-button>
            </template>
          </el-input>
          <el-button 
            type="primary" 
            @click="sendMessage" 
            class="send-btn"
            :disabled="!messageInput.trim()"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 无会话选择时的占位 -->
    <div v-else class="empty-chat-section">
      <div class="empty-chat-content">
        <el-icon class="empty-chat-icon"><ChatDotSquare /></el-icon>
        <h3>选择会话开始聊天</h3>
        <p>从左侧选择一个对话或开始新的对话</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getChatSessionList, getChatMessages, markChatRead } from '@/api/chat'
import { getImageFullUrl } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { 
  Close, 
  Refresh, 
  Picture, 
  View, 
  Promotion, 
  Sunny,
  ChatDotRound,
  ChatLineRound,
  ChatDotSquare
} from '@element-plus/icons-vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const emit = defineEmits(['close'])

const router = useRouter()
const loading = ref(false)
const sessionList = ref([])
const selectedSession = ref(null)
const messages = ref([])
const messageInput = ref('')
const chatMessagesRef = ref(null)
const currentUserId = localStorage.getItem('studentId')
const stompClient = ref(null)

// 用于跟踪临时消息，便于替换
const tempMessagesMap = ref(new Map())

const loadSessionList = async () => {
  loading.value = true
  try {
    sessionList.value = await getChatSessionList()
  } catch (error) {
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

const refreshSessions = async () => {
  await loadSessionList()
  ElMessage.success('刷新成功')
}

const selectSession = async (session) => {
  selectedSession.value = session
  await loadMessages(session.sessionId)
  await markChatRead(session.sessionId)
  // 重新加载会话列表以更新未读数
  await loadSessionList()
  // 连接WebSocket
  connectWebSocket()
}

const loadMessages = async (sessionId) => {
  try {
    const msgList = await getChatMessages(sessionId)
    
    // 清除所有临时消息和映射
    messages.value = msgList.filter(msg => !msg.isTemp)
    tempMessagesMap.value.clear()
    
    await nextTick(() => scrollToBottom())
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

const connectWebSocket = () => {
  if (stompClient.value?.connected) {
    console.log('WebSocket已连接，无需重复连接')
    return
  }
  
  console.log('开始连接WebSocket...')
  // 直接连接到chat-service（绕过网关）
  const wsUrl = 'http://localhost:8085/ws'
  console.log('连接WebSocket URL:', wsUrl)
  
  try {
    // 检查Stomp是否可用
    if (!Stomp) {
      console.error('Stomp未正确导入，请检查stompjs包是否正确安装')
      ElMessage.error('WebSocket客户端初始化失败，请刷新页面重试')
      return
    }
    
    // 尝试不同的导入方式
    const StompClient = Stomp.Stomp || Stomp
    if (!StompClient || typeof StompClient.over !== 'function') {
      console.error('Stomp.over方法不可用，Stomp对象:', Stomp)
      ElMessage.error('WebSocket客户端初始化失败，请刷新页面重试')
      return
    }
    
    const socket = new SockJS(wsUrl)
    const stomp = StompClient.over(socket)
    
    // 启用STOMP调试日志
    stomp.debug = (str) => {
      console.log('STOMP:', str)
    }
    
    stomp.connect({}, 
      () => {
        console.log('WebSocket连接成功！')
        stompClient.value = stomp
        const queuePath = `/queue/${currentUserId}`
        console.log('订阅消息队列:', queuePath)
        stomp.subscribe(queuePath, (message) => {
          try {
            const msg = JSON.parse(message.body)
            console.log('收到新消息:', msg)
            
            if (selectedSession.value && msg.sessionId === selectedSession.value.sessionId) {
              // 检查是否是刚刚发送的消息的回传
              const tempMessageKey = `${msg.content}_${msg.senderId}_${new Date(msg.createTime).getTime()}`
              
              if (tempMessagesMap.value.has(tempMessageKey)) {
                // 如果是回传消息，替换临时消息
                const tempMessageId = tempMessagesMap.value.get(tempMessageKey)
                const index = messages.value.findIndex(m => m.id === tempMessageId)
                
                if (index !== -1) {
                  console.log('替换临时消息为真实消息')
                  messages.value.splice(index, 1, msg)
                  // 从映射中移除
                  tempMessagesMap.value.delete(tempMessageKey)
                } else {
                  // 如果没有找到临时消息，直接添加
                  messages.value.push(msg)
                }
              } else {
                // 不是回传消息，检查是否重复
                const isDuplicate = messages.value.some(existingMsg => 
                  existingMsg.id === msg.id || 
                  (existingMsg.content === msg.content && 
                   existingMsg.senderId === msg.senderId &&
                   Math.abs(new Date(existingMsg.createTime).getTime() - new Date(msg.createTime).getTime()) < 3000)
                )
                
                if (!isDuplicate) {
                  messages.value.push(msg)
                } else {
                  console.log('检测到重复消息，跳过添加')
                }
              }
              
              nextTick(() => {
                scrollToBottom()
              })
            }
            
            // 刷新会话列表
            loadSessionList()
          } catch (error) {
            console.error('解析消息失败:', error, message.body)
          }
        })
        console.log('已成功订阅消息队列:', queuePath)
      },
      (error) => {
        console.error('WebSocket连接失败:', error)
        ElMessage.error('连接失败，请确认chat-service运行在8085端口')
        stompClient.value = null
      }
    )
  } catch (error) {
    console.error('初始化WebSocket连接时出错:', error)
    ElMessage.error('WebSocket初始化失败: ' + (error.message || '未知错误'))
    stompClient.value = null
  }
}

const sendMessage = () => {
  if (!messageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  if (!stompClient.value || !stompClient.value.connected) {
    ElMessage.warning('连接未就绪，请稍后重试')
    return
  }
  
  if (!selectedSession.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  
  // 生成唯一的临时消息ID
  const tempMessageId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const currentTime = new Date()
  
  const message = {
    sessionId: selectedSession.value.sessionId,
    senderId: currentUserId,
    senderName: localStorage.getItem('userName'),
    receiverId: selectedSession.value.otherUserId,
    receiverName: selectedSession.value.otherUserName,
    content: messageInput.value.trim(),
    type: 0,
    productId: selectedSession.value.productId,
    productTitle: selectedSession.value.productTitle,
    productImage: selectedSession.value.productImage
  }
  
  try {
    console.log('发送消息:', message)
    
    // 立即显示消息（乐观更新），但标记为临时消息
    const tempMessage = {
      ...message,
      id: tempMessageId, // 使用临时ID
      createTime: currentTime,
      isTemp: true // 标记为临时消息
    }
    
    // 添加到消息列表
    messages.value.push(tempMessage)
    
    // 记录临时消息，用于后续替换
    const tempMessageKey = `${message.content}_${currentUserId}_${currentTime.getTime()}`
    tempMessagesMap.value.set(tempMessageKey, tempMessageId)
    
    messageInput.value = ''
    nextTick(() => {
      scrollToBottom()
    })
    
    // 发送到后端
    stompClient.value.send('/app/send', {}, JSON.stringify(message))
    
    // 刷新会话列表
    loadSessionList()
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败: ' + (error.message || '未知错误'))
    
    // 发送失败，移除临时消息
    const index = messages.value.findIndex(msg => msg.id === tempMessageId)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  }
}

const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    const container = chatMessagesRef.value.querySelector('.messages-container')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const formatDetailedTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + 
           date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  return getImageFullUrl(imagePath)
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
  emit('close')
}

const closeChat = () => {
  selectedSession.value = null
  messages.value = []
  // 清除临时消息映射
  tempMessagesMap.value.clear()
}

onMounted(() => {
  loadSessionList()
  // 每30秒刷新一次会话列表
  setInterval(loadSessionList, 30000)
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.disconnect()
  }
})


</script>

<style scoped>
.message-list-container {
  display: flex;
  height: calc(100vh - 80px);
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin: 8px;
}

/* 左侧会话列表样式 */
.session-section {
  width: 250px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
  background: linear-gradient(135deg, #ff9800 0%, #e65100 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.refresh-btn {
  color: #ff9800;
  font-weight: 500;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.session-item {
  display: flex;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
  background: white;
  margin: 0 8px;
  border-radius: 12px;
  margin-bottom: 8px;
}

.session-item:hover {
  background: #fff8e1;
  transform: translateX(4px);
}

.session-item.active {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  border-left: 4px solid #ff9800;
  box-shadow: 0 2px 12px rgba(255, 152, 0, 0.1);
}

.session-item.unread {
  background: #fff3e0;
}

.session-avatar {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.user-avatar {
  border: 2px solid #ffe0b2;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  font-weight: 600;
  color: white;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: linear-gradient(135deg, #ff5722 0%, #e65100 100%);
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  text-align: center;
  border: 2px solid white;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.session-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 8px;
}

.session-last-message {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-product {
  margin-top: 8px;
}

.product-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.product-image {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  flex-shrink: 0;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #ccc;
  border-radius: 6px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-title {
  font-size: 12px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.product-price {
  font-size: 12px;
  color: #e65100;
  font-weight: 600;
}

/* 右侧聊天区域样式 */
.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.empty-chat-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.empty-chat-content {
  text-align: center;
  color: #999;
}

.empty-chat-icon {
  font-size: 64px;
  color: #ffe0b2;
  margin-bottom: 16px;
}

.empty-chat-content h3 {
  color: #666;
  margin-bottom: 8px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-avatar {
  border: 2px solid #ffc107;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  font-weight: 600;
  color: white;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
  margin-bottom: 2px;
}

.user-status {
  font-size: 12px;
  color: #4caf50;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-link-btn {
  color: #ff9800;
  font-weight: 500;
}

.close-btn {
  color: #999;
}

.close-btn:hover {
  color: #666;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: #f8f9fa;
  position: relative;
}

.messages-container {
  padding: 20px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.message-bubble {
  display: flex;
  margin-bottom: 16px;
  max-width: 70%;
}

.message-bubble.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-bubble.received {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 8px;
}

.message-bubble.sent .message-avatar {
  margin-left: 12px;
  margin-right: 0;
}

.message-bubble.received .message-avatar {
  margin-right: 12px;
  margin-left: 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
}

.message-bubble.sent .message-content-wrapper {
  align-items: flex-end;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  word-wrap: break-word;
  position: relative;
}

.message-bubble.sent .message-content {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  color: white;
  border-bottom-right-radius: 6px;
}

.message-bubble.received .message-content {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-bottom-left-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  padding: 0 4px;
}

.no-messages {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #ccc;
}

.no-messages-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.no-messages p {
  margin: 0;
  font-size: 14px;
}

/* 输入区域样式 */
.chat-input-section {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

:deep(.message-input .el-input__wrapper) {
  border-radius: 20px;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  background: #f8f9fa;
  box-shadow: none;
}

:deep(.message-input .el-input__wrapper:hover) {
  border-color: #ffc107;
  background: white;
}

:deep(.message-input .el-input__wrapper.is-focus) {
  border-color: #ff9800;
  background: white;
  box-shadow: 0 0 0 2px rgba(255, 152, 0, 0.1);
}

.emoji-btn {
  color: #ff9800;
  padding: 4px;
}

.send-btn {
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border: none;
  border-radius: 20px;
  padding: 12px 20px;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.send-btn:disabled {
  background: #ccc;
  box-shadow: none;
  transform: none;
  cursor: not-allowed;
}

/* 空状态样式 */
.custom-empty {
  padding: 60px 20px;
}

.empty-image {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #fff3cc 0%, #ffe699 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.empty-image .el-icon {
  font-size: 32px;
  color: #ff9800;
}

.empty-text {
  color: #999;
  font-size: 14px;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-list-container {
    flex-direction: column;
    height: calc(100vh - 60px);
  }
  
  .session-section {
    width: 100%;
    height: 40%;
  }
  
  .chat-section, .empty-chat-section {
    height: 60%;
  }
  
  .section-header {
    padding: 16px 20px;
  }
  
  .session-item {
    padding: 12px 16px;
  }
  
  .message-bubble {
    max-width: 85%;
  }
}
</style>