#!/data/data/com.termux/files/usr/bin/bash
# Termux:Boot 开机自启脚本
# 安装方法：
#   pkg install termux-boot
#   把本文件放到 ~/.termux/boot/start-dsh-tunnel.sh
#   并给予执行权限
set -euo pipefail

# 等待网络就绪
sleep 5

# 如果隧道脚本存在则后台启动
if [ -x "$HOME/bin/dsh-tunnel.sh" ]; then
    nohup "$HOME/bin/dsh-tunnel.sh" > "$HOME/.dsh-tunnel.log" 2>&1 &
fi
