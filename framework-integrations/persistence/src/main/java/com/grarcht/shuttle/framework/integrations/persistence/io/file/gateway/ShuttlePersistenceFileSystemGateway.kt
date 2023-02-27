package com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway

import android.util.Log
import com.grarcht.shuttle.framework.integrations.persistence.io.closeQuietly
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttleFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

private const val LOG_TAG = "FileSystemGateway"

/**
 * A gateway for writing to, reading from, and deleting files from the app's
 * file system space.  This class uses the Gateway Design Pattern.  For
 * more information on this design
 * pattern, refer to: <a href="https://microservices.io/patterns/apigateway.html">
 *     Gateway Design Pattern</a>
 */
class ShuttlePersistenceFileSystemGateway(
    private val fileFactory: ShuttleFileFactory
) : ShuttleFileSystemGateway {

    /**
     * Reads the bytes from the file at the [filePath].
     * @param filePath for the file containing the bytes to read
     * @return the byte array of read bytes or null if they could not be retrieved.
     */
    override fun readFromFile(filePath: String): Serializable? {
        val file = File(filePath)

        if (file.exists().not()) {
            return null
        }

        var serializable: Serializable? = null
        var ois: ObjectInputStream? = null
        try {
            val fis = FileInputStream(file)
            ois = ObjectInputStream(fis)
            serializable = ois.readObject() as Serializable?
        } catch (ioe: IOException) {
            Log.e(LOG_TAG, "Caught when reading from a file.", ioe)
        } catch (oome: OutOfMemoryError) {
            Log.e(LOG_TAG, "Caught when reading from a file.", oome)
        } catch (se: SecurityException) {
            Log.wtf(LOG_TAG, "Caught when reading from a file.", se)
        } finally {
            ois?.closeQuietly()
        }

        return serializable
    }

    /**
     * Writes the [serializable] to the file, named by [fileName], in the directory, named by [directoryName].
     * @param directoryName where the file should be written to
     * @param fileName to name the file
     * @param serializable to write to the file
     * @return path to the file for which the [serializable] were written to
     */
    override fun writeToFile(
        directoryName: String,
        fileName: String,
        serializable: Serializable
    ): String? {
        var file: File? = File("$directoryName/$fileName")


        // DO NOT REMOVE
        // This condition guards against modifying the data underneath the hood.  If it is modified, resulting parcels
        // from unmarshaling can be unreadable.  When using Google's Navigation Architecture component, there are have
        // been documented and first hand experience bugs with start destinations getting launched more than once.  If
        // that were to happen and a file were to have already been written to, re-writing to the file would make it
        // unreadable.
        if (file?.exists() == true) {
            return file.absolutePath
        }

        file = fileFactory.createFile(directoryName, "/$fileName")

        var oos: ObjectOutputStream? = null
        try {
            val fos = FileOutputStream(file)
            oos = ObjectOutputStream(fos)
            oos.writeObject(serializable)
            oos.flush()
        } catch (ioe: IOException) {
            Log.wtf(LOG_TAG, "Caught when writing to a file.", ioe)
        } finally {
            oos?.closeQuietly()
        }

        return file?.absolutePath
    }

    /**
     * Deletes all files at the [directoryPath].
     * @param directoryPath for the files to be deleted
     * @return true if the files could be deleted
     */
    override fun deleteAllFilesAt(directoryPath: String): ShuttlePersistenceRemoveCargoResult {
        var result: ShuttlePersistenceRemoveCargoResult = ShuttlePersistenceRemoveCargoResult.UnableToRemove
        @Suppress("SwallowedException")
        try {
            val file = File(directoryPath)
            result = if (file.exists().not()) {
                ShuttlePersistenceRemoveCargoResult.DoesNotExist
            } else {
                val deleted = file.deleteRecursively()
                if (deleted) {
                    ShuttlePersistenceRemoveCargoResult.Removed
                } else {
                    ShuttlePersistenceRemoveCargoResult.UnableToRemove
                }
            }
        } catch (se: SecurityException) {
            // This shouldn't happen
            Log.wtf(LOG_TAG, "Caught when deleting a file.")
        }
        return result
    }

    /**
     * Deletes a file at the [filePath].
     * @param filePath for the file to be deleted
     * @return true if the file could be deleted
     */
    override fun deleteFile(filePath: String): ShuttlePersistenceRemoveCargoResult {
        var result: ShuttlePersistenceRemoveCargoResult = ShuttlePersistenceRemoveCargoResult.UnableToRemove
        @Suppress("SwallowedException")
        try {
            val file = File(filePath)
            result = if (file.exists().not()) {
                ShuttlePersistenceRemoveCargoResult.DoesNotExist
            } else {
                val deleted = file.delete()
                if (deleted) {
                    ShuttlePersistenceRemoveCargoResult.Removed
                } else {
                    ShuttlePersistenceRemoveCargoResult.UnableToRemove
                }
            }
        } catch (se: SecurityException) {
            // This shouldn't happen
            Log.wtf(LOG_TAG, "Caught when deleting a file.")
        }
        return result
    }
}
