package com.grarcht.shuttle.framework.integrations.persistence.io

import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import org.junit.Assert
import org.junit.Test

class ShuttlePersistenceFileFactoryTests {

    @Test
    fun verifyCreateFileSucceeds() {
        val factory = ShuttlePersistenceFileFactory()
        val directoryName = "testDir"
        val fileName = "testFile"
        val file = factory.createFile(directoryName, fileName)
        Assert.assertNotNull(file)
    }
}