package com.example.foodhub_android.data.local

import androidx.room.Entity

@Entity(
    tableName = "cached_resources",
    primaryKeys = ["scope", "resource"]
)
data class CachedResourceEntity(
    val scope: String,
    val resource: String,
    val payload: String,
    val updatedAt: Long
)
