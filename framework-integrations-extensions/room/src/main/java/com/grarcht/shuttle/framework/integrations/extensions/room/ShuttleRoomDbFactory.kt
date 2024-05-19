package com.grarcht.shuttle.framework.integrations.extensions.room

import androidx.room.Room

private const val DB_NAME = "Shuttle.db"

/**
 * This factory is used to create [ShuttleRoomDataDb] objects.  For more information
 * on the factory design pattern, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
open class ShuttleRoomDbFactory {

    /**
     * Creates a [ShuttleRoomDataDb] object from the [config].
     * @return the db object reference
     */
    fun createDb(config: ShuttleRoomDbConfig): ShuttleRoomDataDb {
        return if (config.multiprocess) {
            Room.databaseBuilder(
                    config.context.applicationContext,
                    ShuttleRoomDataDb::class.java,
                    DB_NAME
                ).enableMultiInstanceInvalidation()
                .build()
        } else {
            Room.databaseBuilder(
                config.context.applicationContext,
                ShuttleRoomDataDb::class.java,
                DB_NAME
            ).build()
        }
    }
}
