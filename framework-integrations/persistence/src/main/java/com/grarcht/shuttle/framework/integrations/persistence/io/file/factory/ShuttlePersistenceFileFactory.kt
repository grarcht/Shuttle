package com.grarcht.shuttle.framework.integrations.persistence.io.file.factory

import android.util.Log
import java.io.File

private const val LOG_TAG = "FileFactory"

/**
 * This factory is used to create [File]s.  This class uses the factory design pattern.
 * For more information on this design pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">
 *     Factory Design Pattern</a>
 */
class ShuttlePersistenceFileFactory : ShuttleFileFactory {

    /**
     * Creates a new file.
     * @param directoryName the name for the directory where the file is being created
     * @param fileName the name for the file to create
     * @return the reference to the newly created file or null if it was not created
     */
    override fun createFile(directoryName: String, fileName: String): File? {
        var createdFile: File? = null
        try {
            val directory = File(directoryName)
            if (directory.exists().not()) {
                directory.mkdir()
            }
            createdFile = File("${directory.absolutePath}$fileName")
        } catch (se: SecurityException) {
            Log.wtf(LOG_TAG, " Caught when creating a file.", se)
        }
        return createdFile
    }
}
