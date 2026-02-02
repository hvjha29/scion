package com.travelscribe.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request DTO for audio transcription.
 */
@JsonClass(generateAdapter = true)
data class TranscriptionRequestDto(
    @Json(name = "audio_url")
    val audioUrl: String? = null,

    @Json(name = "audio_base64")
    val audioBase64: String? = null,

    @Json(name = "source_languages")
    val sourceLanguages: List<String> = listOf("hi", "en"),

    @Json(name = "target_language")
    val targetLanguage: String = "en",

    @Json(name = "extract_expenses")
    val extractExpenses: Boolean = true,

    @Json(name = "format_output")
    val formatOutput: Boolean = true
)

/**
 * Response DTO for audio transcription.
 */
@JsonClass(generateAdapter = true)
data class TranscriptionResponseDto(
    @Json(name = "request_id")
    val requestId: String? = null,

    @Json(name = "narrative")
    val narrative: String,

    @Json(name = "expenses")
    val expenses: List<ExpenseDto>,

    @Json(name = "detected_languages")
    val detectedLanguages: List<String>? = null,

    @Json(name = "confidence")
    val confidence: Float? = null,

    @Json(name = "processing_time_ms")
    val processingTimeMs: Long? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * DTO for expense data from API.
 */
@JsonClass(generateAdapter = true)
data class ExpenseDto(
    @Json(name = "item")
    val item: String,

    @Json(name = "amount")
    val amount: Double,

    @Json(name = "currency")
    val currency: String,

    @Json(name = "category")
    val category: String? = null,

    @Json(name = "notes")
    val notes: String? = null,

    @Json(name = "is_estimate")
    val isEstimate: Boolean? = null
)

/**
 * Response DTO for audio upload.
 */
@JsonClass(generateAdapter = true)
data class UploadResponseDto(
    @Json(name = "url")
    val url: String,

    @Json(name = "file_id")
    val fileId: String? = null,

    @Json(name = "expires_at")
    val expiresAt: String? = null
)

/**
 * DTO for transcription status.
 */
@JsonClass(generateAdapter = true)
data class TranscriptionStatusDto(
    @Json(name = "request_id")
    val requestId: String,

    @Json(name = "status")
    val status: String, // "queued", "processing", "completed", "failed", "cancelled"

    @Json(name = "position")
    val position: Int? = null,

    @Json(name = "progress")
    val progress: Float? = null,

    @Json(name = "result")
    val result: TranscriptionResponseDto? = null,

    @Json(name = "error")
    val error: String? = null
)
