package com.grarcht.shuttle.demo.processdeath.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val KILL_DELAY_MS = 2000L
private const val UNKNOWN_PID = -1

/**
 * Kills the main app process from a separate `:kill` process so Android treats it as an
 * external kill — preserving the task back stack in recents — rather than a self-kill,
 * which clears the task.
 *
 * Uses [goAsync] to keep the receiver alive long enough for the [delay] before the kill.
 */
class AppProcessKillerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pid = intent.getIntExtra(EXTRA_PID, UNKNOWN_PID)
        if (pid != UNKNOWN_PID) {
            killAppProcess(pid)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun killAppProcess(pid: Int) {
        val pendingResult = goAsync()
        // GlobalScope is intentional: this receiver runs in the :kill process, which has no
        // application-level scope. The coroutine must outlive onReceive() (via goAsync()) and
        // its lifetime is naturally bounded by the delay + Process.killProcess(), so no leak.
        GlobalScope.launch(Dispatchers.Default) {
            delay(KILL_DELAY_MS)
            Process.killProcess(pid)
            pendingResult.finish()
        }
    }

    companion object {
        const val EXTRA_PID = "extra_pid"
    }
}
