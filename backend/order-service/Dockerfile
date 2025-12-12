# Stage 1: Build with Maven (优化版)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /workspace

# 1. 复制整个项目
COPY . .

# 2. 构建根项目，这样所有模块都会被构建和安装到本地仓库
WORKDIR /workspace/backend
RUN mvn clean install -DskipTests

# 3. 构建 order-service
WORKDIR /workspace/backend/order-service
RUN mvn clean package -DskipTests

# Stage 2: Run with minimal JRE
FROM eclipse-temurin:17-jre-alpine  

# 设置时区和工作目录
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
WORKDIR /app

# 创建非root用户（安全）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 复制 jar 文件
COPY --from=builder --chown=appuser:appgroup /workspace/backend/order-service/target/*.jar ./app.jar

# 暴露端口
EXPOSE 8083

# 优化 JVM 参数
ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]