@REM 设置字符格式支持中文
chcp 65001



@echo off
echo 正在启动 留学者指南 系统...

echo 启动后端服务...
start cmd /k "cd backend && mvn spring-boot:run"

echo 等待后端服务启动...
timeout /t 10

echo 启动前端服务...
start cmd /k "cd frontend && npm run dev"

echo 留学者指南 系统启动完成！
echo 后端服务运行在: http://localhost:7077
echo 前端服务运行在: http://localhost:5174 