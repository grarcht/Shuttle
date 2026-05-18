package com.grarcht.shuttle.demo.mvvmwithaservice.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Verifies the functionality of [MessagingAction], including the integer action values assigned
 * to each messaging constant and the [MessagingAction.getActionWith] factory method's mapping
 * and fallback behavior for unmapped values.
 */
class MessagingActionTest {

    @Test
    fun verifyActionValues() {
        assertAll(
            { assertEquals(0, MessagingAction.TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE.actionValue) },
            { assertEquals(1, MessagingAction.TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE.actionValue) },
            { assertEquals(2, MessagingAction.UNKNOWN_DO_NOT_USE.actionValue) }
        )
    }

    @Test
    fun getActionWithReturnsWithShuttleForValue0() {
        assertEquals(
            MessagingAction.TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE,
            MessagingAction.getActionWith(0)
        )
    }

    @Test
    fun getActionWithReturnsWithoutShuttleForValue1() {
        assertEquals(
            MessagingAction.TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE,
            MessagingAction.getActionWith(1)
        )
    }

    @Test
    fun getActionWithReturnsUnknownForUnmappedValue() {
        assertEquals(
            MessagingAction.UNKNOWN_DO_NOT_USE,
            MessagingAction.getActionWith(99)
        )
    }
}
