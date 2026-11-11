package com.logix.optiflow.di

import android.content.Context
import androidx.room.Room
import com.logix.optiflow.data.local.cache.CachedResponseDao
import com.logix.optiflow.data.local.cache.OptiFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OptiFlowDatabase =
        Room.databaseBuilder(context, OptiFlowDatabase::class.java, "optiflow.db").build()

    @Provides
    fun provideCachedResponseDao(database: OptiFlowDatabase): CachedResponseDao = database.cachedResponseDao()
}
