package com.grarcht.shuttle.framework.screen

import android.app.Activity
import android.os.Bundle
import com.grarcht.shuttle.framework.bundle.MockBundleFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ActivityLifecycleCallbackTests {

    @Test
    fun verifyTheOnActivityCreatedFunctionWithTwoParametersIsCalled() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityCreated(activity, savedInstanceState = null)
        Assertions.assertEquals(2, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheLawOfDemeterIsFollowedWhenCallingTheOnActivityCreatedFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityCreated(activity)
        Assertions.assertEquals(1, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheOnActivityStartedFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityStarted(activity)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheOnActivityResumedFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityResumed(activity)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheOnActivityPausedFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityPaused(activity)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheOnActivityStoppedFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityStopped(activity)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }

    @Test
    fun verifyTheOnActivitySaveInstanceStateFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        val savedInstanceState = MockBundleFactory().create()
        callback.onActivitySaveInstanceState(activity, savedInstanceState)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }
    @Test
    fun verifyTheOnActivityDestroyedFunctionIsANoOperationFunction() {
        val callback = TestActivityLifecycleCallback()
        val activity = mock(Activity::class.java)
        callback.onActivityDestroyed(activity)
        Assertions.assertEquals(0, callback.numberOfInvocations)
    }

    private open class TestActivityLifecycleCallback : ActivityLifecycleCallback() {
        var numberOfInvocations = 0

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            numberOfInvocations++
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

        override fun onActivityCreated(activity: Activity) {
            numberOfInvocations++
        }
    }
}