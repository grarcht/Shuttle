package com.grarcht.shuttle.framework.os

import com.grarcht.shuttle.framework.app.ShuttleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

/**
 * Verifies the functionality of [ShuttleBinder]. ShuttleBinder is the IBinder implementation
 * used by local bound services to expose the ShuttleService to connecting clients. If it did not
 * correctly return the bound service, client components would be unable to obtain a reference to
 * the service after binding.
 */
class ShuttleBinderTests {

    @Test
    fun verifyGetServiceReturnsTheBoundService() {
        val service = mock(ShuttleService::class.java)
        val binder = ShuttleBinder(service)

        assertEquals(service, binder.getService())
    }
}
