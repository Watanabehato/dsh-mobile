# 服务端配置

## dsh web

```bash
sudo cp dsh-web.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now dsh-web
```

确认监听：

```bash
ss -lntp | grep 8080
```

## 反向隧道（内网服务器）

修改 `reverse-tunnel.service` 中的中继 VPS 地址和用户，然后安装。

## EasyTier（内网服务器）

修改 `easytier.service` 中的网络名、密钥和虚拟 IP，然后安装。

```bash
sudo cp easytier.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now easytier
```

> 所有示例都假设 dsh web 监听 `127.0.0.1:8080`，请按实际端口调整。
