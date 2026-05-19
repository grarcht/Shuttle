package com.grarcht.shuttle.framework.integrations.extensions.room

import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val CARGO_ID = "testCargoId"
private const val FILE_PATH = "/test/path/cargoId"
private const val EXPECTED_ITEM_COUNT = 5
private const val EXPECTED_INSERT_ROW_ID = 1L
private const val EXPECTED_DELETE_COUNT = 1
private const val EXPECTED_DELETE_ALL_COUNT = 3
private const val EXPECTED_ORPHAN_DELETE_COUNT = 2
private const val OLD_TIMESTAMP = 1_000L
private const val FUTURE_TIMESTAMP = Long.MAX_VALUE

/**
 * Verifies the functionality of [ShuttleRoomDao]. ShuttleRoomDao adapts the Room inner DAO to
 * the framework's ShuttleDataAccessObject interface, delegating all CRUD operations while
 * enforcing type safety for insert operations. Without it, the Room persistence integration would
 * have no bridge between the framework's data-access contract and Room's generated DAO.
 */
class ShuttleRoomDaoTest {

    private lateinit var mockInnerDao: ShuttleRoomDao.Dao
    private lateinit var shuttleRoomDao: ShuttleRoomDao

    @BeforeEach
    fun setUp() {
        mockInnerDao = mock()
        shuttleRoomDao = ShuttleRoomDao(mockInnerDao)
    }

    @Test
    fun verifyGetNumberOfCargoItems() {
        whenever(mockInnerDao.getNumberOfCargoItems()).thenReturn(EXPECTED_ITEM_COUNT)
        val result = shuttleRoomDao.getNumberOfCargoItems()
        assertEquals(EXPECTED_ITEM_COUNT, result)
    }

    @Test
    fun verifyGetCargoByReturnsMatchingData() = runTest {
        val expectedData = ShuttleRoomData(CARGO_ID, FILE_PATH)
        whenever(mockInnerDao.getCargoById(CARGO_ID)).thenReturn(expectedData)
        val result = shuttleRoomDao.getCargoBy(CARGO_ID)
        assertEquals(expectedData, result)
    }

    @Test
    fun verifyGetCargoByReturnsNullWhenNotFound() = runTest {
        whenever(mockInnerDao.getCargoById(CARGO_ID)).thenReturn(null)
        val result = shuttleRoomDao.getCargoBy(CARGO_ID)
        assertNull(result)
    }

    @Test
    fun verifyInsertCargoWithShuttleRoomDataDelegatesToDao() {
        val data = ShuttleRoomData(CARGO_ID, FILE_PATH)
        whenever(mockInnerDao.insertCargo(data)).thenReturn(EXPECTED_INSERT_ROW_ID)
        val result = shuttleRoomDao.insertCargo(data)
        assertEquals(EXPECTED_INSERT_ROW_ID, result)
    }

    @Test
    fun verifyInsertCargoWithNonShuttleRoomDataReturnsStoreFailed() {
        val nonRoomData = mock<ShuttleDataModel>()
        val result = shuttleRoomDao.insertCargo(nonRoomData)
        assertEquals(ShuttleDataAccessObject.STORE_CARGO_FAILED, result)
    }

    @Test
    fun verifyDeleteCargoByDelegatesToDao() {
        whenever(mockInnerDao.deleteCargoBy(CARGO_ID)).thenReturn(EXPECTED_DELETE_COUNT)
        val result = shuttleRoomDao.deleteCargoBy(CARGO_ID)
        assertEquals(EXPECTED_DELETE_COUNT, result)
    }

    @Test
    fun verifyDeleteAllCargoDataDelegatesToDao() {
        whenever(mockInnerDao.deleteAllCargoData()).thenReturn(EXPECTED_DELETE_ALL_COUNT)
        val result = shuttleRoomDao.deleteAllCargoData()
        assertEquals(EXPECTED_DELETE_ALL_COUNT, result)
    }

    @Test
    fun verifyGetCargoOlderThanReturnsMatchingItems() = runTest {
        val oldItem = ShuttleRoomData(CARGO_ID, FILE_PATH)
        whenever(mockInnerDao.getCargoOlderThan(FUTURE_TIMESTAMP)).thenReturn(listOf(oldItem))
        val result = shuttleRoomDao.getCargoOlderThan(FUTURE_TIMESTAMP)
        assertEquals(listOf(oldItem), result)
    }

    @Test
    fun verifyGetCargoOlderThanReturnsEmptyListWhenNoneMatch() = runTest {
        whenever(mockInnerDao.getCargoOlderThan(OLD_TIMESTAMP)).thenReturn(emptyList())
        val result = shuttleRoomDao.getCargoOlderThan(OLD_TIMESTAMP)
        assertEquals(emptyList<ShuttleRoomData>(), result)
    }

    @Test
    fun verifyDeleteCargoOlderThanDelegatesToDao() {
        whenever(mockInnerDao.deleteCargoOlderThan(FUTURE_TIMESTAMP)).thenReturn(EXPECTED_ORPHAN_DELETE_COUNT)
        val result = shuttleRoomDao.deleteCargoOlderThan(FUTURE_TIMESTAMP)
        assertEquals(EXPECTED_ORPHAN_DELETE_COUNT, result)
    }
}
