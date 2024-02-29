package com.grarcht.shuttle.framework.app

import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerFactory

data class ShuttleServiceConfig(
    val serviceName: String,
    val shuttle: Shuttle,
    val rebindOnUnbind: Boolean = false,
    val errorObservable: ShuttleErrorObservable,
    val bindingType: ShuttleServiceBindingType,
    val messengerFactory: ShuttleMessengerFactory
)