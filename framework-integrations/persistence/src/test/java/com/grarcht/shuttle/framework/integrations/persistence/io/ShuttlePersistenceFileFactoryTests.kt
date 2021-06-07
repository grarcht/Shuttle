package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import java.io.File

class ShuttlePersistenceFileFactoryTests {
    private var file: File? = null

    @AfterAll
    fun tearDown() {
        file?.deleteRecursively()
    }

    @Test
    fun verifyCreateFileSucceeds() {
        val factory = ShuttlePersistenceFileFactory()
        val directoryName = "testDir"
        val fileName = "testFile"
        val file = factory.createFile(directoryName, fileName)
        Assert.assertNotNull(file)
    }
}