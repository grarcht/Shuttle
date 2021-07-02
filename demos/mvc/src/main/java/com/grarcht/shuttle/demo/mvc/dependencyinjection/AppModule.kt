package com.grarcht.shuttle.demo.mvc.dependencyinjection

import android.app.Application
import android.content.Context
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttlePersistenceFileSystemGateway
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataDb
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataModelFactory
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDbConfig
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
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
 * This module shows the basics of integrating the Shuttle Framework with Hilt by
 * providing what is needed to get set up with the default solution.  Typically,
 * the dependencies are created in separate functions and passed into the provides
 * function.  To keep a high level of readability and recognizability with this demo,
 * they are created within the provideShuttle function.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideShuttle(@ApplicationContext applicationContext: Context): Shuttle {
        // The demo app uses the Room database by default; however, the framework
        // could be set up to use another database solution.

        val config = ShuttleRoomDbConfig(applicationContext)

        // Get the DAO for the Room database
        val dao = ShuttleRoomDataDb.getInstance(config).shuttleDataAccessObject

        val appFileDirectoryPath = applicationContext.filesDir.absolutePath
        val fileFactory = ShuttlePersistenceFileFactory()
        val fileSystemGateway = ShuttlePersistenceFileSystemGateway(fileFactory)

        // Factory to create data models for storing in the Room database
        val shuttleDataModelFactory = ShuttleRoomDataModelFactory()

        // The warehouse enables storing and picking up intent and bundle
        // cargo (data).
        val shuttleWarehouse: ShuttleWarehouse = ShuttleRepository(
            dao,
            shuttleDataModelFactory,
            appFileDirectoryPath,
            fileSystemGateway
        )

        // The facade hides functionality, such as observing removing cargo
        // after the user presses a back button for an activity.
        val shuttleFacade: ShuttleFacade =
            ShuttleCargoFacade(applicationContext as Application, shuttleWarehouse)

        // The shuttle object is used for easy interaction with the Shuttle
        // framework.
        return CargoShuttle(shuttleFacade, shuttleWarehouse)
    }
}
