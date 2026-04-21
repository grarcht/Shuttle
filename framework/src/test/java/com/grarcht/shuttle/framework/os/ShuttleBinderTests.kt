package com.grarcht.shuttle.framework.os

import com.grarcht.shuttle.framework.app.ShuttleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ShuttleBinderTests {

    @Test
    fun verifyGetServiceReturnsTheBoundService() {
        val service = mock(ShuttleService::class.java)
        val binder = ShuttleBinder(service)

        assertEquals(service, binder.getService())
    }
}
