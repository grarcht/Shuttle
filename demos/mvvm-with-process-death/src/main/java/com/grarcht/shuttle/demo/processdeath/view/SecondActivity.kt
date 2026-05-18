package com.grarcht.shuttle.demo.processdeath.view

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.processdeath.receiver.AppProcessKillerReceiver
import com.grarcht.shuttle.demo.processdeath.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The destination activity for the second screen in the MVVM with Process Death demo. Receives
 * cargo from the first screen, sets up the Compose content via [SecondView], and provides a
 * mechanism to simulate process death by delegating a kill to the separate
 * [com.grarcht.shuttle.demo.processdeath.receiver.AppProcessKillerReceiver] process.
 */
@AndroidEntryPoint
class SecondActivity : ComponentActivity() {
    private val viewModel: SecondViewModel by viewModels()
    private lateinit var secondView: SecondView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        window.setBackgroundDrawable(ContextCompat.getColor(this, com.grarcht.shuttle.demo.core.R.color.colorBeige).toDrawable())
        super.onCreate(savedInstanceState)
        secondView = SecondView(this, viewModel, shuttle)
        setContent {
            MaterialTheme {
                secondView.SetViewContent(savedInstanceState, intent.extras, ::killAppAfterBackgrounding)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Saving the state in a guarded fashion. The super call must have the shuttle bundle to
        // ensure apps do not crash during transactions with saving and restoring states.
        val outStateShuttleBundle = secondView.getSavedInstanceState(outState)
        super.onSaveInstanceState(outStateShuttleBundle)
    }

    /**
     * Moves the task to the back and then sends a broadcast to
     * [com.grarcht.shuttle.demo.processdeath.receiver.AppProcessKillerReceiver] running in the
     * separate [":kill"] process to terminate the main app process after a short delay. Delegating
     * the kill to an external process causes Android to preserve the task back stack in recents,
     * which is required to demonstrate the process death restoration flow.
     */
    fun killAppAfterBackgrounding() {
        // Delegate the kill to AppProcessKillerReceiver running in the :kill process. Android only
        // preserves the task back stack when the kill originates externally; a self-kill clears
        // the task. goAsync() + delay in the receiver provides the 2-second window for the
        // system to commit lifecycle state before the kill fires.
        sendBroadcast(
            Intent(this, AppProcessKillerReceiver::class.java).apply {
                putExtra(AppProcessKillerReceiver.EXTRA_PID, Process.myPid())
            }
        )
        moveTaskToBack(true)
    }
}
