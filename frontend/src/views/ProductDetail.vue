<template>
  <div class="product-detail-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="product-images">
          <!-- 轮播图 -->
          <el-carousel
            v-if="imageList.length > 0"
            v-model="currentImageIndex"
            :interval="0"
            indicator-position="outside"
            height="400px"
            arrow="hover"
          >
            <el-carousel-item v-for="(image, index) in imageList" :key="index">
              <el-image
                :src="image"
                :fallback="'https://via.placeholder.com/400'"
                fit="contain"
                style="width: 100%; height: 100%; border-radius: 8px; cursor: pointer;"
                :preview-src-list="imageList"
                :initial-index="index"
                preview-teleported
                @click="handleImageClick"
              />
            </el-carousel-item>
          </el-carousel>
          <!-- 单张图片或无图片 -->
          <el-image
            v-else
            :src="'https://via.placeholder.com/400'"
            fit="contain"
            style="width: 100%; height: 400px; border-radius: 8px;"
          />
          <!-- 缩略图导航（多张图片时显示） -->
          <div v-if="imageList.length > 1" class="thumbnail-list">
            <div
              v-for="(image, index) in imageList"
              :key="index"
              class="thumbnail-item"
              :class="{ active: currentImageIndex === index }"
              @click="currentImageIndex = index"
            >
              <el-image
                :src="image"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px; cursor: pointer;"
                :fallback="'https://via.placeholder.com/80'"
              />
            </div>
          </div>
        </div>
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
          <!-- 新增商品成色显示 -->
          <div class="info-item">
            <span class="label">商品成色：</span>
            <el-tag :type="getConditionType(product.condition)">
              {{ product.condition }}
            </el-tag>
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
              :disabled="product.status !== 0 || isOwner"
            >
              加入购物车
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="openChat"
              :disabled="isOwner"
              v-if="!isOwner"
            >
              联系卖家
            </el-button>
            <el-button
              type="info"
              size="large"
              disabled
              v-else
            >
              这是您自己的商品
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
      @close="handleChatClose"
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
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { getChatMessages, markChatRead } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { getImageFullUrl } from '@/api/upload'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

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
const currentImageIndex = ref(0)
const isConnecting = ref(false)

// 用于跟踪临时消息，便于替换
const tempMessagesMap = ref(new Map())

// 计算属性：判断当前用户是否是商品所有者
const isOwner = computed(() => {
  return currentUserId && product.value?.sellerId && currentUserId === product.value.sellerId
})

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

// 新增：获取商品成色的标签类型
const getConditionType = (condition) => {
  const conditionTypeMap = {
    '全新': 'success',
    '几乎全新': '',
    '良好': 'info',
    '一般': 'warning',
    '较差': 'danger'
  }
  return conditionTypeMap[condition] || ''
}

// 获取所有图片URL列表
const imageList = computed(() => {
  if (!product.value?.images || !product.value.images.trim()) {
    return []
  }
  
  // 按逗号分割图片字符串
  const imagePaths = product.value.images.split(',').map(path => path.trim()).filter(path => path)
  
  // 处理每张图片的URL
  return imagePaths.map(path => {
    // 如果是blob URL，直接返回（虽然这是临时URL，但为了兼容旧数据）
    if (path.startsWith('blob:')) {
      return path
    }
    // 否则使用getImageFullUrl处理
    return getImageFullUrl(path)
  }).filter(url => url) // 过滤掉空URL
})

// 监听对话框打开/关闭
watch(chatVisible, (newVal) => {
  if (newVal) {
    // 对话框打开时，确保连接WebSocket
    connectWebSocket()
  } else {
    // 对话框关闭时，可以断开连接或保持连接
    // 这里选择保持连接以便接收消息
  }
})

const loadProduct = async () => {
  loading.value = true
  try {
    product.value = await getProductById(route.params.id)
    console.log('商品信息:', product.value)
    console.log('当前用户ID:', currentUserId)
    console.log('卖家ID:', product.value.sellerId)
    console.log('是否是卖家:', isOwner.value)
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
  // 如果是商品所有者，不允许联系自己
  if (isOwner.value) {
    ElMessage.warning('这是您自己的商品，无法联系自己')
    return
  }
  
  chatVisible.value = true
  
  if (!sessionId.value) {
    ElMessage.error('无法建立会话，请刷新页面重试')
    return
  }
  
  console.log('打开聊天窗口，sessionId:', sessionId.value)
  console.log('当前用户ID:', currentUserId)
  console.log('卖家ID:', product.value?.sellerId)
  
  // 先加载历史消息
  loadMessages()
}

const connectWebSocket = () => {
  if (isConnecting.value) {
    console.log('WebSocket连接正在进行中...')
    return
  }
  
  if (stompClient.value?.connected) {
    console.log('WebSocket已连接，无需重复连接')
    return
  }
  
  // 如果已有客户端但未连接，先断开
  if (stompClient.value) {
    try {
      stompClient.value.disconnect()
    } catch (e) {
      console.warn('断开旧连接时出错:', e)
    }
    stompClient.value = null
  }
  
  isConnecting.value = true
  
  // 直接连接到chat-service
  const wsUrl = 'http://localhost:8085/ws'
  console.log('开始连接WebSocket，URL:', wsUrl)
  
  try {
    // 检查Stomp是否可用
    if (!Stomp) {
      console.error('Stomp未正确导入，请检查stompjs包是否正确安装')
      ElMessage.error('WebSocket客户端初始化失败，请刷新页面重试')
      isConnecting.value = false
      return
    }
    
    // 尝试不同的导入方式
    const StompClient = Stomp.Stomp || Stomp
    if (!StompClient || typeof StompClient.over !== 'function') {
      console.error('Stomp.over方法不可用，Stomp对象:', Stomp)
      ElMessage.error('WebSocket客户端初始化失败，请刷新页面重试')
      isConnecting.value = false
      return
    }
    
    const socket = new SockJS(wsUrl)
    const stomp = StompClient.over(socket)
    
    // 启用STOMP调试日志（用于调试）
    stomp.debug = (str) => {
      console.log('STOMP:', str)
    }
    
    // 设置连接超时
    const connectTimeout = setTimeout(() => {
      if (!stompClient.value?.connected) {
        console.error('WebSocket连接超时')
        ElMessage.error('连接超时，请检查网络或刷新页面')
        isConnecting.value = false
        stompClient.value = null
      }
    }, 10000) // 10秒超时
    
    stomp.connect({}, 
      () => {
        // 连接成功回调
        clearTimeout(connectTimeout)
        isConnecting.value = false
        console.log('WebSocket连接成功！')
        stompClient.value = stomp
        
        // 订阅消息队列
        const subscription = stomp.subscribe(`/queue/${currentUserId}`, (message) => {
          try {
            const msg = JSON.parse(message.body)
            console.log('收到新消息:', msg)
            
            if (sessionId.value && msg.sessionId !== sessionId.value) {
              console.log('消息sessionId不匹配，忽略')
              return
            }
            
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
            
          } catch (error) {
            console.error('解析消息失败:', error, message.body)
          }
        })
        console.log('已订阅消息队列: /queue/' + currentUserId)
        ElMessage.success('连接成功')
      },
      (error) => {
        // 连接失败回调
        clearTimeout(connectTimeout)
        isConnecting.value = false
        console.error('WebSocket连接失败:', error)
        console.error('错误详情:', JSON.stringify(error, null, 2))
        ElMessage.error('连接失败: ' + (error.headers?.message || error.message || '未知错误'))
        stompClient.value = null
      }
    )
  } catch (error) {
    console.error('初始化WebSocket连接时出错:', error)
    ElMessage.error('WebSocket初始化失败: ' + (error.message || '未知错误'))
    isConnecting.value = false
    stompClient.value = null
  }
}

const sendMessage = () => {
  // 检查输入
  if (!messageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  // 检查WebSocket连接状态
  if (!stompClient.value || !stompClient.value.connected) {
    ElMessage.warning('连接未就绪，正在尝试重新连接...')
    connectWebSocket()
    // 等待2秒后重试发送
    setTimeout(() => {
      if (stompClient.value?.connected) {
        sendMessage() // 递归调用
      } else {
        ElMessage.error('连接失败，请稍后重试发送')
      }
    }, 2000)
    return
  }
  
  // 检查必要数据
  if (!product.value?.sellerId || !currentUserId || !sessionId.value) {
    ElMessage.error('会话信息不完整')
    return
  }
  
  // 获取第一张商品图片
  const productImage = imageList.value.length > 0 ? imageList.value[0] : ''
  
  // 生成唯一的临时消息ID
  const tempMessageId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const currentTime = new Date()
  
  const message = {
    sessionId: sessionId.value,
    senderId: currentUserId,
    senderName: localStorage.getItem('userName') || '用户',
    receiverId: product.value.sellerId,
    receiverName: product.value.sellerName || '卖家',
    content: messageInput.value.trim(),
    type: 0,
    productId: product.value.id,
    productTitle: product.value.title,
    productImage: productImage
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
    
    // 清除所有临时消息和映射
    messages.value = messages.value.filter(msg => !msg.isTemp)
    tempMessagesMap.value.clear()
    
    await nextTick(() => scrollToBottom())
    await markChatRead(sessionId.value)
  } catch (error) {
    console.error('加载聊天记录失败', error)
  }
}

const handleChatClose = () => {
  // 关闭对话框时，可以选择清除临时消息
  // 但不清除历史消息，以便下次打开时还能看到
  messages.value = messages.value.filter(msg => !msg.isTemp)
  tempMessagesMap.value.clear()
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

.product-images {
  position: relative;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.thumbnail-item {
  border: 2px solid transparent;
  border-radius: 4px;
  transition: all 0.3s;
  padding: 2px;
}

.thumbnail-item:hover {
  border-color: #409eff;
}

.thumbnail-item.active {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
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
  display: flex;
  align-items: center;
}

.label {
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
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