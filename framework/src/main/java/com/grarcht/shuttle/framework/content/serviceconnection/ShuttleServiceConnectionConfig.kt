package com.grarcht.shuttle.framework.content.serviceconnection

import android.content.Context
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * Configures a [ShuttleServiceConnection].
 *
 * @param context used for connecting to a service
 * @param serviceName used for logging
 * @param errorObservable provides visibility in to possible errors
 * @param useWithIPC true, if this service should be remote and use interprocess communication
 * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
 * @param serviceChannel emits a [ShuttleConnectedServiceModel]
 */
data class ShuttleServiceConnectionConfig<S : ShuttleService>(
    val context: Context?,
    val serviceName: String,
    val errorObservable: ShuttleVisibilityObservable,
    val useWithIPC: Boolean = false,
    val coroutineScope: CoroutineScope,
    val serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
)
