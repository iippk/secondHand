# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 1. 先复制 package 文件（利用缓存）
COPY package*.json ./

# 2. 安装依赖（使用 ci 模式，确保版本一致）
RUN npm ci --registry=https://registry.npmjs.org --no-audit --no-fund

# 3. 复制源代码
COPY . .

# 4. 构建应用
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 用户（安全）
RUN adduser -D -g '' nginxuser && \
    chown -R nginxuser:nginxuser /var/cache/nginx && \
    chown -R nginxuser:nginxuser /var/log/nginx && \
    chown -R nginxuser:nginxuser /etc/nginx/conf.d

# 复制构建结果
COPY --from=builder --chown=nginxuser:nginxuser /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

USER nginxuser

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]