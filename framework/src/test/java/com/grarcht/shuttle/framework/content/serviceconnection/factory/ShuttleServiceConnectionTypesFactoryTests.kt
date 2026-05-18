package com.grarcht.shuttle.framework.content.serviceconnection.factory

import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnection
import com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware.ShuttleLifecycleAwareServiceConnection
import com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware.ShuttleLifecycleAwareServiceConnectionConfig
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val SERVICE_NAME = "TestService"

/**
 * Verifies the functionality of [ShuttleServiceConnectionTypesFactory]. This factory is the
 * central point for creating both standard and lifecycle-aware service connections and their
 * configurations. Without it, components that need to bind to a ShuttleService would have no
 * standardised way to construct the appropriate connection type.
 */
class ShuttleServiceConnectionTypesFactoryTests {

    private val factory = ShuttleServiceConnectionTypesFactory()

    @Test
    fun verifyCreateServiceConnectionConfigFromParams() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()

        val config = factory.createServiceConnectionConfig(
            context = null,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel
        )

        assertNotNull(config)
    }

    @Test
    fun verifyCreateServiceConnectionFromConfig() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val config = factory.createServiceConnectionConfig(
            context = null,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            coroutineScope = scope,
            serviceChannel = channel
        )

        val connection: ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>> =
            factory.createServiceConnection(config)

        assertNotNull(connection)
    }

    @Test
    fun verifyCreateServiceConnectionFromParams() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()

        val connection: ShuttleServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>> =
            factory.createServiceConnection(
                context = null,
                serviceName = SERVICE_NAME,
                errorObservable = errorObservable,
                useWithIPC = false,
                coroutineScope = scope,
                serviceChannel = channel
            )

        assertNotNull(connection)
    }

    @Test
    fun verifyCreateServiceConnectionConfigFromLifecycleAwareConfig() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val lifecycle = mock<Lifecycle>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val lifecycleAwareConfig = ShuttleLifecycleAwareServiceConnectionConfig(
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

        val config = factory.createServiceConnectionConfig(lifecycleAwareConfig)

        assertNotNull(config)
    }

    @Test
    fun verifyCreateLifecycleAwareServiceConnectionConfigFromParams() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val lifecycle = mock<Lifecycle>()
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()

        val config = factory.createLifecycleAwareServiceConnectionConfig(
            context = null,
            serviceClazz = ShuttleService::class.java,
            lifecycle = lifecycle,
            serviceName = SERVICE_NAME,
            errorObservable = errorObservable,
            useWithIPC = false,
            coroutineScope = scope,
            serviceChannel = channel
        )

        assertNotNull(config)
    }

    @Test
    fun verifyCreateLifecycleAwareServiceConnectionFromConfig() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val lifecycle = mock<Lifecycle>()
        whenever(lifecycle.addObserver(org.mockito.kotlin.any())).then { }
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()
        val lifecycleAwareConfig = ShuttleLifecycleAwareServiceConnectionConfig(
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

        val connection: ShuttleLifecycleAwareServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>> =
            factory.createLifecycleAwareServiceConnection(lifecycleAwareConfig)

        assertNotNull(connection)
    }

    @Test
    fun verifyCreateLifecycleAwareServiceConnectionFromParams() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val lifecycle = mock<Lifecycle>()
        whenever(lifecycle.addObserver(org.mockito.kotlin.any())).then { }
        val scope = TestScope()
        val channel = Channel<ShuttleConnectedServiceModel<ShuttleService>>()

        val connection: ShuttleLifecycleAwareServiceConnection<ShuttleService, ShuttleBinder<ShuttleService>> =
            factory.createLifecycleAwareServiceConnection(
                context = null,
                serviceClazz = ShuttleService::class.java,
                lifecycle = lifecycle,
                serviceName = SERVICE_NAME,
                errorObservable = errorObservable,
                useWithIPC = false,
                coroutineScope = scope,
                serviceChannel = channel
            )

        assertAll(
            { assertNotNull(connection) },
            { assertTrue(connection is ShuttleLifecycleAwareServiceConnection) }
        )
    }
}
