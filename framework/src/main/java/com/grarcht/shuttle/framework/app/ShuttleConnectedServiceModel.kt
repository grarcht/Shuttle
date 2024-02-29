package com.grarcht.shuttle.framework.app

import android.os.Messenger

data class ShuttleConnectedServiceModel<S : ShuttleService>(
    var localService: S? = null,
    var ipcMessenger: Messenger? = null
)
