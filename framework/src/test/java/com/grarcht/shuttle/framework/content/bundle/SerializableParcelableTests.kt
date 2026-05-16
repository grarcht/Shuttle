package com.grarcht.shuttle.framework.content.bundle

import android.os.Parcelable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.mock

/**
 * Verifies the functionality of [SerializableParcelable]. SerializableParcelable is a wrapper
 * that allows a Parcelable to be treated as Serializable so it can be stored in the Shuttle
 * warehouse. Without it, Parcelable data could not participate in the cargo transport pipeline.
 */
class SerializableParcelableTests {

    @Test
    fun verifySerializableParcelableWrapsParcelable() {
        val parcelable = mock(Parcelable::class.java)
        val serializableParcelable = SerializableParcelable(parcelable)
        assertAll(
            { assertNotNull(serializableParcelable) },
            { assertEquals(parcelable, serializableParcelable.parcelable) }
        )
    }
}
