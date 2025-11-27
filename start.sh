#!/bin/bash

# 获取脚本所在的目录
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)

echo "正在启动 留学者指南 系统..."
echo "脚本运行目录: $SCRIPT_DIR"

echo "启动后端服务..."
cd "$SCRIPT_DIR/backend" && mvn spring-boot:run &
BACKEND_PID=$!

echo "等待后端服务启动..."
# 可以考虑更智能的等待方式，例如检查端口是否被监听
sleep 10

echo "启动前端服务..."
cd "$SCRIPT_DIR/frontend" && npm run dev &
FRONTEND_PID=$!

echo "留学者指南 系统启动完成！"
echo "后端服务运行在: http://localhost:7077"
echo "前端服务运行在: http://localhost:5173"

# 等待用户按下 Ctrl+C
trap "echo '正在停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo '服务已停止。'; exit" INT TERM
wait $BACKEND_PID $FRONTEND_PID 