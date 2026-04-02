package com.grarcht.shuttle.framework.integrations.extensions.room

import android.app.Application
import android.content.Context
import androidx.room.InvalidationTracker
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A minimal concrete subclass of [ShuttleRoomDataDb] used to exercise abstract-class code
 * paths (such as the [ShuttleRoomDataDb.shuttleDataAccessObject] lazy property) without
 * requiring a live SQLite database.
 */
private class TestShuttleRoomDataDb(
    private val innerDao: ShuttleRoomDao.Dao
) : ShuttleRoomDataDb() {
    override fun getShuttleDao(): ShuttleRoomDao.Dao = innerDao
    override fun createInvalidationTracker(): InvalidationTracker =
        InvalidationTracker(this, emptyMap(), emptyMap())
    override fun clearAllTables() = Unit
}

/**
 * Verifies [ShuttleRoomDataDb] companion-object singleton behaviour and the
 * [ShuttleRoomDataDb.shuttleDataAccessObject] lazy property.
 *
 * Kotlin stores `@Volatile private var INSTANCE` (declared in a companion object) as a
 * private static field on the outer class, accessible via [Class.getDeclaredField].
 */
class ShuttleRoomDataDbTest {

    private val instanceField = ShuttleRoomDataDb::class.java
        .getDeclaredField("INSTANCE")
        .also { it.isAccessible = true }

    @BeforeEach
    fun resetInstance() {
        instanceField.set(null, null)
    }

    @AfterEach
    fun tearDown() {
        instanceField.set(null, null)
    }

    // -------------------------------------------------------------------------
    // getInstance – singleton semantics
    // -------------------------------------------------------------------------

    @Test
    fun verifyGetInstanceCreatesNewDbWhenNoneExists() {
        val context = mock<Context>()
        val appContext = mock<Application>()
        whenever(context.applicationContext).thenReturn(appContext)
        val config = ShuttleRoomDbConfig(context)
        val mockDb = mock<ShuttleRoomDataDb>()
        val mockFactory = mock<ShuttleRoomDbFactory>()
        whenever(mockFactory.createDb(config)).thenReturn(mockDb)

        val result = ShuttleRoomDataDb.getInstance(config, mockFactory)

        assertNotNull(result)
        assertEquals(mockDb, result)
    }

    @Test
    fun verifyGetInstanceReturnsCachedInstanceWhenAlreadyExists() {
        val context = mock<Context>()
        val config = ShuttleRoomDbConfig(context)
        val cachedDb = mock<ShuttleRoomDataDb>()
        instanceField.set(null, cachedDb)

        val result = ShuttleRoomDataDb.getInstance(config)

        assertEquals(cachedDb, result)
    }

    @Test
    fun verifyGetInstanceReturnsSameInstanceOnSubsequentCalls() {
        val context = mock<Context>()
        val appContext = mock<Application>()
        whenever(context.applicationContext).thenReturn(appContext)
        val config = ShuttleRoomDbConfig(context)
        val mockDb = mock<ShuttleRoomDataDb>()
        val mockFactory = mock<ShuttleRoomDbFactory>()
        whenever(mockFactory.createDb(config)).thenReturn(mockDb)

        val first = ShuttleRoomDataDb.getInstance(config, mockFactory)
        val second = ShuttleRoomDataDb.getInstance(config, mockFactory)

        assertEquals(first, second)
    }

    // -------------------------------------------------------------------------
    // shuttleDataAccessObject – lazy property body
    // -------------------------------------------------------------------------

    @Test
    fun verifyShuttleDataAccessObjectWrapsInnerDaoInShuttleRoomDao() {
        val mockInnerDao = mock<ShuttleRoomDao.Dao>()
        val db = TestShuttleRoomDataDb(mockInnerDao)

        val accessObject: ShuttleDataAccessObject = db.shuttleDataAccessObject

        assertNotNull(accessObject)
        assertTrue(accessObject is ShuttleRoomDao)
    }
}
