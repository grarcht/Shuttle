package com.grarcht.shuttle.demo.mviwithcompose.dependencyinjection

import android.app.Application
import android.content.Context
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataDb
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataModelFactory
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDbConfig
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttlePersistenceFileSystemGateway
import com.grarcht.shuttle.framework.screen.ShuttleCargoFacade
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleRepository
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides singleton dependencies for the MVI with Compose demo.
 * It wires together the Room database, file system gateway, and Shuttle instance
 * used throughout the demo for cargo transport and pickup.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    /**
     * Provides the singleton [Shuttle] instance backed by a Room database and file
     * system gateway for cargo persistence.
     *
     * @param applicationContext the application context used to configure the Room database
     * and resolve the app file directory path.
     */
    @Provides
    @Singleton
    fun provideShuttle(@ApplicationContext applicationContext: Context): Shuttle {
        val config = ShuttleRoomDbConfig(applicationContext)
        val dao = ShuttleRoomDataDb.getInstance(config).shuttleDataAccessObject
        val appFileDirectoryPath = applicationContext.filesDir.toString()
        val fileFactory = ShuttlePersistenceFileFactory()
        val fileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)
        val shuttleDataModelFactory = ShuttleRoomDataModelFactory()
        val shuttleWarehouse: ShuttleWarehouse = ShuttleRepository(
            dao,
            shuttleDataModelFactory,
            appFileDirectoryPath,
            fileSystemGateway
        )
        val shuttleFacade: ShuttleFacade =
            ShuttleCargoFacade(applicationContext as Application, shuttleWarehouse)
        return CargoShuttle(shuttleFacade, shuttleWarehouse)
    }
}
