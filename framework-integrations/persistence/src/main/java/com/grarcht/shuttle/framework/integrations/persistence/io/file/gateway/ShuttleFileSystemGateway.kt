package com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway

import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import java.io.Serializable

/**
 * A gateway contractual interface for writing to, reading from, and deleting files from the app's
 * file system space.  This class uses the Gateway Design Pattern.  For more information on this design
 * pattern, refer to: <a href="https://microservices.io/patterns/apigateway.html">Gateway Design Pattern</a>
 */
interface ShuttleFileSystemGateway {
    /**
     * Reads the bytes from the file at the [filePath].
     * @param filePath for the file containing the bytes to read
     * @return the byte array of read bytes or null if they could not be retrieved.
     */
    fun readFromFile(filePath: String): Serializable?

    /**
     * Writes the [serializable] to the file, named by [fileName], in the directory, named by [directoryName].
     * @param directoryName where the file should be written to
     * @param fileName to name the file
     * @param serializable to write to the file
     * @return path to the file for which the [serializable] were written to
     */
    fun writeToFile(directoryName: String, fileName: String, serializable: Serializable): String?

    /**
     * Deletes all files at the [directoryPath].
     * @param directoryPath for the files to be deleted
     * @return true if the files could be deleted
     */
    fun deleteAllFilesAt(directoryPath: String): ShuttlePersistenceRemoveCargoResult

    /**
     * Deletes a file at the [filePath].
     * @param filePath for the file to be deleted
     * @return true if the file could be deleted
     */
    fun deleteFile(filePath: String): ShuttlePersistenceRemoveCargoResult
}
