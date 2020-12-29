package com.grarcht.shuttle.framework.integrations.persistence.datamodel

/**
 * The contractual interface used to ensure data, sent to the database implementations,
 * will be the same across various database solutions, like Room and others.
 */
interface ShuttleDataModel {
    /**
     * Used to retrieve the blobs from the database.
     */
    val cargoId: String

    /**
     * The file path for the data blob, stored in the file system.
     */
    val filePath: String
}
