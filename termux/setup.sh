#!/data/data/com.termux/files/usr/bin/bash
# dsh-mobile Termux 一键安装脚本
# 用法: bash setup.sh

set -euo pipefail

echo "==> 更新 Termux 软件源"
pkg update -y

echo "==> 安装必要工具"
pkg install -y openssh autossh termux-services termux-api

echo "==> 创建 ~/bin 目录"
mkdir -p "$HOME/bin" "$HOME/.ssh"

echo "==> 复制隧道脚本"
cp "$(dirname "$0")/dsh-tunnel.sh" "$HOME/bin/dsh-tunnel.sh"
cp "$(dirname "$0")/dsh-easytier.sh" "$HOME/bin/dsh-easytier.sh"
chmod +x "$HOME/bin/dsh-tunnel.sh" "$HOME/bin/dsh-easytier.sh"

echo "==> 生成 SSH 密钥（如果不存在）"
if [ ! -f "$HOME/.ssh/id_ed25519" ]; then
    ssh-keygen -t ed25519 -N "" -f "$HOME/.ssh/id_ed25519"
fi

echo ""
echo "完成！接下来："
echo "1. 编辑 ~/bin/dsh-tunnel.sh 填入你的服务器信息"
echo "2. 把 ~/.ssh/id_ed25519.pub 加到服务器的 ~/.ssh/authorized_keys"
echo "3. 运行 ~/bin/dsh-tunnel.sh 测试隧道"
echo "4. 如需开机自启：复制 termux/boot/start-dsh-tunnel.sh 到 ~/.termux/boot/"
