package com.grarcht.shuttle.framework.app

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShuttleServiceTypeTests {

    @Test
    fun verifyIsBoundServiceReturnsTrueForAidlType() {
        assertTrue(ShuttleServiceType.BOUND_AIDL.isBoundService())
    }

    @Test
    fun verifyIsBoundServiceReturnsTrueForLocalType() {
        assertTrue(ShuttleServiceType.BOUND_LOCAL.isBoundService())
    }

    @Test
    fun verifyIsBoundServiceReturnsTrueForMessengerType() {
        assertTrue(ShuttleServiceType.BOUND_MESSENGER.isBoundService())
    }

    @Test
    fun verifyIsBoundServiceReturnsFalseForNonBoundType() {
        assertFalse(ShuttleServiceType.NON_BOUND_STARTED.isBoundService())
    }

    @Test
    fun verifyIsAIDLBoundServiceReturnsTrueForAidlType() {
        assertTrue(ShuttleServiceType.BOUND_AIDL.isAIDLBoundService())
    }

    @Test
    fun verifyIsAIDLBoundServiceReturnsFalseForOtherTypes() {
        assertFalse(ShuttleServiceType.BOUND_LOCAL.isAIDLBoundService())
        assertFalse(ShuttleServiceType.BOUND_MESSENGER.isAIDLBoundService())
        assertFalse(ShuttleServiceType.NON_BOUND_STARTED.isAIDLBoundService())
    }

    @Test
    fun verifyIsLocalBoundServiceReturnsTrueForLocalType() {
        assertTrue(ShuttleServiceType.BOUND_LOCAL.isLocalBoundService())
    }

    @Test
    fun verifyIsLocalBoundServiceReturnsFalseForOtherTypes() {
        assertFalse(ShuttleServiceType.BOUND_AIDL.isLocalBoundService())
        assertFalse(ShuttleServiceType.BOUND_MESSENGER.isLocalBoundService())
        assertFalse(ShuttleServiceType.NON_BOUND_STARTED.isLocalBoundService())
    }

    @Test
    fun verifyIsMessengerBoundServiceReturnsTrueForMessengerType() {
        assertTrue(ShuttleServiceType.BOUND_MESSENGER.isMessengerBoundService())
    }

    @Test
    fun verifyIsMessengerBoundServiceReturnsFalseForOtherTypes() {
        assertFalse(ShuttleServiceType.BOUND_AIDL.isMessengerBoundService())
        assertFalse(ShuttleServiceType.BOUND_LOCAL.isMessengerBoundService())
        assertFalse(ShuttleServiceType.NON_BOUND_STARTED.isMessengerBoundService())
    }

    @Test
    fun verifyIsNonBoundServiceReturnsTrueForNonBoundType() {
        assertTrue(ShuttleServiceType.NON_BOUND_STARTED.isNonBoundService())
    }

    @Test
    fun verifyIsNonBoundServiceReturnsFalseForOtherTypes() {
        assertFalse(ShuttleServiceType.BOUND_AIDL.isNonBoundService())
        assertFalse(ShuttleServiceType.BOUND_LOCAL.isNonBoundService())
        assertFalse(ShuttleServiceType.BOUND_MESSENGER.isNonBoundService())
    }
}
