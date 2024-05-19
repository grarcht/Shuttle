package com.grarcht.shuttle.framework.app

import android.os.Message
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerFactory
import com.grarcht.shuttle.framework.validator.ShuttleServiceMessageValidator
import com.grarcht.shuttle.framework.validator.ShuttleValidator

/**
 * Used to configure a [ShuttleService].
 *
 * @param serviceName the name of the service
 * @param shuttle used to transport and pickup cargo
 * @param rebindOnUnbind if true, the service will rebind when an unbind occurs
 * @param errorObservable used to observe errors
 * @param bindingType used to determine the binder to create
 * @param messengerFactory creates the service's messenger
 * @param messageValidator validates received messages
 */
data class ShuttleServiceConfig(
    val serviceName: String,
    val shuttle: Shuttle,
    val rebindOnUnbind: Boolean = false,
    val errorObservable: ShuttleVisibilityObservable,
    val bindingType: ShuttleServiceType,
    val messengerFactory: ShuttleMessengerFactory,
    val messageValidator: ShuttleValidator<Message> = ShuttleServiceMessageValidator()
)