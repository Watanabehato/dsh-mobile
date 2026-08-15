# dsh-mobile

在 Android 手机上随时随地使用 `dsh web` 的开源客户端。

- APK：Kotlin + WebView 壳
- 隧道：Termux + OpenSSH / autossh
- 组网：EasyTier（可选）
- 服务端：dsh web + systemd 示例

## 快速开始

### 1. 服务端

```bash
dsh web --host 127.0.0.1 --port 3080
```

生产环境建议用 systemd 托管，参考 `server/dsh-web.service`。

### 2. Termux 手机端

```bash
# 安装依赖
bash termux/setup.sh

# 编辑隧道脚本中的服务器信息
vim ~/bin/dsh-tunnel.sh

# 启动隧道
~/bin/dsh-tunnel.sh
```

然后手机浏览器打开 `http://127.0.0.1:3080` 验证。

### 3. Android App

用 Android Studio 打开 `android/` 目录，构建并安装到手机。

App 内填写：

- 隧道类型：`ssh-local` / `ssh-reverse` / `easytier`
- 本地 Web 地址：`http://127.0.0.1:3080` 或 EasyTier 虚拟 IP
- 可选：通过 Termux `RUN_COMMAND` 一键启动隧道

## 目录结构

```text
android/       Android Studio 工程
termux/        Termux 安装与隧道脚本
server/        systemd 服务示例
scripts/       构建辅助脚本
docs/          架构文档
```

## 开源协议

MIT，见 [LICENSE](LICENSE)。
