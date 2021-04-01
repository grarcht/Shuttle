package com.grarcht.shuttle.framework.integrations.extensions.room

import android.app.Application
import android.content.Context
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Verifies the [ShuttleRoomDbFactory] functionality.
 */
class ShuttleRoomDbFactoryTest {
    private val dbFactory = ShuttleRoomDbFactory()

    @Test
    fun verifyCreationOfTheDb() {
        val context = mock(Context::class.java)
        val applicationContext = mock(Application::class.java)

        `when`(context.applicationContext).thenReturn(applicationContext)

        val config = ShuttleRoomDbConfig(context)
        val db = dbFactory.createDb(config)

        assertNotNull(db)

        db.close()
    }

}