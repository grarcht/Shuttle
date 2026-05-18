package com.grarcht.shuttle.demo.mviwithcompose.intent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val TEST_CARGO_ID = "test-cargo-id"

/**
 * Verifies the functionality of [CargoPickupIntent], including the cargo ID held by the
 * [CargoPickupIntent.LoadCargo] data class, value-based equality, and copy semantics.
 */
class CargoPickupIntentTest {

    @Test
    fun loadCargoHoldsCargoId() {
        val intent = CargoPickupIntent.LoadCargo(TEST_CARGO_ID)
        assertEquals(TEST_CARGO_ID, intent.cargoId)
    }

    @Test
    fun loadCargoEqualityIsValueBased() {
        val intent1 = CargoPickupIntent.LoadCargo(TEST_CARGO_ID)
        val intent2 = CargoPickupIntent.LoadCargo(TEST_CARGO_ID)
        assertAll(
            { assertEquals(intent1, intent2) },
            { assertEquals(intent1.hashCode(), intent2.hashCode()) }
        )
    }

    @Test
    fun loadCargoCopyProducesNewInstanceWithUpdatedCargoId() {
        val original = CargoPickupIntent.LoadCargo(TEST_CARGO_ID)
        val copy = original.copy(cargoId = "other-id")
        assertAll(
            { assertEquals("other-id", copy.cargoId) },
            { assertEquals(TEST_CARGO_ID, original.cargoId) }
        )
    }
}
