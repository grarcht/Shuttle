package com.grarcht.shuttle.framework.validator

import android.os.Bundle
import android.os.Message
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID

/**
 * Validates data.
 */
class ShuttleServiceMessageValidator : ShuttleValidator<Message> {
    /**
     * Validates the [data].
     * @param data to validate
     *
     * @return true if the data is valid.
     */
    override fun validate(data: Message): Boolean {
        val dataBundle: Bundle? = data.data
        return if (dataBundle != null) {
            val cargoId = dataBundle.getString(CARGO_ID_KEY, NO_CARGO_ID)
            cargoId != NO_CARGO_ID
        } else {
            false
        }
    }
}
