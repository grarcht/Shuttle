package com.grarcht.shuttle.framework.integrations.persistence

/**
 * The contractual interface used to various database implementations for Shuttle.
 */
interface ShuttleDatabase {
    /**
     * The Data Access Object (DAO) reference for the DAO used to access the Shuttle database.
     */
    val shuttleDataAccessObject: ShuttleDataAccessObject
}
