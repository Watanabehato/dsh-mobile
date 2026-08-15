package com.dsh.mobile

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 通过 Termux 的 RUN_COMMAND 能力启动隧道脚本。
 *
 * 用户需要在 Termux 的“允许外部应用执行命令”中授权本应用。
 */
object TermuxLauncher {

    private const val TAG = "TermuxLauncher"
    private const val TERMUX_PACKAGE = "com.termux"
    private const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
    private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"

    fun startTunnel(context: Context) {
        val intent = Intent(RUN_COMMAND_ACTION).apply {
            setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/home/bin/dsh-tunnel.sh")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("--background"))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
        }
        try {
            context.startService(intent)
            Log.i(TAG, "Termux tunnel start intent sent")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to start Termux tunnel", e)
            throw e
        }
    }
}
