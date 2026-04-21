package com.grarcht.shuttle.framework.app

import android.os.Messenger
import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnectionConfig
import com.grarcht.shuttle.framework.content.serviceconnection.factory.ShuttleServiceConnectionFactory
import com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware.ShuttleLifecycleAwareServiceConnectionConfig
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

private const val SERVICE_NAME = "TestService"

class ShuttleConnectedServiceModelTests {

    @Test
    fun verifyShuttleConnectedServiceModelHoldsDefaultNullValues() {
        val model = ShuttleConnectedServiceModel<ShuttleService>()
        assertNull(model.localService)
        assertNull(model.ipcMessenger)
    }

    @Test
    fun verifyShuttleConnectedServiceModelHoldsProvidedValues() {
        val service = mock<ShuttleService>()
        val messenger = mock<Messenger>()
        val model = ShuttleConnectedServiceModel(service, messenger)

        assertEquals(service, model.localService)
        assertEquals(messenger, model.ipcMessenger)
    }

    @Test
    fun verifyShuttleServiceConnectionConfigHoldsValues() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val config = ShuttleServiceConnectionConfig(
            context = null,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel
        )

        assertNotNull(config)
        assertEquals(SERVICE_NAME, config.serviceName)
        assertEquals(false, config.useWithIPC)
    }

    @Test
    fun verifyShuttleLifecycleAwareServiceConnectionConfigHoldsValues() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val lifecycle = mock<Lifecycle>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val factory = mock<ShuttleServiceConnectionFactory>()
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

        assertNotNull(config)
        assertEquals(SERVICE_NAME, config.serviceName)
        assertEquals(ShuttleService::class.java, config.serviceClazz)
    }
}
