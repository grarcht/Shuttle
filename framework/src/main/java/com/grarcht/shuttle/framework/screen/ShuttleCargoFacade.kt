package com.grarcht.shuttle.framework.screen

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

private const val ON_PRESSED_CALLBACK_ENABLED = true
private const val LOG_TAG = "ShuttleCargoFacade"

/**
 * This is a contractual interface for a facade that  hides functionality, such as removing cargo after a delivery
 * (data is removed from the db after the user presses the back button on the screen where the data was delivered).
 * This class uses the Facade Design Pattern.  For more information on this pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/facade_pattern.htm">Facade Design Pattern</a>
 */
open class ShuttleCargoFacade(
    application: Application,
    private val shuttleWarehouse: ShuttleWarehouse,
    private val handler: Handler? = Handler(Looper.getMainLooper())
) : ShuttleFacade {
    internal val screenCallback = ScreenCallback()

    init {
        application.registerActivityLifecycleCallbacks(screenCallback)
    }

    /**
     * Removes the cargo in the warehouse after it has been delivered.
     * @param currentScreenClass where the cargo is first used with Shuttle
     * @param nextScreenClass the screen where app is navigating to next
     * @param cargoId denotes the cargo to remove
     */
    override fun removeCargoAfterDelivery(currentScreenClass: Class<*>, nextScreenClass: Class<*>, cargoId: String) {
        val activityTypeName = nextScreenClass.typeName
        screenCallback.screens.add(Screen(activityTypeName, cargoId))
    }

    internal inner class ScreenCallback : ActivityLifecycleCallback() {
        val screens = mutableListOf<Screen>()

        override fun onActivityCreated(activity: Activity) {
            if (activity is AppCompatActivity) {
                val activityTypeName = activity.javaClass.typeName

                screens.forEach { screen ->
                    if (activityTypeName == screen.typeName ||
                        activityTypeName.contains(screen.typeName)
                    ) {
                        // watch for the back press event
                        val callback = ActivityBackPressedCallback(
                            screen,
                            activity,
                            ON_PRESSED_CALLBACK_ENABLED
                        )

                        activity.onBackPressedDispatcher.addCallback(callback)
                    }
                }
            }
        }
    }

    private inner class ActivityBackPressedCallback(
        private val screen: Screen,
        private val activity: Activity,
        enabled: Boolean,
    ) : OnBackPressedCallback(enabled) {

        override fun handleOnBackPressed() {
            isEnabled = false

            GlobalScope.launch {
                shuttleWarehouse.removeCargoBy(screen.cargoId).consumeAsFlow().collectLatest {
                    when (it) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            // ignore
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist,
                        is ShuttleRemoveCargoResult.Removed,
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            cancel() // cancel the channel
                        }
                    }
                }
            }.invokeOnCompletion { throwable ->
                throwable?.let {
                    Log.e(LOG_TAG, "Caught when removing cargo by id.", it)
                }

                // Call on back pressed so the user doesn't have to hit the back button twice.
                handler?.post {
                    activity.onBackPressed()
                }

                // avoid extra callbacks
                screenCallback.screens.remove(screen)
            }
        }
    }

    internal class Screen(val typeName: String, val cargoId: String)
}
