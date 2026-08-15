package com.dsh.mobile

import com.jcraft.jsch.Session

/**
 * 全局隧道状态，供 Activity 轮询显示。
 */
object SshTunnelState {
    @Volatile
    var connected: Boolean = false

    @Volatile
    var message: String = "未连接"

    @Volatile
    var localPort: Int = 3080

    @Volatile
    internal var session: Session? = null
}
