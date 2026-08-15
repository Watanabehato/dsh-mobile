# dsh-mobile

在 Android 手机上随时随地使用 `dsh web` 的开源客户端。

- APK：Kotlin + WebView
- SSH 隧道：**内置在 APK 里**，不需要安装 Termux
- 连接方式：APK 通过 JSch 建立 SSH 本地端口转发
- 服务端：dsh web + systemd 示例

## 特点

- 一个 APK 搞定，不需要额外安装 Termux、SSH 客户端或 EasyTier
- 打开 App 填写服务器信息，点“连接”即可
- 连接成功后自动隐藏配置表单，只显示 dsh Web UI
- 断开连接后自动恢复配置表单，方便修改重连
- Material 3 风格界面，带连接状态提示

## 快速开始

### 1. 服务端

```bash
dsh web --host 127.0.0.1 --port 3080
```

生产环境建议用 systemd 托管，参考 `server/dsh-web.service`。

### 2. Android App

直接安装 APK，打开后填写：

- SSH 服务器地址
- SSH 端口（默认 22）
- 用户名
- 密码
- dsh 端口（默认 3080）
- 本地端口（默认 3080）

点“连接”，连接成功后配置表单自动隐藏，WebView 会自动打开 dsh Web UI。

## 版本

当前版本：`0.4.0`

## 用 GitHub Actions 打包 APK

仓库已包含 `.github/workflows/build-apk.yml`，推到 GitHub 后：

```bash
# 登录 GitHub CLI（只需一次）
gh auth login

# 推送代码
git remote add origin git@github.com:<你的用户名>/<仓库名>.git
git push -u origin main

# 手动触发构建并下载 APK
bash scripts/gh-build.sh
```

也可以在 GitHub 仓库页面：

1. 打开 **Actions**
2. 选择 **Build APK**
3. 点 **Run workflow**
4. 构建完成后下载 `dsh-mobile-debug` 工件里的 APK

## 发布 Release

仓库支持通过 Git tag 触发 Release 构建：

```bash
git tag dsh-harness
git push origin dsh-harness
```

GitHub Actions 会自动构建 APK 并发布到 Release。

## 目录结构

```text
android/       Android Studio 工程（内置 SSH 隧道）
termux/        旧版 Termux 脚本，保留作为参考/备用方案
server/        systemd 服务示例
scripts/       构建辅助脚本
docs/          架构文档
```

## 开源协议

MIT，见 [LICENSE](LICENSE)。
