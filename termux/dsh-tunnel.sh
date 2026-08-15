#!/data/data/com.termux/files/usr/bin/bash
# dsh-mobile SSH 本地端口转发隧道
# 场景：dsh web 运行在公网可达的服务器上
#
# 使用前编辑下面的变量，或通过环境变量覆盖。

set -euo pipefail

SSH_HOST="${SSH_HOST:-your-server.com}"
SSH_PORT="${SSH_PORT:-22}"
SSH_USER="${SSH_USER:-user}"
DSH_PORT="${DSH_PORT:-8080}"          # 服务器上 dsh web 的端口
LOCAL_PORT="${LOCAL_PORT:-8080}"      # 手机本地监听端口

echo "==> 启动 SSH 隧道: ${SSH_USER}@${SSH_HOST}:${SSH_PORT} -> 127.0.0.1:${DSH_PORT}"

exec autossh -M 0 \
  -o "ServerAliveInterval=30" \
  -o "ServerAliveCountMax=3" \
  -o "ExitOnForwardFailure=yes" \
  -o "StrictHostKeyChecking=accept-new" \
  -N \
  -L "127.0.0.1:${LOCAL_PORT}:127.0.0.1:${DSH_PORT}" \
  -p "${SSH_PORT}" \
  "${SSH_USER}@${SSH_HOST}"
