package com.grarcht.shuttle.framework.validator

import android.os.Bundle
import android.os.Message
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID

/**
 *
 */
class ShuttleServiceMessageValidator : ShuttleValidator<Message> {
    override fun validate(data: Message): Boolean {
        return if (data.data != null && data.data is Bundle) {
            val bundle = data.data
            val cargoId = bundle.getString(CARGO_ID_KEY, NO_CARGO_ID)
            cargoId != NO_CARGO_ID
        } else {
            false
        }
    }
}