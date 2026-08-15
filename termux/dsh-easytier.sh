#!/data/data/com.termux/files/usr/bin/bash
# dsh-mobile EasyTier 组网启动脚本
# 场景：dsh web 运行在内网，手机通过 EasyTier 虚拟网访问
#
# 需要先安装 easytier-core：
# - 使用 EasyTier 官方 Android App，或
# - 在 Termux 中通过 cargo/二进制安装 easytier-core
#
# 使用前编辑下面的变量，或通过环境变量覆盖。

set -euo pipefail

EASYTIER_BIN="${EASYTIER_BIN:-easytier-core}"
NETWORK_NAME="${NETWORK_NAME:-dsh-net}"
NETWORK_SECRET="${NETWORK_SECRET:-change-me}"
PHONE_IPV4="${PHONE_IPV4:-10.144.144.2}"
PUBLIC_NODE="${PUBLIC_NODE:-}"   # 例如 tcp://1.2.3.4:11010，可选

if ! command -v "$EASYTIER_BIN" >/dev/null 2>&1; then
    echo "未找到 easytier-core，请先安装："
    echo "  1. 使用 EasyTier 官方 Android App；或"
    echo "  2. 在 Termux 中安装 easytier-core 后重试。"
    exit 1
fi

ARGS=(
  -n "$NETWORK_NAME"
  -k "$NETWORK_SECRET"
  --ipv4 "$PHONE_IPV4"
)

if [ -n "$PUBLIC_NODE" ]; then
  ARGS+=(-p "$PUBLIC_NODE")
fi

echo "==> 启动 EasyTier: $NETWORK_NAME / $PHONE_IPV4"
exec "$EASYTIER_BIN" "${ARGS[@]}"
