package com.grarcht.shuttle.demo.mvvmwithnavigation.dependencyinjection

import android.content.Context
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomDbConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideShuttleRoomDbConfig(@ApplicationContext context: Context): ShuttleRoomDbConfig =
        ShuttleRoomDbConfig(context)
}
