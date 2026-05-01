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
 * graph (DAO, warehouse, facade, [Shuttle]) is provided by ShuttleCoreModule in :demos-core-lib.
 * This module supplies the Room config (with multi-process enabled for IPC), service connection
 * factory, visibility observable, and the remote service configuration.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideShuttleRoomDbConfig(@ApplicationContext context: Context): ShuttleRoomDbConfig =
        ShuttleRoomDbConfig(context, multiprocess = true)

    @Provides
    fun provideShuttleServiceConnectionFactory(): ShuttleServiceConnectionFactory =
        ShuttleServiceConnectionTypesFactory()

    @Named("MainScope")
    @Provides
    fun provideMainScope(): CoroutineScope = MainScope()

    @Provides
    fun provideVisibilityReporter(): ShuttleVisibilityReporter =
        DefaultLoggerVisibilityReporter()

    @Provides
    fun provideShuttleVisibilityObservable(
        reporter: ShuttleVisibilityReporter,
        @Named("MainScope") mainScope: CoroutineScope
    ): ShuttleVisibilityObservable =
        ShuttleChannelVisibilityObservable(reporter, mainScope)

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
