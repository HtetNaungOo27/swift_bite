package com.example.foodhub_android.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedResourceDao {
    @Query("SELECT payload FROM cached_resources WHERE scope = :scope AND resource = :resource")
    fun observe(scope: String, resource: String): Flow<String?>

    @Upsert
    suspend fun upsert(entity: CachedResourceEntity)

    @Query("DELETE FROM cached_resources WHERE scope = :scope")
    suspend fun clearScope(scope: String)

    @Query("DELETE FROM cached_resources")
    suspend fun clearAll()
}
