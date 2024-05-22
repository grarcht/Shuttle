package com.grarcht.shuttle.framework.app

import android.os.Messenger
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnection

/**
 * Holds the service references, emitted from the [ShuttleServiceConnection].
 * @param localService used for transporting cargo with the local service
 * @param ipcMessenger used for transporting cargo with the remote service via IPC
 */
data class ShuttleConnectedServiceModel<S : ShuttleService>(
    var localService: S? = null,
    var ipcMessenger: Messenger? = null
)
