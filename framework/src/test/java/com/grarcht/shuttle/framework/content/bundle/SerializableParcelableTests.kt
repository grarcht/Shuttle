package com.grarcht.shuttle.framework.content.bundle

import android.os.Parcelable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class SerializableParcelableTests {

    @Test
    fun verifySerializableParcelableWrapsParcelable() {
        val parcelable = mock(Parcelable::class.java)
        val serializableParcelable = SerializableParcelable(parcelable)
        assertNotNull(serializableParcelable)
        assertEquals(parcelable, serializableParcelable.parcelable)
    }
}
