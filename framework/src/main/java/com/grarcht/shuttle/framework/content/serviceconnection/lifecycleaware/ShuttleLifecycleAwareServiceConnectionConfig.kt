package com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.content.serviceconnection.factory.ShuttleServiceConnectionFactory
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * Configures a [ShuttleLifecycleAwareServiceConnection].
 *
 * @param serviceClazz the [ShuttleService] class to connect to
 * @param context used for service binding
 * @param lifecycle used to connect to and disconnect from the [ShuttleService]
 * @param serviceName used for logging
 * @param errorObservable provides visibility in to possible errors
 * @param useWithIPC true, if this service should be remote and use interprocess communication
 * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
 * @param serviceChannel emits a [ShuttleConnectedServiceModel]
 * @param serviceConnectionFactory creates varieties of service connections and associated configs
 */
data class ShuttleLifecycleAwareServiceConnectionConfig<S : ShuttleService>(
    val context: Context?,
    val serviceClazz: Class<S>,
    val lifecycle: Lifecycle,
    val serviceName: String,
    val errorObservable: ShuttleVisibilityObservable,
    val useWithIPC: Boolean = false,
    val coroutineScope: CoroutineScope,
    val serviceChannel: Channel<ShuttleConnectedServiceModel<S>>,
    val serviceConnectionFactory: ShuttleServiceConnectionFactory
)
