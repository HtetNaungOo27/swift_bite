package com.example.foodhub_android.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SwiftBiteDatabase =
        Room.databaseBuilder(context, SwiftBiteDatabase::class.java, "swiftbite-cache.db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideCachedResourceDao(database: SwiftBiteDatabase): CachedResourceDao =
        database.cachedResourceDao()
}
