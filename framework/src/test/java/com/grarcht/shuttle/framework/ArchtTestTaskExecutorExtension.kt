package com.grarcht.shuttle.framework

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * A JUnit 5 extension that replaces the Architecture Components [ArchTaskExecutor] delegate with a
 * synchronous implementation before each test and restores the default after each test, allowing
 * LiveData and other arch-core constructs to run on the test thread without requiring an Android device.
 */
class ArchtTestTaskExecutorExtension : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(Executor())
    }

    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    private class Executor : TaskExecutor() {
        override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

        override fun postToMainThread(runnable: Runnable) = runnable.run()

        override fun isMainThread(): Boolean = true
    }
}
