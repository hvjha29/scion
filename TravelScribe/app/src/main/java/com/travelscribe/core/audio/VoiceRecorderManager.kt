package com.travelscribe.core.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing voice recording functionality.
 * Implementations handle the Android MediaRecorder API for capturing audio.
 *
 * This interface provides a clean abstraction for:
 * - Starting/stopping audio recording
 * - Monitoring recording state and amplitude
 * - Managing audio file lifecycle
 *
 * Usage:
 * ```kotlin
 * @Inject lateinit var voiceRecorder: VoiceRecorderManager
 *
 * // Start recording
 * val result = voiceRecorder.startRecording()
 * result.onSuccess { audioPath -> /* recording started */ }
 *
 * // Stop and save
 * val stopResult = voiceRecorder.stopRecording()
 * stopResult.onSuccess { finalPath -> /* use audio file */ }
 * ```
 */
interface VoiceRecorderManager {

    /**
     * Current state of the recorder.
     */
    val recordingState: StateFlow<RecordingState>

    /**
     * Current amplitude level for visualization (0-32767).
     * Emits values during active recording for waveform display.
     */
    val amplitude: StateFlow<Int>

    /**
     * Duration of current recording in milliseconds.
     * Emits updates every second during active recording.
     */
    val recordingDuration: StateFlow<Long>

    /**
     * Flow of recording errors.
     */
    val errors: Flow<RecordingError>

    /**
     * Starts a new audio recording session.
     *
     * Prerequisites:
     * - RECORD_AUDIO permission must be granted
     * - No active recording session
     *
     * @param outputFormat Desired output format (default: M4A/AAC)
     * @return Result containing the path where audio will be saved,
     *         or failure with error details
     */
    suspend fun startRecording(
        outputFormat: AudioOutputFormat = AudioOutputFormat.M4A
    ): Result<String>

    /**
     * Pauses the current recording session.
     * Only available on Android N+ (API 24+).
     *
     * @return Result indicating success or failure
     */
    suspend fun pauseRecording(): Result<Unit>

    /**
     * Resumes a paused recording session.
     * Only available on Android N+ (API 24+).
     *
     * @return Result indicating success or failure
     */
    suspend fun resumeRecording(): Result<Unit>

    /**
     * Stops the current recording and finalizes the audio file.
     *
     * @return Result containing the final audio file path,
     *         or failure if recording could not be finalized
     */
    suspend fun stopRecording(): Result<RecordingResult>

    /**
     * Cancels the current recording and deletes any partial file.
     * Use this when the user explicitly cancels recording.
     */
    fun cancelRecording()

    /**
     * Releases all resources held by the recorder.
     * Must be called when the recorder is no longer needed.
     */
    fun release()

    /**
     * Checks if the app has the required permissions for recording.
     *
     * @return true if RECORD_AUDIO permission is granted
     */
    fun hasRecordingPermission(): Boolean

    /**
     * Deletes a previously recorded audio file.
     *
     * @param audioPath Path to the audio file to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteRecording(audioPath: String): Result<Unit>

    /**
     * Gets the file size of a recording in bytes.
     *
     * @param audioPath Path to the audio file
     * @return File size in bytes, or null if file doesn't exist
     */
    fun getRecordingFileSize(audioPath: String): Long?
}

/**
 * Represents the current state of the voice recorder.
 */
sealed interface RecordingState {
    /**
     * Recorder is idle and ready to start a new recording.
     */
    data object Idle : RecordingState

    /**
     * Recorder is preparing/initializing.
     */
    data object Preparing : RecordingState

    /**
     * Actively recording audio.
     * @property filePath Path where audio is being recorded
     * @property startTimeMs Epoch timestamp when recording started
     */
    data class Recording(
        val filePath: String,
        val startTimeMs: Long
    ) : RecordingState

    /**
     * Recording is paused (API 24+).
     * @property filePath Path where audio is being recorded
     * @property pausedAtMs Duration recorded before pause
     */
    data class Paused(
        val filePath: String,
        val pausedAtMs: Long
    ) : RecordingState

    /**
     * Processing/finalizing the recording.
     */
    data object Stopping : RecordingState

    /**
     * An error occurred during recording.
     * @property error The error that occurred
     */
    data class Error(val error: RecordingError) : RecordingState
}

/**
 * Result of a completed recording.
 */
data class RecordingResult(
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val format: AudioOutputFormat
)

/**
 * Supported audio output formats.
 */
enum class AudioOutputFormat(
    val extension: String,
    val mimeType: String
) {
    /**
     * MPEG-4 container with AAC audio.
     * Recommended for speech recognition APIs.
     */
    M4A("m4a", "audio/mp4"),

    /**
     * 3GPP container with AMR audio.
     * Smaller file size, lower quality.
     */
    AMR("amr", "audio/amr"),

    /**
     * WebM container with Opus audio.
     * Good balance of quality and size.
     */
    WEBM("webm", "audio/webm"),

    /**
     * AAC audio stream.
     */
    AAC("aac", "audio/aac")
}

/**
 * Errors that can occur during recording.
 */
sealed class RecordingError(
    open val message: String,
    open val cause: Throwable? = null
) {
    /**
     * Missing RECORD_AUDIO permission.
     */
    data class PermissionDenied(
        override val message: String = "Recording permission not granted"
    ) : RecordingError(message)

    /**
     * Failed to initialize the MediaRecorder.
     */
    data class InitializationFailed(
        override val message: String,
        override val cause: Throwable? = null
    ) : RecordingError(message, cause)

    /**
     * Error occurred during active recording.
     */
    data class RecordingFailed(
        override val message: String,
        override val cause: Throwable? = null
    ) : RecordingError(message, cause)

    /**
     * Failed to finalize/save the recording.
     */
    data class FinalizationFailed(
        override val message: String,
        override val cause: Throwable? = null
    ) : RecordingError(message, cause)

    /**
     * Storage-related error (no space, write permission, etc.).
     */
    data class StorageError(
        override val message: String,
        override val cause: Throwable? = null
    ) : RecordingError(message, cause)

    /**
     * Operation not allowed in current state.
     */
    data class InvalidState(
        override val message: String,
        val currentState: RecordingState
    ) : RecordingError(message)

    /**
     * Unknown/unexpected error.
     */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : RecordingError(message, cause)
}

/**
 * Configuration for audio recording quality.
 */
data class RecordingConfig(
    val sampleRate: Int = 44100,
    val bitRate: Int = 128000,
    val channels: Int = 1,
    val outputFormat: AudioOutputFormat = AudioOutputFormat.M4A,
    val maxDurationMs: Long = 10 * 60 * 1000, // 10 minutes default
    val maxFileSizeBytes: Long = 50 * 1024 * 1024 // 50 MB default
) {
    companion object {
        /**
         * High quality preset for best transcription accuracy.
         */
        val HIGH_QUALITY = RecordingConfig(
            sampleRate = 48000,
            bitRate = 192000,
            channels = 1
        )

        /**
         * Standard quality preset balancing size and quality.
         */
        val STANDARD = RecordingConfig()

        /**
         * Low quality preset for minimum file size.
         */
        val LOW_QUALITY = RecordingConfig(
            sampleRate = 22050,
            bitRate = 64000,
            channels = 1
        )
    }
}
