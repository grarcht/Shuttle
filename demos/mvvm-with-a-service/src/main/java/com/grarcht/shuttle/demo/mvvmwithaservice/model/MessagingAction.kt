package com.grarcht.shuttle.demo.mvvmwithaservice.model

import android.os.Message

/**
 * Denotes what type of message to send to the [RemoteService].
 *
 * @param actionValue
 */
enum class MessagingAction(val actionValue: Int) {
    TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE(0),
    TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE(1),
    UNKNOWN_DO_NOT_USE(2);

    companion object {
        /**
         * Used to determine flow.
         * @param actionValue the raw value passed in [Message]s
         */
        fun getActionWith(actionValue: Int): MessagingAction {
            return when (actionValue) {
                TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE.actionValue -> TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE
                TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE.actionValue -> TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE
                else -> UNKNOWN_DO_NOT_USE
            }
        }
    }
}