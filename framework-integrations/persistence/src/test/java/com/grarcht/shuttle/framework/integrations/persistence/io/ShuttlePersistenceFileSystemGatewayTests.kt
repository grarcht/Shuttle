package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttleFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttlePersistenceFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.io.Serializable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttlePersistenceFileSystemGatewayTests {
    private val directoryName = "cargo"
    private val fileName = "paintColor"
    private var filePath = ""
    private var gateway: ShuttleFileSystemGateway? = null
    private val paintColor = PaintColor("Blue")

    @BeforeAll
    fun runBeforeAllTests() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        gateway = ShuttlePersistenceFileSystemGateway(fileFactory)
    }

    @AfterAll
    fun tearDown() {
        gateway?.deleteFile(filePath)
        File(directoryName).deleteRecursively()
    }

    @Test
    fun verifyWriteToFile() {
        filePath = gateway?.writeToFile(directoryName, fileName, paintColor) ?: ""
        Assertions.assertTrue(filePath.isNotEmpty())
    }

    @Test
    fun verifyReadFromFile() {
        filePath = gateway?.writeToFile(directoryName, fileName, paintColor) ?: ""
        val deserializedPaintColor = gateway?.readFromFile(filePath) as PaintColor
        Assertions.assertEquals("Blue", deserializedPaintColor.color)
    }

    @Test
    fun verifyDeleteFile() {
        filePath = gateway?.writeToFile(directoryName, fileName, paintColor) ?: ""
        val result: ShuttlePersistenceRemoveCargoResult =
            gateway?.deleteFile(filePath) ?: ShuttlePersistenceRemoveCargoResult.UnableToRemove
        Assertions.assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }

    @Test
    fun verifyDeleteAllFiles() {
        filePath = gateway?.writeToFile(directoryName, fileName, paintColor) ?: ""
        val result: ShuttlePersistenceRemoveCargoResult =
            gateway?.deleteAllFilesAt(directoryName) ?: ShuttlePersistenceRemoveCargoResult.UnableToRemove
        Assertions.assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }
}
