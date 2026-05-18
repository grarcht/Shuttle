package com.grarcht.shuttle.demo.mvvmwithaservice.dependencyinjection

import android.content.Context
import com.grarcht.shuttle.demo.mvvmwithaservice.model.RemoteService
import com.grarcht.shuttle.demo.mvvmwithaservice.visibility.DefaultLoggerVisibilityReporter
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.app.ShuttleServiceConfig
import com.grarcht.shuttle.framework.app.ShuttleServiceType
import com.grarcht.shuttle.framework.content.serviceconnection.factory.ShuttleServiceConnectionFactory
import com.grarcht.shuttle.framework.content.serviceconnection.factory.ShuttleServiceConnectionTypesFactory
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDbConfig
import com.grarcht.shuttle.framework.os.messenger.factory.ShuttleServiceMessengerFactory
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityReporter
import com.grarcht.shuttle.framework.visibility.observation.ShuttleChannelVisibilityObservable
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides bindings specific to the mvvm-with-a-service demo. The core Shuttle infrastructure
 * graph (DAO, warehouse, facade, [Shuttle]) is provided by ShuttleCoreModule in :demos-core-foundation.
 * This module supplies the Room config (with multi-process enabled for IPC), service connection
 * factory, visibility observable, and the remote service configuration.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    /**
     * Provides the [ShuttleRoomDbConfig] with multi-process mode enabled so the Room database
     * is accessible from both the main process and the remote service process.
     */
    @Provides
    @Singleton
    fun provideShuttleRoomDbConfig(@ApplicationContext context: Context): ShuttleRoomDbConfig =
        ShuttleRoomDbConfig(context, multiprocess = true)

    /** Provides the factory used to create lifecycle-aware service connections. */
    @Provides
    fun provideShuttleServiceConnectionFactory(): ShuttleServiceConnectionFactory =
        ShuttleServiceConnectionTypesFactory()

    /** Provides a [kotlinx.coroutines.CoroutineScope] tied to the main dispatcher. */
    @Named("MainScope")
    @Provides
    fun provideMainScope(): CoroutineScope = MainScope()

    /** Provides the [ShuttleVisibilityReporter] that logs Shuttle events via [android.util.Log]. */
    @Provides
    fun provideVisibilityReporter(): ShuttleVisibilityReporter =
        DefaultLoggerVisibilityReporter()

    /** Provides the [ShuttleVisibilityObservable] that routes visibility events to the reporter. */
    @Provides
    fun provideShuttleVisibilityObservable(
        reporter: ShuttleVisibilityReporter,
        @Named("MainScope") mainScope: CoroutineScope
    ): ShuttleVisibilityObservable =
        ShuttleChannelVisibilityObservable(reporter, mainScope)

    /**
     * Provides the [ShuttleServiceConfig] for [com.grarcht.shuttle.demo.mvvmwithaservice.model.RemoteService],
     * configured for IPC messenger binding with automatic rebind on unbind.
     */
    @Provides
    @Named("RemoteServiceConfig")
    fun provideRemoteServiceConfig(
        shuttle: Shuttle,
        errorObservable: ShuttleVisibilityObservable
    ): ShuttleServiceConfig =
        ShuttleServiceConfig(
            serviceName = RemoteService.SERVICE_NAME,
            shuttle = shuttle,
            rebindOnUnbind = true,
            errorObservable = errorObservable,
            bindingType = ShuttleServiceType.BOUND_MESSENGER,
            messengerFactory = ShuttleServiceMessengerFactory()
        )
}
