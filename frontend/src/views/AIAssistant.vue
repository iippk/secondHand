<template>
  <div class="ai-assistant">
    <!-- AI助手浮动按钮 -->
    <div 
      class="ai-assistant-btn"
      @click="toggleChatWindow"
      :class="{ 'pulse': hasNewMessage }"
    >
      <img :src="aiSvg" alt="AI助手" class="ai-icon" />
      <div v-if="hasNewMessage" class="notification-dot"></div>
    </div>

    <!-- 聊天窗口 -->
    <div v-if="showChatWindow" class="chat-window">
      <div class="chat-header">
        <div class="header-left">
          <img :src="aiSvg" alt="AI助手" class="header-icon" />
          <h3>校园二手交易AI助手</h3>
        </div>
        <button @click="toggleChatWindow" class="close-btn">×</button>
      </div>
      
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message', message.role]"
        >
          <div class="message-avatar">
            <img v-if="message.role === 'ai'" :src="aiSvg" alt="AI" />
            <div v-else class="user-avatar">我</div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-time">
              {{ formatTime(message.timestamp) }}
            </div>
          </div>
        </div>
        
        <div v-if="loading" class="message ai">
          <div class="message-avatar">
            <img :src="aiSvg" alt="AI" />
          </div>
          <div class="message-content-wrapper">
            <div class="message-content typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-input-area">
        <div class="chat-input">
          <textarea 
            v-model="inputMessage"
            @keydown="handleKeydown"
            placeholder="输入您的问题，例如：如何评估二手手机价格？"
            rows="1"
            ref="textareaRef"
          ></textarea>
          <button 
            @click="sendMessage" 
            :disabled="loading || !inputMessage.trim()"
            class="send-btn"
          >
            <span v-if="!loading">发送</span>
            <span v-else class="loading">...</span>
          </button>
        </div>
        
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <button @click="quickAction('如何评估物品价格？')" class="quick-btn">
            💰 价格评估
          </button>
          <button @click="quickAction('交易注意事项')" class="quick-btn">
            🔒 交易安全
          </button>
          <button @click="quickAction('平台使用指南')" class="quick-btn">
            📚 使用帮助
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// AI助手SVG图标
import aiSvg from '@/assets/ai-assistant.svg'

const userStore = useUserStore()

const showChatWindow = ref(false)
const inputMessage = ref('')
const messages = ref([])
const loading = ref(false)
const hasNewMessage = ref(false)
const messagesContainer = ref(null)
const textareaRef = ref(null)
const conversationId = ref(null)

// 初始化欢迎消息
onMounted(() => {
  messages.value.push({
    role: 'ai',
    content: '您好！我是校园二手交易AI助手，可以帮您评估物品价格、提供交易建议、解答平台使用问题等。请问有什么可以帮您的？',
    timestamp: new Date()
  })
})

// 点击外部关闭聊天窗口
const handleClickOutside = (event) => {
  const aiAssistant = document.querySelector('.ai-assistant')
  if (aiAssistant && !aiAssistant.contains(event.target)) {
    showChatWindow.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const toggleChatWindow = (event) => {
  event.stopPropagation()
  showChatWindow.value = !showChatWindow.value
  if (showChatWindow.value) {
    hasNewMessage.value = false
    nextTick(() => {
      scrollToBottom()
      textareaRef.value?.focus()
    })
  }
}

const handleKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  // 检查用户是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录后再使用AI助手')
    return
  }
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage,
    timestamp: new Date()
  })
  
  loading.value = true
  scrollToBottom()
  
  try {
    const response = await fetch('/api/ai-service2/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: JSON.stringify({
        message: userMessage,
        conversationId: conversationId.value
      })
    })
    
    if (!response.ok) {
      if (response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        userStore.logout()
        return
      }
      throw new Error(`请求失败: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data.success) {
      messages.value.push({
        role: 'ai',
        content: data.response,
        timestamp: new Date()
      })
      conversationId.value = data.conversationId
      
      if (!showChatWindow.value) {
        hasNewMessage.value = true
      }
    } else {
      throw new Error(data.message || 'AI服务返回错误')
    }
  } catch (error) {
    console.error('AI聊天错误:', error)
    ElMessage.error('AI助手暂时不可用，请稍后重试')
    
    // 添加错误提示消息
    messages.value.push({
      role: 'ai',
      content: '抱歉，我暂时无法响应您的请求。请检查网络连接或稍后重试。',
      timestamp: new Date()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const quickAction = (action) => {
  inputMessage.value = action
  sendMessage()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

.ai-assistant-btn {
  position: relative;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  border: none;
}

.ai-assistant-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.ai-assistant-btn.pulse {
  animation: pulse 1.5s infinite;
}

.notification-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #ff4757;
  border-radius: 50%;
  border: 2px solid white;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(102, 126, 234, 0); }
  100% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0); }
}

.ai-icon {
  width: 30px;
  height: 30px;
  filter: brightness(0) invert(1);
}

.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e1e5e9;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  width: 20px;
  height: 20px;
  filter: brightness(0) invert(1);
}

.chat-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.message.ai .message-avatar {
  background: #667eea;
}

.message.ai .message-avatar img {
  width: 18px;
  height: 18px;
  filter: brightness(0) invert(1);
}

.message.user .message-avatar {
  background: #10b981;
  color: white;
}

.message-content-wrapper {
  max-width: calc(100% - 40px);
}

.message-content {
  padding: 10px 14px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.4;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 5px;
}

.message.ai .message-content {
  background: white;
  color: #333;
  border: 1px solid #e1e5e9;
  border-bottom-left-radius: 5px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  padding: 0 4px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #999;
  margin: 0 1px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input-area {
  border-top: 1px solid #e1e5e9;
  background: white;
}

.chat-input {
  padding: 12px 16px;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.chat-input textarea {
  flex: 1;
  border: 1px solid #e1e5e9;
  border-radius: 20px;
  padding: 10px 16px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
  max-height: 100px;
  line-height: 1.4;
}

.chat-input textarea:focus {
  border-color: #667eea;
}

.send-btn {
  background: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
  min-width: 60px;
  height: 40px;
}

.send-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.send-btn .loading {
  animation: blink 1.4s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.quick-actions {
  padding: 8px 16px 12px;
  background: white;
  border-top: 1px solid #f1f3f4;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.quick-btn {
  background: #f8f9fa;
  border: 1px solid #e1e5e9;
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-assistant {
    bottom: 16px;
    right: 16px;
  }
  
  .chat-window {
    width: calc(100vw - 32px);
    height: 70vh;
    right: 0;
    left: 0;
    margin: 0 auto;
  }
  
  .ai-assistant-btn {
    width: 56px;
    height: 56px;
  }
  
  .ai-icon {
    width: 28px;
    height: 28px;
  }
}
</style>