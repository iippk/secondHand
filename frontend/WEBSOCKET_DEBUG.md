# WebSocket 连接调试指南

## 问题排查步骤

### 1. 检查浏览器控制台
打开浏览器开发者工具（F12），查看：
- Console标签：查看连接日志和错误信息
- Network标签：查看WebSocket连接状态

### 2. 检查连接状态
在浏览器控制台输入：
```javascript
// 检查WebSocket连接
console.log('STOMP客户端:', window.stompClient)
```

### 3. 测试直接连接（绕过网关）
如果网关有问题，可以尝试直接连接到chat-service：
```javascript
const socket = new SockJS('http://localhost:8085/ws')
```

### 4. 检查服务状态
- chat-service是否在8085端口运行
- gateway-service是否在8080端口运行
- Nacos中chat-service是否已注册

### 5. 检查网络请求
在Network标签中查找：
- `/api/chat-service/ws` 的WebSocket连接
- 状态应该是101 Switching Protocols（WebSocket握手成功）

## 常见问题

### 问题1：连接失败
**症状**：控制台显示"WebSocket连接失败"
**可能原因**：
- chat-service未启动
- 网关路由配置错误
- 防火墙阻止连接

**解决方法**：
1. 确认chat-service已启动
2. 检查网关配置
3. 尝试直接连接chat-service测试

### 问题2：消息发送无反应
**症状**：点击发送按钮没有反应
**可能原因**：
- WebSocket未连接
- STOMP客户端未初始化
- 消息格式错误

**解决方法**：
1. 检查控制台是否有错误
2. 确认WebSocket连接状态
3. 查看sendMessage函数的日志输出

### 问题3：消息发送但接收不到
**症状**：消息发送成功但对方收不到
**可能原因**：
- 后端消息处理失败
- 消息队列订阅失败
- 数据库保存失败

**解决方法**：
1. 查看chat-service控制台日志
2. 检查数据库chat_message表
3. 确认消息队列订阅成功

