package com.grarcht.shuttle.framework.screen

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * This class simplifies logic and improves readability by enforcing only one function to be overwritten.
 */
internal abstract class ActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        onActivityCreated(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        // no-operation
    }

    override fun onActivityResumed(activity: Activity) {
        // no-operation
    }

    override fun onActivityPaused(activity: Activity) {
        // no-operation
    }

    override fun onActivityStopped(activity: Activity) {
        // no-operation
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // no-operation
    }

    override fun onActivityDestroyed(activity: Activity) {
        // no-operation
    }

    /**
     * Notifies of the activity having been created to perform actions accordingly.
     * @param activity the newly created activity
     */
    abstract fun onActivityCreated(activity: Activity)
}

