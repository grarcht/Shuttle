package com.grarcht.shuttle.framework.integrations.persistence.io.file.factory

import java.io.File

/**
 * This factory contractual interface is used to create [File]s.  This class uses the factory design pattern.
 * For more information on this design pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
interface ShuttleFileFactory {
    /**
     * Creates a new file.
     * @param directoryName the name for the directory where the file is being created
     * @param fileName the name for the file to create
     * @return the reference to the newly created file or null if it was not created
     */
    fun createFile(directoryName: String, fileName: String): File?
}
