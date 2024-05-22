package com.grarcht.shuttle.framework.content.serviceconnection.factory

import android.content.Context
import android.content.ServiceConnection
import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnection
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnectionConfig
import com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware.ShuttleLifecycleAwareServiceConnection
import com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware.ShuttleLifecycleAwareServiceConnectionConfig
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * Creates variations of [ServiceConnection]s. This class is based on the Factory Design Pattern.
 * For more information on the design pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
class ShuttleServiceConnectionTypesFactory : ShuttleServiceConnectionFactory {

    /**
     * Creates a Shuttle service connection config.
     *
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    override fun <S : ShuttleService> createShuttleServiceConnectionConfig(
        context: Context?,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnectionConfig<S> {
        return ShuttleServiceConnectionConfig<S>(
            context,
            serviceName,
            errorObservable,
            useWithIPC,
            coroutineScope,
            serviceChannel
        )
    }

    /**
     * Creates a Shuttle service connection with the [config].
     *
     * @param config configures the [ShuttleServiceConnection]
     * @return the newly created config
     */
    override fun <S : ShuttleService, B : ShuttleBinder<S>> createServiceConnection(
        config: ShuttleServiceConnectionConfig<S>
    ): ShuttleServiceConnection<S, B> {
        return ShuttleServiceConnection(config)
    }

    /**
     * Creates a Shuttle service connection with the params.
     *
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    override fun <S : ShuttleService, B : ShuttleBinder<S>> createServiceConnection(
        context: Context?,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnection<S, B> {
        val config = createShuttleServiceConnectionConfig(
            context,
            serviceName,
            errorObservable,
            useWithIPC,
            coroutineScope,
            serviceChannel
        )
        return ShuttleServiceConnection(config)
    }

    /**
     * Creates a Shuttle service connection with the [ShuttleLifecycleAwareServiceConnectionConfig], intended for
     * [ShuttleLifecycleAwareServiceConnection] that extends [ShuttleServiceConnection].
     *
     * @param config configures a [ShuttleServiceConnection]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    override fun <S : ShuttleService, B : ShuttleBinder<S>> createShuttleServiceConnectionConfig(
        config: ShuttleLifecycleAwareServiceConnectionConfig<S>
    ): ShuttleServiceConnectionConfig<S> {
        return createShuttleServiceConnectionConfig(
            config.context,
            config.serviceName,
            config.errorObservable,
            config.useWithIPC,
            config.coroutineScope,
            config.serviceChannel
        )
    }

    /**
     * Creates a lifecycle aware Shuttle service connection config with the params.
     *
     * @param serviceClazz the [ShuttleService] class to connect to
     * @param context used for service binding
     * @param lifecycle used to connect to and disconnect from the [ShuttleService]
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    override fun <S : ShuttleService> createLifecycleAwareServiceConnectionConfig(
        context: Context?,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleLifecycleAwareServiceConnectionConfig<S> {
        return ShuttleLifecycleAwareServiceConnectionConfig(
            context,
            serviceClazz,
            lifecycle,
            serviceName,
            errorObservable,
            useWithIPC,
            coroutineScope,
            serviceChannel,
            this
        )
    }

    /**
     * Creates a lifecycle aware Shuttle service connection with the [config].
     *
     * @param config configures the [ShuttleLifecycleAwareServiceConnection]
     *
     * @return the newly created config
     */
    override fun <S : ShuttleService, B : ShuttleBinder<S>> createLifecycleAwareServiceConnection(
        config: ShuttleLifecycleAwareServiceConnectionConfig<S>
    ): ShuttleLifecycleAwareServiceConnection<S, B> {
        return ShuttleLifecycleAwareServiceConnection(config)
    }

    /**
     * Creates a lifecycle aware Shuttle service connection with the params.
     *
     * @param serviceClazz the [ShuttleService] class to connect to
     * @param context used for service binding
     * @param lifecycle used to connect to and disconnect from the [ShuttleService]
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    override fun <S : ShuttleService, B : ShuttleBinder<S>> createLifecycleAwareServiceConnection(
        context: Context?,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleLifecycleAwareServiceConnection<S, B> {
        val config = createLifecycleAwareServiceConnectionConfig(
            context,
            serviceClazz,
            lifecycle,
            serviceName,
            errorObservable,
            useWithIPC,
            coroutineScope,
            serviceChannel
        )
        return ShuttleLifecycleAwareServiceConnection(config)
    }
}
