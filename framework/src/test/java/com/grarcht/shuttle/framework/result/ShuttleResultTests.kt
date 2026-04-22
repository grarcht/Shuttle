package com.grarcht.shuttle.framework.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val CARGO_ID = "cargoId1"

/**
 * Verifies the functionality of [ShuttlePickupCargoResult], [ShuttleRemoveCargoResult], and
 * [ShuttleStoreCargoResult]. These sealed result classes communicate the state of cargo
 * operations (pickup, removal, and storage) through coroutine channels. If their constructors or
 * property accessors were broken, consumers would be unable to distinguish between operation
 * states and handle them correctly.
 */
class ShuttleResultTests {

    @Test
    fun verifyNotPickingUpCargoYetIsInstantiable() {
        val result = ShuttlePickupCargoResult.NotPickingUpCargoYet
        assertNotNull(result)
    }

    @Test
    fun verifyNotRemovingCargoYetHoldsCargoId() {
        val result = ShuttleRemoveCargoResult.NotRemovingCargoYet(CARGO_ID)
        assertAll(
            { assertNotNull(result) },
            { assertEquals(CARGO_ID, result.cargoId) }
        )
    }

    @Test
    fun verifyNotStoringCargoYetHoldsCargoId() {
        val result = ShuttleStoreCargoResult.NotStoringCargoYet(CARGO_ID)
        assertAll(
            { assertNotNull(result) },
            { assertEquals(CARGO_ID, result.cargoId) }
        )
    }
}
