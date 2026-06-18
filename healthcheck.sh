#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | xargs)
else
    echo "❌ 错误: 未找到 .env 配置文件"
    exit 1
fi

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0

pass() {
    echo -e "${GREEN}✅ PASS${NC} - $1"
    PASS=$((PASS + 1))
}

fail() {
    echo -e "${RED}❌ FAIL${NC} - $1"
    FAIL=$((FAIL + 1))
}

info() {
    echo -e "${YELLOW}ℹ️  INFO${NC} - $1"
}

echo "=========================================="
echo "  厂区弱电布线配件资产台账系统"
echo "  健康检查脚本"
echo "=========================================="
echo ""
echo "项目端口分配:"
echo "  前端: $FRONTEND_PORT"
echo "  后端: $BACKEND_PORT"
echo "  MySQL: $MYSQL_PORT"
echo "  Redis: $REDIS_PORT"
echo ""
echo "=========================================="
echo "  一、容器状态检查"
echo "=========================================="

SERVICES=("mysql" "redis" "backend" "frontend")
for svc in "${SERVICES[@]}"; do
    STATUS=$(docker compose ps --format '{{.Status}}' "$svc" 2>/dev/null || echo "")
    if echo "$STATUS" | grep -q "Up"; then
        pass "容器 $svc 运行正常: $STATUS"
    else
        fail "容器 $svc 状态异常: $STATUS"
    fi
done

echo ""
echo "=========================================="
echo "  二、端口绑定检查（仅绑定 127.0.0.1）"
echo "=========================================="

check_port() {
    local port=$1
    local result
    result=$(lsof -nP -iTCP:$port -sTCP:LISTEN 2>/dev/null)
    
    if [ -z "$result" ]; then
        fail "端口 $port 未被监听"
        return
    fi
    
    if echo "$result" | grep -q "127.0.0.1:$port"; then
        if echo "$result" | grep -qvE "(0\.0\.0\.0|:::|\*:)" ; then
            pass "端口 $port 仅绑定 127.0.0.1: $result"
        else
            fail "端口 $port 绑定了非法地址: $result"
        fi
    else
        fail "端口 $port 未绑定 127.0.0.1: $result"
    fi
}

check_port $FRONTEND_PORT
check_port $BACKEND_PORT
check_port $MYSQL_PORT
check_port $REDIS_PORT

echo ""
echo "=========================================="
echo "  三、前端访问一致性检查"
echo "=========================================="

info "等待前端页面加载..."
sleep 2

TITLE_127=$(curl -sS http://127.0.0.1:$FRONTEND_PORT 2>/dev/null | grep -o '<title>[^<]*</title>' | sed 's/<title>\(.*\)<\/title>/\1/')
TITLE_LOCAL=$(curl -sS http://localhost:$FRONTEND_PORT 2>/dev/null | grep -o '<title>[^<]*</title>' | sed 's/<title>\(.*\)<\/title>/\1/')

if [ -z "$TITLE_127" ]; then
    fail "无法获取 127.0.0.1:$FRONTEND_PORT 页面标题"
else
    pass "127.0.0.1:$FRONTEND_PORT 页面标题: $TITLE_127"
fi

if [ -z "$TITLE_LOCAL" ]; then
    fail "无法获取 localhost:$FRONTEND_PORT 页面标题"
else
    pass "localhost:$FRONTEND_PORT 页面标题: $TITLE_LOCAL"
fi

if [ "$TITLE_127" = "$TITLE_LOCAL" ] && [ -n "$TITLE_127" ]; then
    pass "127.0.0.1 和 localhost 页面标题一致"
else
    fail "127.0.0.1 和 localhost 页面标题不一致: '$TITLE_127' vs '$TITLE_LOCAL'"
fi

HASH_127=$(curl -sS http://127.0.0.1:$FRONTEND_PORT 2>/dev/null | md5sum | awk '{print $1}')
HASH_LOCAL=$(curl -sS http://localhost:$FRONTEND_PORT 2>/dev/null | md5sum | awk '{print $1}')

if [ "$HASH_127" = "$HASH_LOCAL" ] && [ -n "$HASH_127" ]; then
    pass "127.0.0.1 和 localhost 返回内容 MD5 一致: $HASH_127"
else
    fail "127.0.0.1 和 localhost 返回内容 MD5 不一致"
fi

echo ""
echo "=========================================="
echo "  四、后端 API 检查"
echo "=========================================="

API_RESPONSE=$(curl -s http://127.0.0.1:$BACKEND_PORT/api/dashboard/stats 2>/dev/null)
API_CODE=$(echo "$API_RESPONSE" | grep -o '"code":[0-9]*' | head -1 | sed 's/"code"://')

if [ "$API_CODE" = "200" ]; then
    pass "后端 API 正常: /api/dashboard/stats 返回 code=200"
    info "统计数据: $API_RESPONSE"
else
    fail "后端 API 异常: /api/dashboard/stats 返回 code=$API_CODE, response=$API_RESPONSE"
fi

CATEGORY_RESPONSE=$(curl -s http://127.0.0.1:$BACKEND_PORT/api/categories/tree 2>/dev/null)
CATEGORY_CODE=$(echo "$CATEGORY_RESPONSE" | grep -o '"code":[0-9]*' | head -1 | sed 's/"code"://')

if [ "$CATEGORY_CODE" = "200" ]; then
    pass "后端 API 正常: /api/categories/tree 返回 code=200"
else
    fail "后端 API 异常: /api/categories/tree 返回 code=$CATEGORY_CODE"
fi

echo ""
echo "=========================================="
echo "  五、容器名称检查（与项目名一致）"
echo "=========================================="

EXPECTED_NAMES=("${PROJECT_NAME}_mysql" "${PROJECT_NAME}_redis" "${PROJECT_NAME}_backend" "${PROJECT_NAME}_frontend")
for expected in "${EXPECTED_NAMES[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${expected}$"; then
        pass "容器名称正确: $expected"
    else
        fail "缺少容器或名称错误: $expected"
    fi
done

echo ""
echo "=========================================="
echo "  检查结果汇总"
echo "=========================================="
echo ""
echo -e "${GREEN}通过: $PASS${NC}  ${RED}失败: $FAIL${NC}"
echo ""

if [ "$FAIL" -eq 0 ]; then
    echo -e "${GREEN}=========================================="
    echo "  ✅ 所有检查通过，系统运行正常！"
    echo "==========================================${NC}"
    echo ""
    echo "📱 前端访问地址:"
    echo "   http://localhost:$FRONTEND_PORT"
    echo "   http://127.0.0.1:$FRONTEND_PORT"
    echo ""
    echo "🔧 后端API地址:"
    echo "   http://127.0.0.1:$BACKEND_PORT/api"
    echo ""
    exit 0
else
    echo -e "${RED}=========================================="
    echo "  ❌ 存在 $FAIL 项检查失败，请排查！"
    echo "==========================================${NC}"
    echo ""
    echo "常用排查命令:"
    echo "   查看容器日志: docker compose logs -f"
    echo "   重启服务: docker compose restart"
    echo "   查看端口占用: lsof -nP -iTCP:$FRONTEND_PORT -sTCP:LISTEN"
    echo ""
    exit 1
fi
