package com.travelscribe.domain.model

import java.time.Instant

/**
 * Domain model representing a single travel log entry.
 * A TravelLog is created from a voice recording that gets transcribed
 * by the LLM backend into structured text and extracted expenses.
 *
 * @property id Unique identifier for the travel log
 * @property dayId Reference to the parent [TravelDay]
 * @property rawAudioPath File path to the original audio recording (.m4a/.mp3)
 * @property audioDurationMs Duration of the audio recording in milliseconds
 * @property transcribedText The narrative text transcribed from the audio
 * @property originalLanguages Languages detected in the original audio
 * @property expenses List of expenses extracted from the transcription
 * @property location Optional location where this log was recorded
 * @property isEdited Whether the transcribed text has been manually edited
 * @property createdAt Timestamp when this log was created
 * @property updatedAt Timestamp when this log was last modified
 */
data class TravelLog(
    val id: Long = 0,
    val dayId: Long,
    val rawAudioPath: String? = null,
    val audioDurationMs: Long? = null,
    val transcribedText: String,
    val originalLanguages: List<String> = listOf("hi", "en"),
    val expenses: List<Expense> = emptyList(),
    val location: String? = null,
    val isEdited: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Returns the total expenses amount in a specific currency.
     */
    fun getTotalExpenses(currency: String): Double {
        return expenses
            .filter { it.currency.equals(currency, ignoreCase = true) }
            .sumOf { it.amount }
    }

    /**
     * Returns all unique currencies used in expenses.
     */
    fun getUniqueCurrencies(): Set<String> {
        return expenses.map { it.currency }.toSet()
    }

    /**
     * Returns expenses grouped by category.
     */
    fun getExpensesByCategory(): Map<ExpenseCategory, List<Expense>> {
        return expenses.groupBy { it.category }
    }

    /**
     * Returns a preview of the transcribed text (first N characters).
     */
    fun getTextPreview(maxLength: Int = 100): String {
        return if (transcribedText.length <= maxLength) {
            transcribedText
        } else {
            "${transcribedText.take(maxLength)}..."
        }
    }

    /**
     * Formats the audio duration for display (e.g., "2:35").
     */
    fun getFormattedDuration(): String? {
        return audioDurationMs?.let { ms ->
            val seconds = (ms / 1000) % 60
            val minutes = (ms / (1000 * 60)) % 60
            val hours = (ms / (1000 * 60 * 60))
            
            if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    }
}
