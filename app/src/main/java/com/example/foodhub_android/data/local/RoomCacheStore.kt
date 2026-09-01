package com.example.foodhub_android.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCacheStore @Inject constructor(
    private val dao: CachedResourceDao
) {
    private val maintenanceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun observe(scope: String, resource: String): Flow<String?> = dao.observe(scope, resource)

    suspend fun write(scope: String, resource: String, payload: String) {
        dao.upsert(CachedResourceEntity(scope, resource, payload, System.currentTimeMillis()))
    }

    fun purgeScope(scope: String) {
        maintenanceScope.launch { dao.clearScope(scope) }
    }

    fun purgeAll() {
        maintenanceScope.launch { dao.clearAll() }
    }
}
