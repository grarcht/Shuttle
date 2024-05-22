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
 * Creates variations of [ServiceConnection]s. This interface is based on the Factory Design Pattern.
 * For more information on the design pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
interface ShuttleServiceConnectionFactory {

    /**
     * Creates a Shuttle service connection config.
     *
     * @param context used for connecting to the service
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    fun <S : ShuttleService> createShuttleServiceConnectionConfig(
        context: Context?,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean = false,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnectionConfig<S>

    /**
     * Creates a Shuttle service connection with the [config].
     *
     * @param config configures the [ShuttleServiceConnection]
     * @return the newly created config
     */
    fun <S : ShuttleService, B : ShuttleBinder<S>> createServiceConnection(
        config: ShuttleServiceConnectionConfig<S>
    ): ShuttleServiceConnection<S, B>

    /**
     * Creates a Shuttle service connection with the params.
     *
     * @param context used for connecting to the service
     * @param serviceName used for logging
     * @param errorObservable provides visibility in to possible errors
     * @param useWithIPC true, if this service should be remote and use interprocess communication
     * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
     * @param serviceChannel emits a [ShuttleConnectedServiceModel]
     *
     * @return the newly created config
     */
    @Suppress("LongParameterList")
    fun <S : ShuttleService, B : ShuttleBinder<S>> createServiceConnection(
        context: Context?,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnection<S, B>

    /**
     * Creates a Shuttle service connection with the [ShuttleLifecycleAwareServiceConnectionConfig], intended for
     * [ShuttleLifecycleAwareServiceConnection] that extends [ShuttleServiceConnection].
     *
     * @param config configures a [ShuttleServiceConnection]
     *
     * @return the newly created config
     */
    fun <S : ShuttleService, B : ShuttleBinder<S>> createShuttleServiceConnectionConfig(
        config: ShuttleLifecycleAwareServiceConnectionConfig<S>
    ): ShuttleServiceConnectionConfig<S>

    /**
     * Creates a lifecycle aware Shuttle service connection config with the params.
     *
     * @param context used for connecting to the service
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
    fun <S : ShuttleService> createLifecycleAwareServiceConnectionConfig(
        context: Context?,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleLifecycleAwareServiceConnectionConfig<S>

    /**
     * Creates a lifecycle aware Shuttle service connection with the [config].
     *
     * @param config configures the [ShuttleLifecycleAwareServiceConnection]
     *
     * @return the newly created config
     */
    fun <S : ShuttleService, B : ShuttleBinder<S>> createLifecycleAwareServiceConnection(
        config: ShuttleLifecycleAwareServiceConnectionConfig<S>
    ): ShuttleLifecycleAwareServiceConnection<S, B>

    /**
     * Creates a lifecycle aware Shuttle service connection with the params.
     *
     * @param context used for connecting to the service
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
    fun <S : ShuttleService, B : ShuttleBinder<S>> createLifecycleAwareServiceConnection(
        context: Context?,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle,
        serviceName: String,
        errorObservable: ShuttleVisibilityObservable,
        useWithIPC: Boolean,
        coroutineScope: CoroutineScope,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleLifecycleAwareServiceConnection<S, B>
}
