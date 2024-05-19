package com.grarcht.shuttle.framework.integrations.extensions.room

import android.content.Context

/**
 * The config used in configuring the database.
 */
data class ShuttleRoomDbConfig(val context: Context, val multiprocess: Boolean = false)
