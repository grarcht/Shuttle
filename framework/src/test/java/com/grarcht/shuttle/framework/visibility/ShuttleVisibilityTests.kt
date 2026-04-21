package com.grarcht.shuttle.framework.visibility

import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.error.ShuttleServiceError
import com.grarcht.shuttle.framework.visibility.information.ShuttleVisibilityFeedback
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

private const val SERVICE_NAME = "TestService"
private const val ERROR_MESSAGE = "An error occurred"
private const val CONTEXT = "TestContext"
private const val INFO_MESSAGE = "Info message"

class ShuttleVisibilityTests {

    // -------------------------------------------------------------------------
    // ShuttleDefaultError.ObservedError
    // -------------------------------------------------------------------------

    @Test
    fun verifyObservedErrorHoldsValues() {
        val throwable = RuntimeException(ERROR_MESSAGE)
        val error = ShuttleDefaultError.ObservedError(CONTEXT, ERROR_MESSAGE, throwable)

        assertNotNull(error)
        assertEquals(CONTEXT, error.context)
        assertEquals(ERROR_MESSAGE, error.errorMessage)
        assertEquals(throwable, error.error)
    }

    // -------------------------------------------------------------------------
    // ShuttleServiceError variants
    // -------------------------------------------------------------------------

    @Test
    fun verifyConnectToServiceErrorHoldsValues() {
        val throwable = RuntimeException(ERROR_MESSAGE)
        val error = ShuttleServiceError.ConnectToServiceError(SERVICE_NAME, "state", ERROR_MESSAGE, throwable)

        assertNotNull(error)
        assertEquals(SERVICE_NAME, error.serviceName)
        assertEquals(ERROR_MESSAGE, error.errorMessage)
        assertEquals(throwable, error.error)
    }

    @Test
    fun verifyDisconnectFromServiceErrorHoldsValues() {
        val throwable = RuntimeException(ERROR_MESSAGE)
        val error = ShuttleServiceError.DisconnectFromServiceError(SERVICE_NAME, Unit, ERROR_MESSAGE, throwable)

        assertNotNull(error)
        assertEquals(SERVICE_NAME, error.serviceName)
        assertEquals(ERROR_MESSAGE, error.errorMessage)
        assertEquals(throwable, error.error)
    }

    @Test
    fun verifyHandleMessageErrorHoldsValues() {
        val throwable = RuntimeException(ERROR_MESSAGE)
        val error = ShuttleServiceError.HandleMessageError(SERVICE_NAME, 42, ERROR_MESSAGE, throwable)

        assertNotNull(error)
        assertEquals(SERVICE_NAME, error.serviceName)
        assertEquals(42, error.data)
        assertEquals(ERROR_MESSAGE, error.errorMessage)
        assertEquals(throwable, error.error)
    }

    @Test
    fun verifyGeneralErrorHoldsValues() {
        val throwable = RuntimeException(ERROR_MESSAGE)
        val error = ShuttleServiceError.GeneralError(SERVICE_NAME, null, ERROR_MESSAGE, throwable)

        assertNotNull(error)
        assertEquals(SERVICE_NAME, error.serviceName)
        assertEquals(ERROR_MESSAGE, error.errorMessage)
        assertEquals(throwable, error.error)
    }

    // -------------------------------------------------------------------------
    // ShuttleVisibilityFeedback.Information
    // -------------------------------------------------------------------------

    @Test
    fun verifyInformationHoldsValues() {
        val info = ShuttleVisibilityFeedback.Information<String>(CONTEXT, "data", INFO_MESSAGE)

        assertNotNull(info)
        assertEquals(CONTEXT, info.context)
        assertEquals("data", info.data)
        assertEquals(INFO_MESSAGE, info.message)
    }
}
