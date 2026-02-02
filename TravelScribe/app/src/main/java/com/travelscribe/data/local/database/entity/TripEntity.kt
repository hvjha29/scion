package com.travelscribe.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a Trip in the local database.
 */
@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["start_date"]),
        Index(value = ["title"])
    ]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "start_date")
    val startDate: Long, // Epoch day

    @ColumnInfo(name = "end_date")
    val endDate: Long?, // Epoch day, nullable for ongoing trips

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Epoch millis

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long // Epoch millis
)
