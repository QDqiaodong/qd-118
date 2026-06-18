#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | xargs)
else
    echo "❌ 错误: 未找到 .env 配置文件"
    exit 1
fi

check_port() {
    local port=$1
    if lsof -nP -iTCP:$port -sTCP:LISTEN > /dev/null 2>&1; then
        local pid=$(lsof -nP -iTCP:$port -sTCP:LISTEN | tail -1 | awk '{print $2}')
        local name=$(lsof -nP -iTCP:$port -sTCP:LISTEN | tail -1 | awk '{print $1}')
        echo "❌ 端口 $port 已被占用! (PID: $pid, 进程: $name)"
        return 1
    fi
    return 0
}

echo "=========================================="
echo "  厂区弱电布线配件资产台账系统"
echo "  构建部署脚本"
echo "=========================================="
echo ""

echo "🔍 检查端口占用..."
PORTS_TO_CHECK=($FRONTEND_PORT $BACKEND_PORT $MYSQL_PORT $REDIS_PORT)
for port in "${PORTS_TO_CHECK[@]}"; do
    check_port "$port" || {
        echo ""
        echo "请先停止占用端口的进程，或修改 .env 文件中的端口配置。"
        exit 1
    }
    echo "✅ 端口 $port 可用"
done
echo ""

echo "🔨 开始构建 Docker 镜像..."
echo "镜像仓库: $DOCKER_REGISTRY"
echo ""

cd "$SCRIPT_DIR"

echo "📦 清理旧数据卷（可选）..."
read -p "是否清理旧数据卷？(y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker compose down -v
fi

echo ""
echo "🚀 启动所有服务..."
docker compose up -d --build

echo ""
echo "⏳ 等待服务就绪..."
sleep 10

MAX_WAIT=60
WAIT_COUNT=0
while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
    FRONTEND_OK=$(curl -sSf http://127.0.0.1:$FRONTEND_PORT/ > /dev/null 2>&1 && echo "yes" || echo "no")
    BACKEND_OK=$(curl -sSf http://127.0.0.1:$BACKEND_PORT/api/categories/tree > /dev/null 2>&1 && echo "yes" || echo "no")

    if [ "$FRONTEND_OK" = "yes" ] && [ "$BACKEND_OK" = "yes" ]; then
        echo "✅ 服务全部就绪!"
        break
    fi

    echo -n "."
    sleep 2
    WAIT_COUNT=$((WAIT_COUNT + 2))
done

echo ""
echo ""
echo "=========================================="
echo "  ✅  部署成功!"
echo "=========================================="
echo ""
echo "📱 前端访问地址:"
echo "   http://localhost:$FRONTEND_PORT"
echo "   http://127.0.0.1:$FRONTEND_PORT"
echo ""
echo "🔧 后端API地址:"
echo "   http://127.0.0.1:$BACKEND_PORT/api"
echo ""
echo "🗄️  数据库端口: $MYSQL_PORT"
echo "📦 Redis 端口: $REDIS_PORT"
echo ""
echo "📊 容器状态:"
docker compose ps
echo ""
echo "💡 常用命令:"
echo "   停止服务: docker compose down"
echo "   查看日志: docker compose logs -f"
echo "   重启服务: docker compose restart"
echo "   查看端口: lsof -nP -iTCP:$FRONTEND_PORT -sTCP:LISTEN"
echo ""
echo "=========================================="
