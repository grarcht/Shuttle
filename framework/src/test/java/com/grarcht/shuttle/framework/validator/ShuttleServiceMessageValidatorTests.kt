package com.grarcht.shuttle.framework.validator

import android.os.Message
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.bundle.MockBundleFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ShuttleServiceMessageValidatorTests {
    private val validator = ShuttleServiceMessageValidator()

    @Test
    fun verifyMessageDataIsValid() {
        val validCargoId = "Cargo1"
        val message = mock(Message::class.java)
        val data = MockBundleFactory().create()
        data.putString(CARGO_ID_KEY, validCargoId)

        `when`(message.data).thenReturn(data)

        val isValid = validator.validate(message)
        Assertions.assertTrue(isValid)
    }

    @Test
    fun verifyMessageWithNoCargoIdKeyIsInvalid() {
        val validCargoId = NO_CARGO_ID
        val message = mock(Message::class.java)
        val data = MockBundleFactory().create()
        data.putString(CARGO_ID_KEY, validCargoId)

        `when`(message.data).thenReturn(data)

        val isValid = validator.validate(message)
        Assertions.assertFalse(isValid)
    }

    @Test
    fun verifyMessageWithNoCargoIdAddedIsInvalid() {
        val message = Message()

        val isValid = validator.validate(message)

        Assertions.assertFalse(isValid)
    }
}