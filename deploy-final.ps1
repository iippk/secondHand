# deploy-final.ps1 - 最终部署脚本
Write-Host "=== CSHP 平台最终部署 ===" -ForegroundColor Green

# 1. 清理环境
Write-Host "步骤 1: 停止并清理现有容器..." -ForegroundColor Yellow
docker-compose down
docker rm -f $(docker ps -aq) 2>$null

# 2. 检查端口占用
Write-Host "步骤 2: 检查端口占用..." -ForegroundColor Yellow
$ports = @(3306, 6379, 8848, 18080, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 80)
foreach ($port in $ports) {
    $process = netstat -ano | findstr ":$port"
    if ($process) {
        Write-Host "端口 $port 被占用" -ForegroundColor Red
        # 如果需要，可以在这里结束进程
    }
}

# 3. 启动基础服务（不带端口冲突检查）
Write-Host "步骤 3: 启动基础服务..." -ForegroundColor Cyan
docker-compose up -d mysql redis nacos sentinel

# 4. 等待基础服务启动
Write-Host "步骤 4: 等待基础服务启动（90秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 90

# 5. 检查基础服务状态
Write-Host "步骤 5: 检查基础服务状态..." -ForegroundColor Cyan
docker-compose ps

# 6. 构建微服务（使用简化版）
Write-Host "步骤 6: 构建网关服务..." -ForegroundColor Cyan
docker build -f backend\gateway-service\Dockerfile.simple -t cshp-gateway-service backend\gateway-service

# 7. 启动网关服务
Write-Host "步骤 7: 启动网关服务..." -ForegroundColor Cyan
docker-compose up -d gateway-service

# 8. 构建并启动前端
Write-Host "步骤 8: 构建并启动前端..." -ForegroundColor Cyan
docker build -f frontend\Dockerfile -t cshp-frontend frontend\
docker-compose up -d frontend

# 9. 等待所有服务启动
Write-Host "步骤 9: 等待所有服务启动（60秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# 10. 显示最终状态
Write-Host "=== 最终容器状态 ===" -ForegroundColor Green
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host "=== 部署完成！===" -ForegroundColor Green
Write-Host "前端访问: http://localhost" -ForegroundColor Cyan
Write-Host "Nacos控制台: http://localhost:8848 (用户名: nacos, 密码: nacos)" -ForegroundColor Cyan
Write-Host "Sentinel控制台: http://localhost:18080 (用户名: sentinel, 密码: sentinel)" -ForegroundColor Cyan
Write-Host "MySQL: localhost:3306 (root/123456)" -ForegroundColor Cyan
Write-Host "Redis: localhost:6379 (密码: redis123)" -ForegroundColor Cyan