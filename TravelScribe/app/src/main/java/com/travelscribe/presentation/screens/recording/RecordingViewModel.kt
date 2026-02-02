package com.travelscribe.presentation.screens.recording

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelscribe.core.audio.VoiceRecorderManager
import com.travelscribe.core.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Recording Screen.
 */
data class RecordingUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val recordingDuration: Long = 0L, // in milliseconds
    val audioFilePath: String? = null,
    val error: String? = null,
    val amplitudes: List<Float> = emptyList()
)

/**
 * ViewModel for the Recording Screen.
 * Manages audio recording state and interactions with VoiceRecorderManager.
 */
@HiltViewModel
class RecordingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voiceRecorderManager: VoiceRecorderManager
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(Constants.NavArgs.TRIP_ID) ?: 0L
    private val dayId: Long = savedStateHandle.get<Long>(Constants.NavArgs.DAY_ID) ?: 0L

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private var durationJob: Job? = null
    private var startTime: Long = 0L

    /**
     * Start audio recording.
     */
    fun startRecording() {
        viewModelScope.launch {
            try {
                val filePath = voiceRecorderManager.startRecording()
                startTime = System.currentTimeMillis()
                
                _uiState.update { state ->
                    state.copy(
                        isRecording = true,
                        isPaused = false,
                        audioFilePath = filePath,
                        error = null
                    )
                }

                // Start duration timer
                startDurationTimer()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to start recording"
                    )
                }
            }
        }
    }

    /**
     * Stop audio recording.
     */
    fun stopRecording() {
        viewModelScope.launch {
            try {
                voiceRecorderManager.stopRecording()
                durationJob?.cancel()
                
                _uiState.update { state ->
                    state.copy(
                        isRecording = false,
                        isPaused = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to stop recording"
                    )
                }
            }
        }
    }

    /**
     * Pause audio recording.
     */
    fun pauseRecording() {
        viewModelScope.launch {
            try {
                voiceRecorderManager.pauseRecording()
                durationJob?.cancel()
                
                _uiState.update { state ->
                    state.copy(
                        isPaused = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to pause recording"
                    )
                }
            }
        }
    }

    /**
     * Resume audio recording.
     */
    fun resumeRecording() {
        viewModelScope.launch {
            try {
                voiceRecorderManager.resumeRecording()
                
                _uiState.update { state ->
                    state.copy(
                        isPaused = false
                    )
                }

                startDurationTimer()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to resume recording"
                    )
                }
            }
        }
    }

    /**
     * Start the duration timer.
     */
    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (isActive) {
                val currentDuration = System.currentTimeMillis() - startTime
                _uiState.update { state ->
                    state.copy(recordingDuration = currentDuration)
                }
                delay(100) // Update every 100ms
            }
        }
    }

    /**
     * Get current amplitude for visualization.
     */
    fun getAmplitude(): Int {
        return if (_uiState.value.isRecording && !_uiState.value.isPaused) {
            voiceRecorderManager.getMaxAmplitude()
        } else {
            0
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
        if (_uiState.value.isRecording) {
            voiceRecorderManager.stopRecording()
        }
    }
}
