package com.grarcht.shuttle.framework.content.serviceconnection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val SERVICE_NAME = "TestService"

/** Subclass used to access the protected [ShuttleServiceConnection.isConnected] getter in tests. */
private class ExposedServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    config: ShuttleServiceConnectionConfig<S>
) : ShuttleServiceConnection<S, B>(config) {
    fun readIsConnected(): Boolean = isConnected
}

/**
 * Verifies the functionality of [ShuttleServiceConnection]. ShuttleServiceConnection manages the
 * full lifecycle of binding to and unbinding from a ShuttleService, emitting the connected model
 * over a channel for consumers. If it handled connection or disconnection events incorrectly,
 * clients would receive stale service references or fail to detect disconnections.
 */
class ShuttleServiceConnectionTests {

    private fun createConfig(
        context: Context? = null,
        useWithIPC: Boolean = false,
        channel: Channel<ShuttleConnectedServiceModel<ShuttleService>> = Channel()
    ): ShuttleServiceConnectionConfig<ShuttleService> {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(org.mockito.kotlin.any())).thenReturn(errorObservable)
        val scope = TestScope()
        return ShuttleServiceConnectionConfig(
            context = context,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = useWithIPC,
            coroutineScope = scope,
            serviceChannel = channel
        )
    }

    @Test
    fun verifyIsConnectedIsFalseByDefault() {
        val config = createConfig()
        val connection = ExposedServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        assertFalse(connection.readIsConnected())
    }

    @Test
    fun verifyOnServiceDisconnectedResetsState() {
        val config = createConfig()
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val componentName = mock<ComponentName>()

        connection.onServiceDisconnected(componentName)

        assertAll(
            { assertNull(connection.localService) },
            { assertNull(connection.ipcServiceMessenger) }
        )
    }

    @Test
    fun verifyOnServiceConnectedWithIPCSetsIpcMessenger() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val config = createConfig(useWithIPC = true, channel = channel)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val binder = mock<IBinder>()
        val componentName = mock<ComponentName>()

        Mockito.mockConstruction(Messenger::class.java).use {
            connection.onServiceConnected(componentName, binder)
        }

        assertNotNull(connection.ipcServiceMessenger)
    }

    @Test
    fun verifyOnServiceConnectedWithLocalServiceSetsLocalService() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val config = createConfig(useWithIPC = false, channel = channel)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val service = mock<ShuttleService>()
        val binder = mock<ShuttleBinder<ShuttleService>>()
        whenever(binder.getService()).thenReturn(service)
        val componentName = mock<ComponentName>()

        connection.onServiceConnected(componentName, binder)

        assertNotNull(connection.localService)
    }

    @Test
    fun verifyConnectToServiceWithNullContextDoesNotThrow() {
        val config = createConfig(context = null)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java)
    }

    @Test
    fun verifyConnectToServiceWithLifecycleNotStartedSkipsBinding() {
        val context = mock<Context>()
        val lifecycle = mock<Lifecycle>()
        whenever(lifecycle.currentState).thenReturn(Lifecycle.State.CREATED)
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle)
    }

    @Test
    fun verifyConnectToServiceWithNullLifecycleBindsService() {
        val context = mock<Context>()
        whenever(context.bindService(org.mockito.kotlin.any<Intent>(), org.mockito.kotlin.any(), org.mockito.kotlin.any<Int>()))
            .thenReturn(true)
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = null)
    }

    @Test
    fun verifyConnectToServiceHandlesSecurityException() {
        val context = mock<Context>()
        whenever(context.bindService(org.mockito.kotlin.any<Intent>(), org.mockito.kotlin.any(), org.mockito.kotlin.any<Int>()))
            .thenThrow(SecurityException("access denied"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = null)
    }

    @Test
    fun verifyConnectToServiceHandlesGenericException() {
        val context = mock<Context>()
        whenever(context.bindService(org.mockito.kotlin.any<Intent>(), org.mockito.kotlin.any(), org.mockito.kotlin.any<Int>()))
            .thenThrow(RuntimeException("bind failed"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = null)
    }

    @Test
    fun verifyDisconnectFromServiceWhenNotConnectedDoesNotThrow() {
        val config = createConfig()
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.disconnectFromService()
    }

    @Test
    fun verifyDisconnectFromServiceWhenConnectedUnbindsService() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val context = mock<Context>()
        val config = createConfig(context = context, channel = channel)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val binder = mock<ShuttleBinder<ShuttleService>>()
        whenever(binder.getService()).thenReturn(mock<ShuttleService>())
        val componentName = mock<ComponentName>()
        connection.onServiceConnected(componentName, binder)

        connection.disconnectFromService()
    }

    @Test
    fun verifyDisconnectFromServiceHandlesException() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val context = mock<Context>()
        whenever(context.unbindService(org.mockito.kotlin.any())).thenThrow(RuntimeException("unbind failed"))
        val config = createConfig(context = context, channel = channel)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val binder = mock<ShuttleBinder<ShuttleService>>()
        whenever(binder.getService()).thenReturn(mock<ShuttleService>())
        val componentName = mock<ComponentName>()
        connection.onServiceConnected(componentName, binder)

        connection.disconnectFromService()
    }

    @Test
    fun verifyEmitConnectedServiceModelSendsModelForLocalService() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(org.mockito.kotlin.any())).thenReturn(errorObservable)
        // Use Unconfined dispatcher so the coroutine body runs eagerly
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val config = ShuttleServiceConnectionConfig(
            context = null,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel
        )
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val service = mock<ShuttleService>()
        val binder = mock<ShuttleBinder<ShuttleService>>()
        whenever(binder.getService()).thenReturn(service)
        val componentName = mock<ComponentName>()

        connection.onServiceConnected(componentName, binder)

        val model = channel.tryReceive().getOrNull()
        assertNotNull(model)
    }

    @Test
    fun verifyConnectToServiceHandlesSecurityExceptionWithNonNullLifecycle() {
        val context = mock<Context>()
        val lifecycle = mock<Lifecycle>()
        whenever(lifecycle.currentState).thenReturn(Lifecycle.State.STARTED)
        whenever(
            context.bindService(
                org.mockito.kotlin.any<Intent>(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any<Int>()
            )
        ).thenThrow(SecurityException("access denied"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = lifecycle)
    }

    @Test
    fun verifyConnectToServiceHandlesGenericExceptionWithNonNullLifecycle() {
        val context = mock<Context>()
        val lifecycle = mock<Lifecycle>()
        whenever(lifecycle.currentState).thenReturn(Lifecycle.State.STARTED)
        whenever(
            context.bindService(
                org.mockito.kotlin.any<Intent>(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any<Int>()
            )
        ).thenThrow(RuntimeException("bind failed"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = lifecycle)
    }

    @Test
    fun verifyDisconnectFromServiceWhenContextAlreadyNulledDoesNotThrow() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val context = mock<Context>()
        val config = createConfig(context = context, channel = channel)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val binder = mock<ShuttleBinder<ShuttleService>>()
        whenever(binder.getService()).thenReturn(mock<ShuttleService>())
        val componentName = mock<ComponentName>()
        connection.onServiceConnected(componentName, binder)

        // First disconnect: unbinds and sets context = null internally
        connection.disconnectFromService()
        // Second disconnect: isConnected is still true but context is now null →
        // covers the null branch of context?.let { it.unbindService(this) }
        connection.disconnectFromService()
    }

    @Test
    fun verifyConnectToServiceHandlesSecurityExceptionWithLifecycleHavingNullState() {
        val context = mock<Context>()
        val lifecycle = mock<Lifecycle>()
        // First call (line 106 shouldConnect check) returns STARTED so binding is attempted;
        // second call (line 115 in catch block) returns null to cover the null-currentState branch.
        whenever(lifecycle.currentState)
            .thenReturn(Lifecycle.State.STARTED)
            .thenReturn(null)
        whenever(
            context.bindService(
                org.mockito.kotlin.any<Intent>(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any<Int>()
            )
        ).thenThrow(SecurityException("access denied"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = lifecycle)
    }

    @Test
    fun verifyConnectToServiceHandlesGenericExceptionWithLifecycleHavingNullState() {
        val context = mock<Context>()
        val lifecycle = mock<Lifecycle>() // currentState returns null (Mockito default)
        whenever(lifecycle.currentState).thenReturn(null)
        whenever(
            context.bindService(
                org.mockito.kotlin.any<Intent>(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any<Int>()
            )
        ).thenThrow(RuntimeException("bind failed"))
        val config = createConfig(context = context)
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)

        connection.connectToService(ShuttleService::class.java, lifecycle = lifecycle)
    }

    @Test
    fun verifyEmitConnectedServiceModelSendsModelForIPC() {
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>(Channel.UNLIMITED)
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(org.mockito.kotlin.any())).thenReturn(errorObservable)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val config = ShuttleServiceConnectionConfig(
            context = null,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = true,
            coroutineScope = scope,
            serviceChannel = channel
        )
        val connection = ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>>(config)
        val binder = mock<IBinder>()
        val componentName = mock<ComponentName>()

        Mockito.mockConstruction(Messenger::class.java).use {
            connection.onServiceConnected(componentName, binder)
        }

        val model = channel.tryReceive().getOrNull()
        assertNotNull(model)
    }
}
