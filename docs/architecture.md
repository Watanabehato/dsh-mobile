# dsh Mobile：手机随时随地使用 dsh 的开源方案文档

> 目标：做一个 Android APK，配合 Termux 建立 SSH 隧道/EasyTier 组网，让用户随时随地在手机上通过 WebView 使用服务器的 `dsh web`。
> 本文档是完整方案设计，后续可据此生成 Android 工程、Termux 脚本和服务端配置。

---

## 1. 项目定位

- **产品名（建议）**：`dsh-mobile`
- **开源协议建议**：Apache-2.0 或 MIT（若依赖 EasyTier 二进制，注意其开源协议与分发要求）
- **核心体验**：
  1. 手机打开 APK，进入 dsh Web UI；
  2. 隧道由 Termux 负责（SSH 隧道或 EasyTier）；
  3. APK 只做 WebView 壳、配置管理、隧道状态提示和快速启动；
  4. 支持两种网络场景：**公网服务器直连** 与 **内网/NAT 穿透**。

---

## 2. 两种网络场景

### 场景 A：dsh 运行在“有公网 IP 的服务器”上

```
手机 APK (WebView)
      │
      │ http://127.0.0.1:8080
      ▼
Termux 内 SSH 本地端口转发
      │ ssh -L 127.0.0.1:8080:127.0.0.1:<dsh-port> user@server
      ▼
公网服务器 sshd
      │
      ▼
dsh web 监听 127.0.0.1:<dsh-port>
```

- 优点：简单、稳定、只需一个 SSH 账号。
- 关键点：dsh web 只监听 `127.0.0.1`，不直接暴露公网，安全性好。
- 手机需要能访问服务器公网 IP:22。

### 场景 B：dsh 运行在“家里/内网，无公网 IP”上

有两种推荐实现：

#### B1：反向 SSH 隧道（轻量，适合已有公网 VPS）

```
内网服务器（运行 dsh web）
      │ ssh -R 127.0.0.1:8080:127.0.0.1:<dsh-port> user@relay-vps
      ▼
公网中继 VPS（sshd + GatewayPorts 可选）
      │
      ▼
手机 Termux 再 SSH 到 VPS，或直接访问 VPS 的转发端口
```

- 适合：已经有一台便宜的公网 VPS。
- 注意：如果只让 VPS 的 `127.0.0.1:8080` 转发，手机还需要再 SSH 到 VPS 做本地转发；如果 VPS 开启 `GatewayPorts yes` 并绑定 `0.0.0.0`，手机可直接访问，但必须加鉴权/防火墙。

#### B2：EasyTier 组网（推荐长期使用，NAT 穿透更友好）

```
内网服务器：easytier-core（加入虚拟网）
手机：EasyTier 官方 APK / Termux 运行 easytier-core（加入同一虚拟网）

手机 APK (WebView)
      │ http://<内网服务器虚拟IP>:<dsh-port>
      ▼
EasyTier 虚拟局域网/P2P 网络
      │
      ▼
内网服务器 dsh web
```

- EasyTier 是 Rust 实现的去中心化组网工具，支持 UDP/TCP/WebSocket、NAT 穿透、虚拟网卡。
- 比 SSH 反向隧道更适合“长期在线、多条路径自动选择、多设备组网”。
- 手机端可以使用：
  1. EasyTier 官方 Android App（最简单）；
  2. 在 Termux 中运行 `easytier-core`（适合自动化）；
  3. 以后如果要“一个 APK 全内置”，可以把 `easytier-core` 的 Android 二进制编进 APK，用 VpnService/TUN 跑起来（工作量较大）。

> 关于“EasyTier 内核”：是的，第二个版本可以用 EasyTier 作为组网内核。推荐先以“官方 App / Termux 运行 easytier-core + 我们的 WebView 壳”开源；如果社区需要，再演进为“APK 内置 EasyTier core + VpnService”。

---

## 3. 总体架构

### 3.1 组件

| 组件 | 技术选型 | 说明 |
| --- | --- | --- |
| Android APK | Kotlin + Jetpack Compose / XML + WebView | 提供配置页、状态页、内嵌 dsh Web UI |
| 隧道层 | Termux + OpenSSH / autossh | 建立 SSH 本地/反向隧道 |
| 组网层（可选） | EasyTier | 替代/补充 SSH 隧道，实现 NAT 穿透 |
| 服务端 | dsh web + systemd | 提供 Web UI，监听 127.0.0.1 |
| 配置存储 | Android `EncryptedSharedPreferences` | 保存服务器地址、端口、SSH 用户等，不硬编码密钥 |
| 密钥管理 | Termux `~/.ssh/id_ed25519` + `ssh-agent` | 私钥留在 Termux 内部，APK 不接触私钥 |

### 3.2 APK 功能清单（v1）

1. **连接配置**
   - 场景选择：公网直连 / 反向隧道 / EasyTier。
   - 服务器地址、SSH 端口、用户名、dsh Web 端口。
   - 隧道本地端口（默认 8080）。
2. **隧道启动入口**
   - 方式 1：调用 Termux `RUN_COMMAND` Intent 执行预设脚本（需用户在 Termux 设置中允许外部应用执行命令）。
   - 方式 2：引导用户手动打开 Termux 运行脚本（最兼容）。
   - 方式 3：跳转 EasyTier App 或 Termux 快捷方式。
3. **WebView 访问 dsh**
   - 加载 `http://127.0.0.1:<local-port>` 或 `http://<EasyTier虚拟IP>:<dsh-port>`。
   - 处理 WebView 的 JS、localStorage、下载、新窗口。
4. **状态与日志**
   - 显示隧道进程是否存活。
   - 显示最近日志（SSH 连接失败、重连等）。
5. **开机/后台自启（可选）**
   - Termux:Boot 实现开机启动隧道。
   - APK 通过前台服务保持 WebView 活跃（可选）。

---

## 4. Termux 隧道脚本设计

### 4.1 公网直连（场景 A）

```bash
# ~/bin/dsh-tunnel.sh
#!/data/data/com.termux/files/usr/bin/bash
set -e

SSH_HOST="${SSH_HOST:-your-server.com}"
SSH_PORT="${SSH_PORT:-22}"
SSH_USER="${SSH_USER:-user}"
DSH_PORT="${DSH_PORT:-8080}"          # dsh web 实际端口
LOCAL_PORT="${LOCAL_PORT:-8080}"      # 手机本地端口

exec autossh -M 0 \
  -o "ServerAliveInterval=30" \
  -o "ServerAliveCountMax=3" \
  -o "ExitOnForwardFailure=yes" \
  -o "StrictHostKeyChecking=accept-new" \
  -N \
  -L "127.0.0.1:${LOCAL_PORT}:127.0.0.1:${DSH_PORT}" \
  -p "${SSH_PORT}" \
  "${SSH_USER}@${SSH_HOST}"
```

### 4.2 反向 SSH 隧道（场景 B1，内网服务器上执行）

```bash
# 在运行 dsh 的内网服务器上，用 systemd 或 screen 保持执行
# 假设中继 VPS 用户为 relay，dsh web 端口 8080
ssh -N -R 127.0.0.1:8080:127.0.0.1:8080 \
  -o ServerAliveInterval=30 \
  -o ExitOnForwardFailure=yes \
  relay@your-vps.com
```

### 4.3 EasyTier（场景 B2）

内网服务器：

```bash
# 示例：easytier-core 加入一个虚拟网
easytier-core \
  -n dsh-net \
  -k YOUR_NETWORK_SECRET \
  --ipv4 10.144.144.1 \
  -p tcp://public-server:11010  # 可选的公共节点，帮助 NAT 穿透
```

手机 Termux：

```bash
pkg install rust  # 或直接使用官方 Android APK
easytier-core \
  -n dsh-net \
  -k YOUR_NETWORK_SECRET \
  --ipv4 10.144.144.2
```

之后手机 WebView 访问 `http://10.144.144.1:8080`。

> 具体参数以 EasyTier 官方文档为准；这里只展示设计方向。

---

## 5. Android 工程设计

### 5.1 建议目录结构

```
dsh-mobile/
├── README.md
├── LICENSE
├── docs/
│   └── architecture.md          # 本文档的正式版
├── android/
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/dsh/mobile/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── WebViewScreen.kt
│   │   │   │   ├── SettingsScreen.kt
│   │   │   │   ├── TunnelManager.kt
│   │   │   │   └── TermuxIntent.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── termux/
│   ├── setup.sh                 # 一键安装 openssh/autossh/termux-services
│   ├── dsh-tunnel.sh            # 公网直连隧道脚本
│   ├── dsh-reverse.sh           # 反向隧道脚本（也可放服务端）
│   ├── dsh-easytier.sh          # EasyTier 启动脚本
│   └── boot/
│       └── start-dsh-tunnel.sh  # Termux:Boot 自启脚本
├── server/
│   ├── dsh-web.service          # systemd unit
│   ├── reverse-tunnel.service   # 内网服务器反向隧道 unit
│   └── easytier.service         # EasyTier unit
└── scripts/
    └── build-apk.sh             # CI 构建脚本
```

### 5.2 关键 Android 实现点

- **WebView 配置**
  - 开启 JavaScript、DOM storage。
  - 设置 `WebViewClient` / `WebChromeClient`。
  - 对 `http://127.0.0.1` 或 `http://10.x.x.x` 允许混合内容（如果 dsh 页面内有 http 资源）。
  - 处理页面标题、加载进度、错误页。

- **调用 Termux**
  - 使用 `Intent` 向 Termux 发送 `RUN_COMMAND`：
    ```kotlin
    val intent = Intent("com.termux.RUN_COMMAND").apply {
        setClassName("com.termux", "com.termux.app.RunCommandService")
        putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/home/bin/dsh-tunnel.sh")
        putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("--background"))
        putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
    }
    startService(intent)
    ```
  - 需要用户在 Termux 的 “允许外部应用执行命令” 中授权。
  - 兼容性兜底：显示“请手动在 Termux 中运行”的引导页。

- **状态检测**
  - 通过尝试连接 `http://127.0.0.1:<local-port>` 判断隧道是否就绪。
  - 或读取 Termux 日志/进程（较复杂，v1 可以先做端口探测）。

### 5.3 安全设计

- **不把私钥放进 APK**。私钥只放在 Termux 的 `~/.ssh`，APK 只存服务器地址、用户名等非敏感配置。
- dsh web 默认只监听 `127.0.0.1`，不要直接暴露到公网。
- 如果使用公网 VPS 转发端口，必须：
  - 使用防火墙只允许自己的 IP；
  - 或仍通过 SSH 本地转发访问；
  - 给 dsh web 加访问令牌/反向代理 Basic Auth。
- 使用 `EncryptedSharedPreferences` 保存口令类配置。
- 开源时提供示例配置，不要提交任何真实密钥。

---

## 6. 服务端配置示例

### 6.1 dsh web 的 systemd unit

```ini
# /etc/systemd/system/dsh-web.service
[Unit]
Description=dsh web UI
After=network.target

[Service]
User=dsh
WorkingDirectory=/home/dsh
ExecStart=/usr/local/bin/dsh web --host 127.0.0.1 --port 8080
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
```

### 6.2 反向隧道 systemd unit（内网服务器）

```ini
[Unit]
Description=Reverse SSH tunnel to relay VPS
After=network-online.target
Wants=network-online.target

[Service]
User=tunnel
ExecStart=/usr/bin/ssh -N -R 127.0.0.1:8080:127.0.0.1:8080 \
  -o ServerAliveInterval=30 \
  -o ExitOnForwardFailure=yes \
  relay@your-vps.com
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### 6.3 EasyTier systemd unit

```ini
[Unit]
Description=EasyTier mesh node
After=network-online.target
Wants=network-online.target

[Service]
User=easytier
ExecStart=/usr/local/bin/easytier-core \
  -n dsh-net \
  -k YOUR_NETWORK_SECRET \
  --ipv4 10.144.144.1
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
```

---

## 7. 开发与开源路线图

### Phase 0：手动验证（半天）
1. 在一台服务器上启动 `dsh web --host 127.0.0.1 --port 8080`。
2. 手机 Termux 安装 `openssh autossh`。
3. 手动运行 SSH 本地转发，手机浏览器访问 `127.0.0.1:8080`，确认 dsh Web 可用。

### Phase 1：APK WebView 壳（1-2 天）
1. 创建 Android 工程。
2. 实现配置页 + WebView 页。
3. 支持手动输入本地端口并打开 dsh。
4. 支持通过 Termux Intent 启动隧道脚本（可选）。

### Phase 2：Termux 自动化（1-2 天）
1. 编写 `setup.sh` 一键安装依赖。
2. 编写隧道脚本和 Termux:Boot 自启。
3. 在 APK 中加入“检测隧道是否存活/一键打开”功能。

### Phase 3：EasyTier 版本（2-3 天）
1. 搭建 EasyTier 虚拟网。
2. 手机使用 EasyTier 官方 App 或 Termux 运行 core。
3. APK 增加“EasyTier 模式”，WebView 指向虚拟 IP。

### Phase 4：开源发布
1. 补充 README、截图、架构图。
2. 编写 GitHub Actions：构建 debug APK。
3. 发布 Release APK 和 Termux 脚本包。
4. 可选：调研把 EasyTier core 编进 APK，做成真正的“一体 APK”。

---

## 8. 风险与注意点

| 风险 | 影响 | 应对 |
| --- | --- | --- |
| Termux 后台被杀 | 隧道断开 | 使用 Termux:Boot、前台通知、`wakelock`；引导用户加入电池白名单 |
| 手机网络切换（Wi-Fi/4G） | SSH 断开 | autossh 自动重连；EasyTier 多路径更稳 |
| dsh web 暴露公网 | 被扫描攻击 | 只监听 127.0.0.1；必须走隧道；加 Basic Auth/Token |
| APK 内置 EasyTier 复杂 | 开发量大、TUN 权限/兼容性坑多 | v1 不内置；先依赖官方 App/Termux |
| 不同 Android 厂商后台限制 | WebView 或 Termux 被清理 | 文档写明各厂商设置；提供“手动打开 Termux”兜底 |
| EasyTier 版本/参数变化 | 脚本失效 | 文档锁定版本；配置集中管理 |

---

## 9. 下一步

1. 确认 dsh 服务端端口与认证方式（是否有 token、是否已有反向代理）。
2. 我先按本文档生成：
   - `android/` Android Studio 工程骨架（Kotlin + WebView）；
   - `termux/` 安装与隧道脚本；
   - `server/` systemd 示例；
   - `README.md` 开源说明。
3. 先做 **公网直连 + WebView 壳** 的可用 MVP，再迭代 EasyTier 版本。

---

*文档生成时间：2026-08-15*
*状态：方案待评审*
