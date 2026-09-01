package com.example.foodhub_android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedResourceEntity::class], version = 1, exportSchema = false)
abstract class SwiftBiteDatabase : RoomDatabase() {
    abstract fun cachedResourceDao(): CachedResourceDao
}
