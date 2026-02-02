package com.travelscribe.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.travelscribe.core.audio.AudioOutputFormat
import com.travelscribe.core.audio.RecordingConfig
import com.travelscribe.core.audio.RecordingError
import com.travelscribe.core.audio.RecordingResult
import com.travelscribe.core.audio.RecordingState
import com.travelscribe.core.audio.VoiceRecorderManager
import com.travelscribe.core.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Implementation of [VoiceRecorderManager] using Android MediaRecorder API.
 */
class VoiceRecorderManagerImpl @Inject constructor(
    private val context: Context,
    private val config: RecordingConfig = RecordingConfig.STANDARD
) : VoiceRecorderManager {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var recordingStartTime: Long = 0L
    private var pausedDuration: Long = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var amplitudeJob: Job? = null
    private var durationJob: Job? = null

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    override val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    override val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _errors = MutableSharedFlow<RecordingError>()
    override val errors: Flow<RecordingError> = _errors.asSharedFlow()

    override suspend fun startRecording(outputFormat: AudioOutputFormat): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Check permission
                if (!hasRecordingPermission()) {
                    val error = RecordingError.PermissionDenied()
                    _recordingState.value = RecordingState.Error(error)
                    _errors.emit(error)
                    return@withContext Result.failure(Exception(error.message))
                }

                // Check current state
                if (_recordingState.value !is RecordingState.Idle) {
                    val error = RecordingError.InvalidState(
                        message = "Cannot start recording: not in idle state",
                        currentState = _recordingState.value
                    )
                    _errors.emit(error)
                    return@withContext Result.failure(Exception(error.message))
                }

                _recordingState.value = RecordingState.Preparing

                // Create output file
                val filePath = createOutputFilePath(outputFormat)
                currentFilePath = filePath

                // Initialize MediaRecorder
                mediaRecorder = createMediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(getOutputFormat(outputFormat))
                    setAudioEncoder(getAudioEncoder(outputFormat))
                    setAudioSamplingRate(config.sampleRate)
                    setAudioEncodingBitRate(config.bitRate)
                    setAudioChannels(config.channels)
                    setOutputFile(filePath)
                    setMaxDuration(config.maxDurationMs.toInt())
                    setMaxFileSize(config.maxFileSizeBytes)

                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED,
                            MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                                scope.launch { stopRecording() }
                            }
                        }
                    }

                    setOnErrorListener { _, _, _ ->
                        scope.launch {
                            val error = RecordingError.RecordingFailed("MediaRecorder error occurred")
                            _recordingState.value = RecordingState.Error(error)
                            _errors.emit(error)
                        }
                    }

                    prepare()
                    start()
                }

                recordingStartTime = System.currentTimeMillis()
                pausedDuration = 0L
                _recordingState.value = RecordingState.Recording(filePath, recordingStartTime)

                // Start amplitude monitoring
                startAmplitudeMonitoring()
                // Start duration tracking
                startDurationTracking()

                Result.success(filePath)
            } catch (e: Exception) {
                val error = RecordingError.InitializationFailed(
                    message = e.message ?: "Failed to initialize recorder",
                    cause = e
                )
                _recordingState.value = RecordingState.Error(error)
                _errors.emit(error)
                cleanupRecorder()
                Result.failure(e)
            }
        }
    }

    override suspend fun pauseRecording(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentState = _recordingState.value
                if (currentState !is RecordingState.Recording) {
                    return@withContext Result.failure(
                        Exception("Cannot pause: not currently recording")
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    val pausedAt = System.currentTimeMillis() - recordingStartTime
                    _recordingState.value = RecordingState.Paused(currentState.filePath, pausedAt)
                    stopAmplitudeMonitoring()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Pause not supported on this Android version"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun resumeRecording(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentState = _recordingState.value
                if (currentState !is RecordingState.Paused) {
                    return@withContext Result.failure(
                        Exception("Cannot resume: not currently paused")
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    pausedDuration += currentState.pausedAtMs
                    _recordingState.value = RecordingState.Recording(
                        currentState.filePath,
                        recordingStartTime
                    )
                    startAmplitudeMonitoring()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Resume not supported on this Android version"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun stopRecording(): Result<RecordingResult> {
        return withContext(Dispatchers.IO) {
            try {
                val currentState = _recordingState.value
                val filePath = when (currentState) {
                    is RecordingState.Recording -> currentState.filePath
                    is RecordingState.Paused -> currentState.filePath
                    else -> {
                        return@withContext Result.failure(
                            Exception("Cannot stop: no active recording")
                        )
                    }
                }

                _recordingState.value = RecordingState.Stopping
                stopAmplitudeMonitoring()
                stopDurationTracking()

                mediaRecorder?.apply {
                    stop()
                    reset()
                    release()
                }
                mediaRecorder = null

                val file = File(filePath)
                if (!file.exists()) {
                    val error = RecordingError.FinalizationFailed("Recording file not found")
                    _recordingState.value = RecordingState.Error(error)
                    return@withContext Result.failure(Exception(error.message))
                }

                val durationMs = System.currentTimeMillis() - recordingStartTime
                val result = RecordingResult(
                    filePath = filePath,
                    durationMs = durationMs,
                    fileSizeBytes = file.length(),
                    format = AudioOutputFormat.M4A // TODO: Get from config
                )

                _recordingState.value = RecordingState.Idle
                _recordingDuration.value = 0L
                currentFilePath = null

                Result.success(result)
            } catch (e: Exception) {
                val error = RecordingError.FinalizationFailed(
                    message = e.message ?: "Failed to finalize recording",
                    cause = e
                )
                _recordingState.value = RecordingState.Error(error)
                _errors.emit(error)
                cleanupRecorder()
                Result.failure(e)
            }
        }
    }

    override fun cancelRecording() {
        scope.launch(Dispatchers.IO) {
            stopAmplitudeMonitoring()
            stopDurationTracking()
            cleanupRecorder()

            // Delete partial file
            currentFilePath?.let { path ->
                File(path).delete()
            }
            currentFilePath = null

            _recordingState.value = RecordingState.Idle
            _recordingDuration.value = 0L
        }
    }

    override fun release() {
        cancelRecording()
        scope.cancel()
    }

    override fun hasRecordingPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun deleteRecording(audioPath: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(audioPath)
                if (file.exists()) {
                    file.delete()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getRecordingFileSize(audioPath: String): Long? {
        val file = File(audioPath)
        return if (file.exists()) file.length() else null
    }

    // ==================== Private Helper Methods ====================

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private fun createOutputFilePath(format: AudioOutputFormat): String {
        val recordingsDir = File(context.filesDir, Constants.Audio.AUDIO_DIRECTORY)
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "recording_$timestamp.${format.extension}"

        return File(recordingsDir, fileName).absolutePath
    }

    private fun getOutputFormat(format: AudioOutputFormat): Int {
        return when (format) {
            AudioOutputFormat.M4A -> MediaRecorder.OutputFormat.MPEG_4
            AudioOutputFormat.AMR -> MediaRecorder.OutputFormat.AMR_NB
            AudioOutputFormat.AAC -> MediaRecorder.OutputFormat.AAC_ADTS
            AudioOutputFormat.WEBM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaRecorder.OutputFormat.WEBM
            } else {
                MediaRecorder.OutputFormat.MPEG_4
            }
        }
    }

    private fun getAudioEncoder(format: AudioOutputFormat): Int {
        return when (format) {
            AudioOutputFormat.M4A, AudioOutputFormat.AAC -> MediaRecorder.AudioEncoder.AAC
            AudioOutputFormat.AMR -> MediaRecorder.AudioEncoder.AMR_NB
            AudioOutputFormat.WEBM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaRecorder.AudioEncoder.VORBIS
            } else {
                MediaRecorder.AudioEncoder.AAC
            }
        }
    }

    private fun startAmplitudeMonitoring() {
        amplitudeJob = scope.launch {
            while (isActive && _recordingState.value is RecordingState.Recording) {
                try {
                    val amp = mediaRecorder?.maxAmplitude ?: 0
                    _amplitude.value = amp
                } catch (e: Exception) {
                    // Ignore
                }
                delay(Constants.Audio.AMPLITUDE_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopAmplitudeMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }

    private fun startDurationTracking() {
        durationJob = scope.launch {
            while (isActive) {
                val state = _recordingState.value
                if (state is RecordingState.Recording) {
                    _recordingDuration.value = System.currentTimeMillis() - recordingStartTime
                }
                delay(1000L)
            }
        }
    }

    private fun stopDurationTracking() {
        durationJob?.cancel()
        durationJob = null
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.apply {
                reset()
                release()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        mediaRecorder = null
    }
}
