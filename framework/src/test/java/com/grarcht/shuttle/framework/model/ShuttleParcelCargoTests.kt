package com.grarcht.shuttle.framework.model

import android.os.Parcel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

private const val CARGO_ID = "cargoId1"
private const val PARCEL_FLAGS = 0

/**
 * Verifies the functionality of [ShuttleParcelCargo]. ShuttleParcelCargo is the Parcelable model
 * used to pass a cargo ID across process boundaries via Android's parcel mechanism. If
 * parcelling or unparcelling the cargo ID failed, IPC-bound services would be unable to identify
 * which warehouse entry to retrieve for a given delivery.
 */
class ShuttleParcelCargoTests {

    @Test
    fun verifyDescribeContentsReturnsFileDescriptor() {
        val cargo = ShuttleParcelCargo(CARGO_ID)
        assertEquals(android.os.Parcelable.CONTENTS_FILE_DESCRIPTOR, cargo.describeContents())
    }

    @Test
    fun verifyWriteToParcelWritesCargoId() {
        val cargo = ShuttleParcelCargo(CARGO_ID)
        val parcel = mock(Parcel::class.java)

        cargo.writeToParcel(parcel, PARCEL_FLAGS)

        verify(parcel).writeString(CARGO_ID)
    }

    @Test
    fun verifyCreateFromParcelReadsCargoId() {
        val parcel = mock(Parcel::class.java)
        whenever(parcel.readString()).thenReturn(CARGO_ID)

        val cargo = ShuttleParcelCargo.createFromParcel(parcel)

        assertAll(
            { assertNotNull(cargo) },
            { assertEquals(CARGO_ID, cargo.cargoId) }
        )
    }

    @Test
    fun verifyCreateFromParcelUsesDefaultCargoIdWhenParcelReturnsNull() {
        val parcel = mock(Parcel::class.java)
        whenever(parcel.readString()).thenReturn(null)

        val cargo = ShuttleParcelCargo.createFromParcel(parcel)

        assertAll(
            { assertNotNull(cargo) },
            { assertEquals(NO_CARGO_ID, cargo.cargoId) }
        )
    }

    @Test
    fun verifyNewArrayCreatesArrayOfCorrectSize() {
        val size = 5
        val array = ShuttleParcelCargo.newArray(size)

        assertAll(
            { assertNotNull(array) },
            { assertEquals(size, array.size) }
        )
    }
}
