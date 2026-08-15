# dsh-mobile 架构（纯 APK 版）

## 目标

一个 APK 内完成 SSH 隧道 + dsh Web 访问，不依赖 Termux、不依赖外部 SSH 客户端。

## 架构

```
Android App
├── MainActivity          # 配置 SSH 信息、显示连接状态、承载 WebView
├── SshTunnelService      # 前台服务，持有 SSH 连接和本地端口转发
└── WebView               # 访问 http://127.0.0.1:<localPort>
```

连接链路：

```
WebView
  │ http://127.0.0.1:3080
  ▼
JSch SSH 本地端口转发（APK 内置）
  │ 127.0.0.1:3080 -> 服务器 127.0.0.1:3080
  ▼
服务器 dsh web
```

## 技术选型

- Kotlin + AndroidX
- WebView 显示 dsh Web UI
- JSch (`com.github.mwiede:jsch`) 建立 SSH 隧道
- 前台 Service 保持隧道存活，带常驻通知
- SharedPreferences 保存连接配置（MVP；后续可换 EncryptedSharedPreferences）

## 安全说明

- MVP 使用密码登录，且 `StrictHostKeyChecking=no`，方便内网/自用。
- 正式开源版建议增加：
  - 私钥登录
  - known_hosts 主机校验
  - 密码加密存储
  - 可选 SSH 代理 / 跳板机
- dsh web 仍建议只监听 `127.0.0.1`，不要直接暴露公网。

## 目录

```text
android/app/src/main/java/com/dsh/mobile/
├── MainActivity.kt
├── SshTunnelService.kt
└── SshTunnelState.kt
```

## 后续可做

- 私钥/密钥登录
- EasyTier 或 WireGuard 组网模式
- 连接状态自动重连
- 深色模式、更好的移动端布局
- CI 自动签名 Release APK
