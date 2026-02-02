package com.travelscribe.domain.repository

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.Expense

/**
 * Repository interface for audio transcription operations.
 * Handles communication with the backend LLM API.
 */
interface TranscriptionRepository {

    /**
     * Transcribes an audio file to structured text and expenses.
     *
     * @param audioFilePath Path to the local audio file
     * @param sourceLanguages Languages present in the audio (e.g., ["hi", "en"])
     * @param targetLanguage Target language for transcription (default: "en")
     * @return Resource containing the transcription result
     */
    suspend fun transcribeAudio(
        audioFilePath: String,
        sourceLanguages: List<String> = listOf("hi", "en"),
        targetLanguage: String = "en"
    ): Resource<TranscriptionResult>

    /**
     * Uploads audio file to the server.
     *
     * @param audioFilePath Path to the local audio file
     * @return Resource containing the remote URL of the uploaded file
     */
    suspend fun uploadAudio(audioFilePath: String): Resource<String>

    /**
     * Gets the transcription status for an async transcription request.
     *
     * @param requestId The transcription request ID
     * @return Resource containing the current status
     */
    suspend fun getTranscriptionStatus(requestId: String): Resource<TranscriptionStatus>

    /**
     * Cancels an ongoing transcription request.
     *
     * @param requestId The transcription request ID
     * @return Resource indicating success or failure
     */
    suspend fun cancelTranscription(requestId: String): Resource<Unit>
}

/**
 * Result of a transcription operation.
 */
data class TranscriptionResult(
    /**
     * The narrative text transcribed from the audio.
     */
    val narrative: String,

    /**
     * List of expenses extracted from the audio.
     */
    val expenses: List<Expense>,

    /**
     * Detected languages in the original audio.
     */
    val detectedLanguages: List<String>,

    /**
     * Confidence score of the transcription (0.0 to 1.0).
     */
    val confidence: Float,

    /**
     * Processing time in milliseconds.
     */
    val processingTimeMs: Long,

    /**
     * Additional metadata from the API.
     */
    val metadata: Map<String, Any>? = null
)

/**
 * Status of an async transcription request.
 */
sealed interface TranscriptionStatus {
    /**
     * Request is queued for processing.
     */
    data class Queued(val position: Int) : TranscriptionStatus

    /**
     * Request is currently being processed.
     */
    data class Processing(val progress: Float) : TranscriptionStatus

    /**
     * Transcription completed successfully.
     */
    data class Completed(val result: TranscriptionResult) : TranscriptionStatus

    /**
     * Transcription failed.
     */
    data class Failed(val error: String) : TranscriptionStatus

    /**
     * Request was cancelled.
     */
    data object Cancelled : TranscriptionStatus
}
