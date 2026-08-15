#!/usr/bin/env bash
# 构建 dsh-mobile debug APK
# 需要本机已安装 Android SDK，并配置 ANDROID_HOME 或 local.properties
set -euo pipefail

cd "$(dirname "$0")/../android"

if [ ! -f local.properties ] && [ -z "${ANDROID_HOME:-}" ]; then
    echo "错误：请设置 ANDROID_HOME 或创建 android/local.properties"
    exit 1
fi

./gradlew :app:assembleDebug

echo "构建完成："
echo "  android/app/build/outputs/apk/debug/app-debug.apk"
