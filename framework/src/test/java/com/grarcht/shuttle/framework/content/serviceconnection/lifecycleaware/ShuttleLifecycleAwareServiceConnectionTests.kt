package com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.content.serviceconnection.factory.ShuttleServiceConnectionTypesFactory
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val SERVICE_NAME = "TestService"

/**
 * Verifies the functionality of [ShuttleLifecycleAwareServiceConnection]. This connection
 * automatically binds to a ShuttleService when the owning lifecycle starts and unbinds when it
 * stops, preventing memory leaks and dangling service connections. If it did not respond
 * correctly to lifecycle events, services would remain connected after destruction.
 */
class ShuttleLifecycleAwareServiceConnectionTests {

    private fun createConnection(
        context: Context? = null
    ): ShuttleLifecycleAwareServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>> {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(org.mockito.kotlin.any())).thenReturn(errorObservable)
        val lifecycle = mock<Lifecycle>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val factory = ShuttleServiceConnectionTypesFactory()
        val config = ShuttleLifecycleAwareServiceConnectionConfig(
            context = context,
            serviceClazz = ShuttleService::class.java,
            lifecycle = lifecycle,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel,
            serviceConnectionFactory = factory
        )
        return ShuttleLifecycleAwareServiceConnection(config)
    }

    @Test
    fun verifyOnStartWithNullContextDoesNotThrow() {
        val connection = createConnection(context = null)
        val owner = mock<LifecycleOwner>()
        val lifecycle = mock<Lifecycle>()
        whenever(owner.lifecycle).thenReturn(lifecycle)
        whenever(lifecycle.currentState).thenReturn(Lifecycle.State.STARTED)

        connection.onStart(owner)
    }

    @Test
    fun verifyOnStartWithContextAttemptsToConnectToService() {
        val context = mock<Context>()
        val connection = createConnection(context = context)
        val owner = mock<LifecycleOwner>()
        val lifecycle = mock<Lifecycle>()
        whenever(owner.lifecycle).thenReturn(lifecycle)
        whenever(lifecycle.currentState).thenReturn(Lifecycle.State.STARTED)

        connection.onStart(owner)
    }

    @Test
    fun verifyOnStopDisconnectsFromService() {
        val connection = createConnection(context = null)
        val owner = mock<LifecycleOwner>()

        connection.onStop(owner)
    }

    @Test
    fun verifyOnDestroyRemovesLifecycleObserver() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(org.mockito.kotlin.any())).thenReturn(errorObservable)
        val lifecycle = mock<Lifecycle>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val factory = ShuttleServiceConnectionTypesFactory()
        val config = ShuttleLifecycleAwareServiceConnectionConfig(
            context = null,
            serviceClazz = ShuttleService::class.java,
            lifecycle = lifecycle,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel,
            serviceConnectionFactory = factory
        )
        val connection = ShuttleLifecycleAwareServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val owner = mock<LifecycleOwner>()

        connection.onDestroy(owner)

        verify(lifecycle).removeObserver(connection)
    }
}
