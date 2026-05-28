#!/bin/bash

set -e

echo "=========================================="
echo "  图书馆管理系统 - 一键部署脚本"
echo "=========================================="

VERSION="${1:-latest}"
INSTALL_DIR="/opt/library-management-system"
SERVICE_NAME="library-backend"

color_red='\033[0;31m'
color_green='\033[0;32m'
color_yellow='\033[1;33m'
color_reset='\033[0m'

log_info() { echo -e "${color_green}[INFO]${color_reset} $1"; }
log_warn() { echo -e "${color_yellow}[WARN]${color_reset} $1"; }
log_error() { echo -e "${color_red}[ERROR]${color_reset} $1"; }

check_dependencies() {
    log_info "检查系统依赖..."
    local missing=()
    command -v docker >/dev/null 2>&1 || missing+=("docker")
    command -v wget >/dev/null 2>&1 || missing+=("wget")
    command -v unzip >/dev/null 2>&1 || missing+=("unzip")
    
    if [ ${#missing[@]} -ne 0 ]; then
        log_error "缺少以下依赖: ${missing[*]}"
        log_info "安装命令: apt-get install docker.io wget unzip (Ubuntu/Debian)"
        exit 1
    fi
    log_info "依赖检查通过 ✓"
}

download_release() {
    if [ "$VERSION" = "latest" ]; then
        DOWNLOAD_URL=$(curl -s https://api.github.com/repos/gxfdev/library-management-system/releases/latest | grep browser_download_url | cut -d '"' -f 4)
    else
        DOWNLOAD_URL="https://github.com/gxfdev/library-management-system/releases/download/v${VERSION}/library-management-system-v${VERSION}.zip"
    fi
    
    if [ -z "$DOWNLOAD_URL" ]; then
        log_error "未找到版本 v${VERSION} 的发布包"
        exit 1
    fi
    
    log_info "下载发布包: $(basename $DOWNLOAD_URL) ..."
    mkdir -p /tmp/library-deploy
    cd /tmp/library-deploy
    wget -q --show-progress "$DOWNLOAD_URL"
}

install() {
    log_info "安装到 $INSTALL_DIR ..."
    sudo mkdir -p "$INSTALL_DIR"
    sudo rm -rf "$INSTALL_DIR"/*
    unzip -qo "/tmp/library-deploy/$(ls /tmp/library-deploy/*.zip)" -d "$INSTALL_DIR"
    
    if [ ! -f "$INSTALL_DIR/.env" ]; then
        cp "$INSTALL_DIR/.env.example" "$INSTALL_DIR/.env"
        log_warn "请编辑 .env 文件配置数据库密码和JWT密钥:"
        log_warn "  sudo nano $INSTALL_DIR/.env"
    fi
    
    log_info "文件解压完成 ✓"
}

start_services() {
    cd "$INSTALL_DIR"
    log_info "启动服务..."
    docker compose up -d --build
    
    log_info "等待服务启动..."
    sleep 10
    
    if curl -sf http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        log_info "后端服务启动成功 ✓"
    else
        log_warn "后端服务可能还在启动中，请稍后检查: docker logs library-backend"
    fi
    
    if curl -sf http://localhost > /dev/null 2>&1; then
        log_info "前端服务启动成功 ✓"
    else
        log_warn "前端服务可能还在启动中，请稍后检查: docker logs library-frontend"
    fi
}

stop_services() {
    if [ -d "$INSTALL_DIR" ] && [ -f "$INSTALL_DIR/docker-compose.yml" ]; then
        cd "$INSTALL_DIR"
        docker compose down
        log_info "服务已停止"
    else
        log_warn "未找到已安装的服务"
    fi
}

show_status() {
    echo ""
    echo "=========================================="
    echo "  服务状态"
    echo "=========================================="
    docker ps --filter "name=library" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
    echo "访问地址:"
    echo "  前端: http://$(hostname -I | awk '{print $1}')"
    echo "  后端API: http://$(hostname -I | awk '{print $1}'):8080"
    echo "  默认账号: admin / admin123"
}

case "${2:-install}" in
    install)
        check_dependencies
        download_release
        install
        start_services
        show_status
        ;;
    update)
        stop_services
        check_dependencies
        download_release
        install
        start_services
        show_status
        ;;
    stop)
        stop_services
        ;;
    start)
        start_services
        show_status
        ;;
    status)
        show_status
        ;;
    uninstall)
        stop_services
        sudo rm -rf "$INSTALL_DIR"
        docker volume rm library_mysql-data 2>/dev/null || true
        log_info "已完全卸载"
        ;;
    *)
        echo "用法: $0 <version> <command>"
        echo ""
        echo "Commands:"
        echo "  install   安装并启动（默认）"
        echo "  update    更新到指定版本"
        echo "  start     启动服务"
        echo "  stop      停止服务"
        echo "  status    查看状态"
        echo "  uninstall 完全卸载"
        echo ""
        echo "Examples:"
        echo "  $0 latest install     # 安装最新版"
        echo "  $0 1.0.0 install      # 安装 v1.0.0"
        echo "  $0 latest update      # 更新到最新版"
        exit 1
        ;;
esac
