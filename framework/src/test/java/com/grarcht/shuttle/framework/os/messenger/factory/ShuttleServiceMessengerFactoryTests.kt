package com.grarcht.shuttle.framework.os.messenger.factory

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessageReceiver
import com.grarcht.shuttle.framework.validator.ShuttleValidator
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

private const val SERVICE_NAME = "TestService"

class ShuttleServiceMessengerFactoryTests {

    @Test
    fun verifyCreateMessengerReturnsShuttleMessengerDecorator() {
        val looper = mock<Looper>()
        val receiver = mock<ShuttleMessageReceiver>()
        val observable = mock<ShuttleVisibilityObservable>()
        val validator = mock<ShuttleValidator<Message>>()
        val factory = ShuttleServiceMessengerFactory()

        Mockito.mockConstruction(Messenger::class.java).use { _ ->
            Mockito.mockConstruction(Handler::class.java).use { _ ->
                val decorator = factory.createMessenger(
                    looper = looper,
                    serviceName = SERVICE_NAME,
                    messageReceiver = receiver,
                    errorObservable = observable,
                    messageValidator = validator
                )
                assertNotNull(decorator)
            }
        }
    }
}
