package com.grarcht.shuttle.framework.os.messenger

import android.os.Message
import android.os.Messenger

/**
 * Receives and handles IPC [Message]s from [Messenger]s.
 */
interface ShuttleMessageReceiver {
    /**
     * Override this function to handle IPC messaging.
     *
     * Since this service supports local binding and messenger binding for IPC, and this function is for the
     * latter, then this function is open and not abstract.
     *
     * @param messageWhat see [Message.what]
     * @param msg see [Message]
     */
    fun onReceiveMessage(messageWhat: Int, msg: Message)
}