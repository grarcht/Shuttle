package com.grarcht.shuttle.demo.core.di

import android.app.Application
import android.content.Context
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataDb
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDataModelFactory
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDbConfig
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.factory.ShuttlePersistenceFileFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
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
 * Hilt module installed in every demo app that depends on :demos-core-lib. Provides the full
 * Shuttle infrastructure graph — DAO, file system gateway, data model factory, warehouse, facade,
 * and the [Shuttle] singleton — each as a discrete binding so individual pieces can be swapped
 * in isolation (e.g. during testing).
 *
 * The only binding intentionally absent is [ShuttleRoomDbConfig]: each app module provides its
 * own so it can control app-specific options such as multi-process Room support.
 */
@InstallIn(SingletonComponent::class)
@Module
object ShuttleCoreModule {

    @Provides
    @Singleton
    fun provideShuttleDataAccessObject(config: ShuttleRoomDbConfig): ShuttleDataAccessObject =
        ShuttleRoomDataDb.getInstance(config).shuttleDataAccessObject

    @Provides
    @Singleton
    fun provideShuttleFileSystemGateway(): ShuttleFileSystemGateway =
        ShuttlePersistenceFileSystemGateway(ShuttlePersistenceFileFactory())

    @Provides
    @Singleton
    fun provideShuttleDataModelFactory(): ShuttleDataModelFactory =
        ShuttleRoomDataModelFactory()

    @Provides
    @Singleton
    fun provideShuttleWarehouse(
        dao: ShuttleDataAccessObject,
        dataModelFactory: ShuttleDataModelFactory,
        @ApplicationContext context: Context,
        fileSystemGateway: ShuttleFileSystemGateway
    ): ShuttleWarehouse =
        ShuttleRepository(dao, dataModelFactory, context.filesDir.absolutePath, fileSystemGateway)

    @Provides
    @Singleton
    fun provideShuttleFacade(
        @ApplicationContext context: Context,
        warehouse: ShuttleWarehouse
    ): ShuttleFacade =
        ShuttleCargoFacade(context as Application, warehouse)

    @Provides
    @Singleton
    fun provideShuttle(facade: ShuttleFacade, warehouse: ShuttleWarehouse): Shuttle =
        CargoShuttle(facade, warehouse)
}
