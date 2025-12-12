# Stage 1: Build with Maven
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /workspace

# Copy entire project
COPY . .

# 1. 构建根项目，这样所有模块都会被构建和安装到本地仓库
WORKDIR /workspace/backend
RUN mvn clean install -DskipTests

# 2. 构建 gateway-service
WORKDIR /workspace/backend/gateway-service
RUN mvn clean package -DskipTests

# Stage 2: Run with Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy jar file from build stage
COPY --from=builder /workspace/backend/gateway-service/target/*.jar ./app.jar

# Set environment variables
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# Expose port
EXPOSE 8080

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]