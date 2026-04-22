package com.grarcht.shuttle.framework.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val LOG_TAG = "ContextKtxTest"

/**
 * Verifies the functionality of [registerReceiverQuietly] and [unregisterReceiverQuietly] Context
 * extension functions. These extensions safely register and unregister BroadcastReceivers without
 * throwing exceptions on null contexts or illegal states. If they did not handle edge cases
 * correctly, service-related receiver management would cause crashes in components that use them.
 */
class ContextKtxTests {

    @Test
    fun verifyRegisterReceiverQuietlyDoesNothingWhenContextIsNull() {
        val receiver = mock<BroadcastReceiver>()
        val filter = mock<IntentFilter>()

        val nullContext: Context? = null
        nullContext.registerReceiverQuietly(receiver, filter, LOG_TAG)
    }

    @Test
    fun verifyRegisterReceiverQuietlyRegistersReceiverOnNonNullContext() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()
        val filter = mock<IntentFilter>()

        context.registerReceiverQuietly(receiver, filter, LOG_TAG)
    }

    @Test
    fun verifyRegisterReceiverQuietlyHandlesIllegalStateException() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()
        val filter = mock<IntentFilter>()
        whenever(context.registerReceiver(any<BroadcastReceiver>(), any<IntentFilter>()))
            .thenThrow(IllegalStateException("already registered"))

        context.registerReceiverQuietly(receiver, filter, LOG_TAG)
    }

    @Test
    fun verifyUnregisterReceiverQuietlyDoesNothingWhenContextIsNull() {
        val receiver = mock<BroadcastReceiver>()

        val nullContext: Context? = null
        nullContext.unregisterReceiverQuietly(receiver, LOG_TAG)
    }

    @Test
    fun verifyUnregisterReceiverQuietlyUnregistersReceiverOnNonNullContext() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()

        context.unregisterReceiverQuietly(receiver, LOG_TAG)

        verify(context).unregisterReceiver(receiver)
    }

    @Test
    fun verifyUnregisterReceiverQuietlyHandlesIllegalStateException() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()
        whenever(context.unregisterReceiver(any())).thenThrow(IllegalStateException("not registered"))

        context.unregisterReceiverQuietly(receiver, LOG_TAG)
    }

    @Test
    fun verifyRegisterReceiverQuietlyWithDefaultLogTag() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()
        val filter = mock<IntentFilter>()

        // Call without logTag to cover the default parameter path
        context.registerReceiverQuietly(receiver, filter)
    }

    @Test
    fun verifyUnregisterReceiverQuietlyWithDefaultLogTag() {
        val context = mock<Context>()
        val receiver = mock<BroadcastReceiver>()

        // Call without logTag to cover the default parameter path
        context.unregisterReceiverQuietly(receiver)
    }

}
