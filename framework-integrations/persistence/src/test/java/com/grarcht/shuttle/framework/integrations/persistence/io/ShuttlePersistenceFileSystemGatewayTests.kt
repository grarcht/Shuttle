package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttleFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttlePersistenceFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.Serializable


class ShuttlePersistenceFileSystemGatewayTests {
    private val directoryName = "cargo"
    private val fileName = "paintColor"

    @Test
    fun verifyWriteToFile() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        val gateway: ShuttleFileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)
        val paintColor = PaintColor("Blue")
        val filePath = gateway.writeToFile(directoryName, fileName, paintColor) ?: ""
        Assertions.assertTrue(filePath.isNotEmpty())
    }

    @Test
    fun verifyReadFromFile() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        val gateway: ShuttleFileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)
        val paintColor = PaintColor("Blue")
        val filePath = gateway.writeToFile(directoryName, fileName, paintColor) ?: ""
        val deserializedPaintColor = gateway.readFromFile(filePath) as PaintColor
        Assertions.assertEquals("Blue", deserializedPaintColor.color)
    }

    @Test
    fun verifyDeleteFile() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        val gateway: ShuttleFileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)
        val paintColor = PaintColor("Blue")
        val filePath = gateway.writeToFile(directoryName, fileName, paintColor) ?: ""
        val result: ShuttlePersistenceRemoveCargoResult = gateway.deleteFile(filePath)
        Assertions.assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }

    @Test
    fun verifyDeleteAllFiles() {
        val fileFactory: ShuttleFileFactory = ShuttlePersistenceFileFactory()
        val gateway: ShuttleFileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)
        val paintColor = PaintColor("Blue")
        gateway.writeToFile(directoryName, fileName, paintColor)
        val result: ShuttlePersistenceRemoveCargoResult = gateway.deleteAllFilesAt(directoryName)
        Assertions.assertEquals(ShuttlePersistenceRemoveCargoResult.Removed, result)
    }

    private class PaintColor(val color: String?) : Serializable
}