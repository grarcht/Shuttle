package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttleFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttlePersistenceFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.io.ObjectInputStream
import java.nio.file.Files

private const val DIRECTORY_NAME = "cargo"
private const val FILE_NAME = "paintColor"
private const val PAINT_COLOR_BLUE = "Blue"
private const val PAINT_COLOR_RED = "Red"
private const val NON_EXISTENT_PATH = "/definitely/does/not/exist/shuttle_test_path"
private const val ANY_PATH = "/any/path"
private const val SIMULATED_SECURITY_DENIAL = "simulated security denial"
private const val TEMP_FILE_SUFFIX = ".tmp"

/**
 * Verifies the functionality of [ShuttlePersistenceFileSystemGateway]. This gateway handles all
 * file-system I/O for the persistence integration — writing serialized cargo to disk, reading it
 * back, and deleting individual files or entire directories. If any of these operations misbehaved,
 * cargo would be lost, corrupted, or never cleaned up from the device's storage.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttlePersistenceFileSystemGatewayTests {
    private var filePath = ""
    private var gateway: ShuttleFileSystemGateway? = null
    private val paintColor = PaintColor(PAINT_COLOR_BLUE)

    @BeforeAll
    fun runBeforeAllTests() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        gateway = ShuttlePersistenceFileSystemGateway(fileFactory)
    }

    @AfterAll
    fun tearDown() {
        gateway?.deleteFile(filePath)
        File(DIRECTORY_NAME).deleteRecursively()
    }

    // -------------------------------------------------------------------------
    // writeToFile
    // -------------------------------------------------------------------------

    @Test
    fun verifyWriteToFile() {
        filePath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        assertTrue(filePath.isNotEmpty())
    }

    @Test
    fun verifyWriteToFileReturnsExistingPathWhenFileAlreadyExists() {
        val firstPath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        assertTrue(firstPath.isNotEmpty())

        val secondPath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        assertEquals(firstPath, secondPath)
    }

    @Test
    fun verifyWriteToFileHandlesIOException() {
        val tempDir = Files.createTempDirectory("shuttle_ioe_write").toFile()
        try {
            val mockFactory = mock<ShuttleFileFactory>()
            whenever(mockFactory.createFile(any(), any())).thenReturn(tempDir)
            val gatewayWithMockFactory = ShuttlePersistenceFileSystemGateway(mockFactory)

            val uniqueDir = "shuttle_ioe_${System.nanoTime()}"
            val result = gatewayWithMockFactory.writeToFile(uniqueDir, FILE_NAME, PaintColor(PAINT_COLOR_RED))

            assertNotNull(result)
        } finally {
            tempDir.delete()
        }
    }

    // -------------------------------------------------------------------------
    // readFromFile
    // -------------------------------------------------------------------------

    @Test
    fun verifyReadFromFile() {
        filePath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        val deserializedPaintColor = gateway?.readFromFile(filePath) as PaintColor
        assertEquals(PAINT_COLOR_BLUE, deserializedPaintColor.color)
    }

    @Test
    fun verifyReadFromFileReturnsNullWhenFileDoesNotExist() {
        val result = gateway?.readFromFile(NON_EXISTENT_PATH)
        assertNull(result)
    }

    @Test
    fun verifyReadFromFileHandlesIOException() {
        val tempFile = File.createTempFile("shuttle_ioe_read", TEMP_FILE_SUFFIX)
        try {
            tempFile.writeBytes("not valid serialized content".toByteArray())
            val result = gateway?.readFromFile(tempFile.absolutePath)
            assertNull(result)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun verifyReadFromFileHandlesOutOfMemoryError() {
        val tempFile = File.createTempFile("shuttle_oom_read", TEMP_FILE_SUFFIX)
        try {
            Mockito.mockConstruction(ObjectInputStream::class.java) { mock: ObjectInputStream, _ ->
                whenever(mock.readObject()).thenThrow(OutOfMemoryError("simulated OOM"))
            }.use {
                val result = gateway?.readFromFile(tempFile.absolutePath)
                assertNull(result)
            }
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun verifyReadFromFileHandlesSecurityException() {
        val tempFile = File.createTempFile("shuttle_sec_read", TEMP_FILE_SUFFIX)
        try {
            Mockito.mockConstruction(ObjectInputStream::class.java) { mock: ObjectInputStream, _ ->
                whenever(mock.readObject()).thenThrow(SecurityException(SIMULATED_SECURITY_DENIAL))
            }.use {
                val result = gateway?.readFromFile(tempFile.absolutePath)
                assertNull(result)
            }
        } finally {
            tempFile.delete()
        }
    }

    // -------------------------------------------------------------------------
    // deleteFile
    // -------------------------------------------------------------------------

    @Test
    fun verifyDeleteFile() {
        filePath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        val result: ShuttlePersistenceRemoveCargoResult =
            gateway?.deleteFile(filePath) ?: ShuttlePersistenceRemoveCargoResult.UnableToRemove
        assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }

    @Test
    fun verifyDeleteFileReturnsDoesNotExistWhenFileNotFound() {
        val result = gateway?.deleteFile(NON_EXISTENT_PATH)
        assertEquals(ShuttlePersistenceRemoveCargoResult.DoesNotExist, result)
    }

    @Test
    fun verifyDeleteFileReturnsUnableToRemoveWhenDeletionFails() {
        val mockFile = mock<File>()
        whenever(mockFile.exists()).thenReturn(true)
        whenever(mockFile.delete()).thenReturn(false)
        val testGateway = ShuttlePersistenceFileSystemGateway(
            ShuttlePersistenceFileFactory()
        ) { _ -> mockFile }

        val result = testGateway.deleteFile(ANY_PATH)

        assertEquals(ShuttlePersistenceRemoveCargoResult.UnableToRemove, result)
    }

    @Test
    fun verifyDeleteFileHandlesSecurityException() {
        val testGateway = ShuttlePersistenceFileSystemGateway(
            ShuttlePersistenceFileFactory()
        ) { _ -> throw java.lang.SecurityException(SIMULATED_SECURITY_DENIAL) }

        val result = testGateway.deleteFile(ANY_PATH)

        assertEquals(ShuttlePersistenceRemoveCargoResult.UnableToRemove, result)
    }

    // -------------------------------------------------------------------------
    // deleteAllFilesAt
    // -------------------------------------------------------------------------

    @Test
    fun verifyDeleteAllFiles() {
        filePath = gateway?.writeToFile(DIRECTORY_NAME, FILE_NAME, paintColor) ?: ""
        val result: ShuttlePersistenceRemoveCargoResult =
            gateway?.deleteAllFilesAt(DIRECTORY_NAME) ?: ShuttlePersistenceRemoveCargoResult.UnableToRemove
        assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }

    @Test
    fun verifyDeleteAllFilesReturnsDoesNotExistWhenDirectoryNotFound() {
        val result = gateway?.deleteAllFilesAt(NON_EXISTENT_PATH)
        assertEquals(ShuttlePersistenceRemoveCargoResult.DoesNotExist, result)
    }

    @Test
    fun verifyDeleteAllFilesReturnsUnableToRemoveWhenDeletionFails() {
        val grandparent = Files.createTempDirectory("shuttle_gp").toFile()
        val dirToDelete = File(grandparent, "toDelete").also { it.mkdir() }
        File(dirToDelete, "content.txt").createNewFile()
        grandparent.setWritable(false, false)
        try {
            val result = gateway?.deleteAllFilesAt(dirToDelete.absolutePath)
            assertEquals(ShuttlePersistenceRemoveCargoResult.UnableToRemove, result)
        } finally {
            grandparent.setWritable(true, false)
            grandparent.deleteRecursively()
        }
    }

    @Test
    fun verifyDeleteAllFilesHandlesSecurityException() {
        val testGateway = ShuttlePersistenceFileSystemGateway(
            ShuttlePersistenceFileFactory()
        ) { _ -> throw java.lang.SecurityException(SIMULATED_SECURITY_DENIAL) }

        val result = testGateway.deleteAllFilesAt(ANY_PATH)

        assertEquals(ShuttlePersistenceRemoveCargoResult.UnableToRemove, result)
    }
}
