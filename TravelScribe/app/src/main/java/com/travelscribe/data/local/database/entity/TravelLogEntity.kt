package com.travelscribe.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a TravelLog in the local database.
 * Expenses are stored as a JSON string and converted using TypeConverters.
 */
@Entity(
    tableName = "travel_logs",
    foreignKeys = [
        ForeignKey(
            entity = TravelDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["day_id"]),
        Index(value = ["created_at"])
    ]
)
data class TravelLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "day_id")
    val dayId: Long,

    @ColumnInfo(name = "raw_audio_path")
    val rawAudioPath: String? = null,

    @ColumnInfo(name = "audio_duration_ms")
    val audioDurationMs: Long? = null,

    @ColumnInfo(name = "transcribed_text")
    val transcribedText: String,

    @ColumnInfo(name = "original_languages")
    val originalLanguages: String, // JSON array: ["hi", "en"]

    @ColumnInfo(name = "expenses")
    val expenses: String, // JSON array of Expense objects

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "is_edited")
    val isEdited: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Epoch millis

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long // Epoch millis
)
