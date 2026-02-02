package com.travelscribe.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model representing a Trip.
 * A Trip is the top-level container for organizing travel experiences.
 * Each trip contains multiple [TravelDay]s.
 *
 * @property id Unique identifier for the trip
 * @property title Human-readable title for the trip (e.g., "Japan 2024")
 * @property startDate The starting date of the trip
 * @property endDate The ending date of the trip (nullable for ongoing trips)
 * @property description Optional description or summary of the trip
 * @property coverImagePath Optional path to a cover image for the trip
 * @property createdAt Timestamp when the trip was created
 * @property updatedAt Timestamp when the trip was last modified
 */
data class Trip(
    val id: Long = 0,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val description: String? = null,
    val coverImagePath: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Returns whether this trip is currently ongoing (no end date set).
     */
    val isOngoing: Boolean
        get() = endDate == null

    /**
     * Returns the duration of the trip in days.
     * Returns null if the trip is ongoing.
     */
    val durationDays: Int?
        get() = endDate?.let { 
            java.time.temporal.ChronoUnit.DAYS.between(startDate, it).toInt() + 1 
        }

    /**
     * Returns a formatted date range string.
     */
    fun getFormattedDateRange(): String {
        return if (endDate != null) {
            "${startDate} - ${endDate}"
        } else {
            "${startDate} - Ongoing"
        }
    }
}
