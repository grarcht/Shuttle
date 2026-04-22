package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

private const val DIRECTORY_NAME = "testDir"
private const val FILE_NAME = "testFile"

/**
 * Verifies the functionality of [ShuttlePersistenceFileFactory]. ShuttlePersistenceFileFactory
 * creates the files on disk used to persist cargo payloads between screens. If file creation
 * failed or exceptions were not handled, the persistence layer would be unable to write cargo and
 * all store operations would silently fail.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttlePersistenceFileFactoryTests {
    private var file: File? = null

    @AfterAll
    fun tearDown() {
        file?.deleteRecursively()
    }

    @Test
    fun verifyCreateFileSucceeds() {
        val factory = ShuttlePersistenceFileFactory()
        file = factory.createFile(DIRECTORY_NAME, FILE_NAME)
        Assert.assertNotNull(file)
    }

    @Test
    fun verifyCreateFileHandlesSecurityException() {
        val mockFile = mock<File>()
        whenever(mockFile.exists()).thenReturn(false)
        whenever(mockFile.mkdir()).thenThrow(SecurityException("simulated security denial"))
        val factory = ShuttlePersistenceFileFactory { _ -> mockFile }

        val result = factory.createFile(DIRECTORY_NAME, FILE_NAME)

        assertNull(result)
    }
}
