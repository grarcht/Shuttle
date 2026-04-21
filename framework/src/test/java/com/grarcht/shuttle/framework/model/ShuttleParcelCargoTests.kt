package com.grarcht.shuttle.framework.model

import android.os.Parcel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

private const val CARGO_ID = "cargoId1"
private const val PARCEL_FLAGS = 0

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

        assertNotNull(cargo)
        assertEquals(CARGO_ID, cargo.cargoId)
    }

    @Test
    fun verifyCreateFromParcelUsesDefaultCargoIdWhenParcelReturnsNull() {
        val parcel = mock(Parcel::class.java)
        whenever(parcel.readString()).thenReturn(null)

        val cargo = ShuttleParcelCargo.createFromParcel(parcel)

        assertNotNull(cargo)
        assertEquals(NO_CARGO_ID, cargo.cargoId)
    }

    @Test
    fun verifyNewArrayCreatesArrayOfCorrectSize() {
        val size = 5
        val array = ShuttleParcelCargo.newArray(size)

        assertNotNull(array)
        assertEquals(size, array.size)
    }
}
