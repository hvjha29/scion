package com.travelscribe.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model representing a single day within a [Trip].
 * A TravelDay acts as a container for multiple [TravelLog] entries
 * that occurred on the same date during a trip.
 *
 * @property id Unique identifier for the travel day
 * @property tripId Reference to the parent [Trip]
 * @property date The calendar date this travel day represents
 * @property dayNumber The sequential day number within the trip (e.g., Day 1, Day 2)
 * @property notes Optional notes or summary for this day
 * @property weatherInfo Optional weather information for this day
 * @property location Optional primary location for this day
 * @property createdAt Timestamp when this travel day was created
 * @property updatedAt Timestamp when this travel day was last modified
 */
data class TravelDay(
    val id: Long = 0,
    val tripId: Long,
    val date: LocalDate,
    val dayNumber: Int? = null,
    val notes: String? = null,
    val weatherInfo: String? = null,
    val location: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Returns a formatted display title for this day.
     * Example: "Day 1 - Jan 15, 2024"
     */
    fun getDisplayTitle(): String {
        return if (dayNumber != null) {
            "Day $dayNumber - $date"
        } else {
            date.toString()
        }
    }
}
