# Chat Service 故障排查指南

## 404错误排查

如果遇到404错误，请检查以下几点：

### 1. 确认chat-service已启动
- 检查服务是否在8085端口运行
- 查看控制台日志确认服务启动成功

### 2. 确认服务已注册到Nacos
- 访问Nacos控制台：http://localhost:8848/nacos
- 检查服务列表中是否有 `chat-service`
- 确认服务状态为"健康"

### 3. 确认网关路由配置
- 网关配置路径：`/api/chat-service/**`
- StripPrefix=2，会去掉 `/api/chat-service` 前缀
- 最终转发到chat-service的路径：`/api/chat/xxx`

### 4. 测试直接访问chat-service
如果网关有问题，可以尝试直接访问chat-service：
```
http://localhost:8085/api/chat/unread-count
```

### 5. 检查数据库连接
- 确认数据库 `cshp` 存在
- 确认 `chat_message` 表存在
- 如果表结构需要更新，执行：`update_chat_message_table.sql`

### 6. WebSocket连接
- WebSocket端点：`/ws`
- 通过网关访问：`/api/chat-service/ws`
- 确认WebSocket路由配置正确

## 常见问题

### 问题1：404 Not Found
**原因**：服务未启动或未注册到Nacos
**解决**：重启chat-service，确认Nacos连接正常

### 问题2：消息发送失败
**原因**：WebSocket连接失败
**解决**：检查WebSocket路径配置，确认网关支持WebSocket转发

### 问题3：数据库错误
**原因**：表结构不匹配
**解决**：执行数据库更新脚本

