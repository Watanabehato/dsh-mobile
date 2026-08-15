#!/usr/bin/env bash
# 通过 GitHub Actions 远程构建 dsh-mobile APK
#
# 前置条件：
#   1. 已安装 gh 并登录：gh auth login
#   2. 当前 git 仓库已关联 GitHub 远程仓库
#   3. 已推送 .github/workflows/build-apk.yml 到 GitHub
#
# 用法：
#   bash scripts/gh-build.sh

set -euo pipefail

cd "$(dirname "$0")/.."

if ! gh auth status >/dev/null 2>&1; then
    echo "错误：gh 未登录，请先运行 gh auth login"
    exit 1
fi

if ! git remote get-url origin >/dev/null 2>&1; then
    echo "错误：当前仓库没有 origin 远程，请先添加："
    echo "  git remote add origin git@github.com:<user>/<repo>.git"
    echo "  git push -u origin main"
    exit 1
fi

echo "==> 触发 GitHub Actions 构建"
gh workflow run build-apk.yml --ref main

echo "==> 等待构建完成"
gh run watch --exit-status --interval 10

RUN_ID=$(gh run list --workflow build-apk.yml --limit 1 --json databaseId --jq '.[0].databaseId')
echo "==> 下载构建产物 (run: $RUN_ID)"
rm -rf dist
mkdir -p dist
gh run download "$RUN_ID" --name dsh-mobile-debug --dir dist

echo "完成："
ls -lh dist/*.apk
