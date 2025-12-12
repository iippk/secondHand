<template>
  <div class="product-detail-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-image
          :src="product.images?.split(',')[0] || 'https://via.placeholder.com/400'"
          fit="contain"
          style="width: 100%; height: 400px; border-radius: 8px;"
        />
      </el-col>
      <el-col :span="12">
        <div class="product-info">
          <h1>{{ product.title }}</h1>
          <div class="price">¥{{ product.price }}</div>
          <el-divider />
          <div class="info-item">
            <span class="label">卖家：</span>
            <span>{{ product.sellerName }}</span>
          </div>
          <div class="info-item">
            <span class="label">分类：</span>
            <span>{{ product.category }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="getStatusType(product.status)">
              {{ getStatusText(product.status) }}
            </el-tag>
          </div>
          <el-divider />
          <div class="description">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>
          <div class="actions">
            <el-button
              type="primary"
              size="large"
              @click="addToCart"
              :disabled="product.status !== 0"
            >
              加入购物车
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="openChat"
            >
              联系卖家
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 聊天对话框 -->
    <el-dialog
      v-model="chatVisible"
      title="与卖家沟通"
      width="600px"
    >
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message', msg.senderId === currentUserId ? 'sent' : 'received']"
          >
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="messageInput"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { getChatMessages, markChatRead } from '@/api/chat'
import { ElMessage } from 'element-plus'
import SockJS from 'sockjs-client'
import { Stomp } from 'stompjs'

const route = useRoute()
const router = useRouter()
const product = ref({})
const loading = ref(false)
const chatVisible = ref(false)
const messageInput = ref('')
const messages = ref([])
const chatMessagesRef = ref(null)
const currentUserId = localStorage.getItem('studentId')
const stompClient = ref(null)
const sessionId = computed(() => {
  if (!product.value?.sellerId || !currentUserId) return ''
  const sortedIds = [currentUserId, product.value.sellerId].sort()
  return `${sortedIds[0]}_${sortedIds[1]}`
})

const getStatusText = (status) => {
  const statusMap = {
    0: '待售',
    1: '已售出',
    2: '已下架'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'success',
    1: 'info',
    2: 'warning'
  }
  return typeMap[status] || ''
}

const loadProduct = async () => {
  loading.value = true
  try {
    product.value = await getProductById(route.params.id)
  } catch (error) {
    ElMessage.error('加载商品失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  try {
    await addToCartApi({
      productId: product.value.id,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const openChat = () => {
  chatVisible.value = true
  connectWebSocket()
  loadMessages()
}

const connectWebSocket = () => {
  if (stompClient.value?.connected) {
    return
  }
  const socket = new SockJS('http://localhost:8080/chat-service/ws')
  const stomp = Stomp.over(socket)
  
  stomp.connect({}, () => {
    stompClient.value = stomp
    stomp.subscribe(`/queue/${currentUserId}`, (message) => {
      const msg = JSON.parse(message.body)
      if (sessionId.value && msg.sessionId !== sessionId.value) {
        return
      }
      messages.value.push(msg)
      nextTick(() => {
        scrollToBottom()
      })
    })
  })
}

const sendMessage = () => {
  if (!messageInput.value.trim() || !stompClient.value) return
  
  const message = {
    sessionId: sessionId.value,
    senderId: currentUserId,
    senderName: localStorage.getItem('userName'),
    receiverId: product.value.sellerId,
    receiverName: product.value.sellerName,
    content: messageInput.value,
    type: 0
  }
  
  stompClient.value.send('/app/send', {}, JSON.stringify(message))
  messages.value.push({
    ...message,
    createTime: new Date()
  })
  messageInput.value = ''
  nextTick(() => {
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString()
}

const loadMessages = async () => {
  if (!sessionId.value) return
  try {
    const history = await getChatMessages(sessionId.value)
    messages.value = history || []
    await nextTick(() => scrollToBottom())
    await markChatRead(sessionId.value)
  } catch (error) {
    console.error('加载聊天记录失败', error)
  }
}

onMounted(() => {
  loadProduct()
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.disconnect()
  }
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.product-info h1 {
  font-size: 24px;
  margin-bottom: 16px;
}

.price {
  font-size: 32px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 16px;
}

.info-item {
  margin-bottom: 12px;
  font-size: 14px;
}

.label {
  color: #909399;
  margin-right: 8px;
}

.description {
  margin-top: 20px;
}

.description h3 {
  margin-bottom: 12px;
}

.description p {
  color: #606266;
  line-height: 1.6;
}

.actions {
  margin-top: 30px;
  display: flex;
  gap: 12px;
}

.chat-container {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

.message {
  margin-bottom: 16px;
}

.message.sent {
  text-align: right;
}

.message.received {
  text-align: left;
}

.message-content {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 8px;
  max-width: 70%;
  word-wrap: break-word;
}

.message.sent .message-content {
  background: #409eff;
  color: #fff;
}

.message.received .message-content {
  background: #fff;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-input {
  display: flex;
  gap: 8px;
}
</style>

