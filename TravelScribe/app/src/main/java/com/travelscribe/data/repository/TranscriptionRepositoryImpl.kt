package com.travelscribe.data.repository

import android.util.Base64
import com.travelscribe.core.common.ErrorCode
import com.travelscribe.core.common.Resource
import com.travelscribe.data.mapper.toDomain
import com.travelscribe.data.remote.api.LlmApiService
import com.travelscribe.data.remote.dto.TranscriptionRequestDto
import com.travelscribe.domain.repository.TranscriptionRepository
import com.travelscribe.domain.repository.TranscriptionResult
import com.travelscribe.domain.repository.TranscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Implementation of [TranscriptionRepository] using Retrofit API calls.
 */
class TranscriptionRepositoryImpl @Inject constructor(
    private val apiService: LlmApiService
) : TranscriptionRepository {

    override suspend fun transcribeAudio(
        audioFilePath: String,
        sourceLanguages: List<String>,
        targetLanguage: String
    ): Resource<TranscriptionResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Read audio file and convert to base64
                val audioFile = File(audioFilePath)
                if (!audioFile.exists()) {
                    return@withContext Resource.Error(
                        message = "Audio file not found: $audioFilePath",
                        code = ErrorCode.NOT_FOUND
                    )
                }

                val audioBytes = audioFile.readBytes()
                val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

                val request = TranscriptionRequestDto(
                    audioBase64 = audioBase64,
                    sourceLanguages = sourceLanguages,
                    targetLanguage = targetLanguage,
                    extractExpenses = true,
                    formatOutput = true
                )

                val startTime = System.currentTimeMillis()
                val response = apiService.transcribeAudio(request)
                val endTime = System.currentTimeMillis()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(
                            TranscriptionResult(
                                narrative = body.narrative,
                                expenses = body.expenses.map { it.toDomain() },
                                detectedLanguages = body.detectedLanguages ?: sourceLanguages,
                                confidence = body.confidence ?: 0.9f,
                                processingTimeMs = body.processingTimeMs ?: (endTime - startTime),
                                metadata = body.metadata
                            )
                        )
                    } else {
                        Resource.Error(
                            message = "Empty response from transcription API",
                            code = ErrorCode.API_ERROR
                        )
                    }
                } else {
                    Resource.Error(
                        message = "Transcription failed: ${response.message()}",
                        code = ErrorCode.API_ERROR
                    )
                }
            } catch (e: Exception) {
                Resource.Error(
                    message = e.message ?: "Transcription failed",
                    code = ErrorCode.TRANSCRIPTION_FAILED,
                    exception = e
                )
            }
        }
    }

    override suspend fun uploadAudio(audioFilePath: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val audioFile = File(audioFilePath)
                if (!audioFile.exists()) {
                    return@withContext Resource.Error(
                        message = "Audio file not found: $audioFilePath",
                        code = ErrorCode.NOT_FOUND
                    )
                }

                val mimeType = when {
                    audioFilePath.endsWith(".m4a") -> "audio/mp4"
                    audioFilePath.endsWith(".mp3") -> "audio/mpeg"
                    audioFilePath.endsWith(".aac") -> "audio/aac"
                    audioFilePath.endsWith(".amr") -> "audio/amr"
                    audioFilePath.endsWith(".webm") -> "audio/webm"
                    else -> "audio/*"
                }

                val requestBody = audioFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "audio",
                    audioFile.name,
                    requestBody
                )

                val response = apiService.uploadAudio(multipartBody)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body.url)
                    } else {
                        Resource.Error(
                            message = "Empty response from upload API",
                            code = ErrorCode.API_ERROR
                        )
                    }
                } else {
                    Resource.Error(
                        message = "Upload failed: ${response.message()}",
                        code = ErrorCode.API_ERROR
                    )
                }
            } catch (e: Exception) {
                Resource.Error(
                    message = e.message ?: "Upload failed",
                    code = ErrorCode.NETWORK_ERROR,
                    exception = e
                )
            }
        }
    }

    override suspend fun getTranscriptionStatus(requestId: String): Resource<TranscriptionStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTranscriptionStatus(requestId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val status = when (body.status.lowercase()) {
                            "queued" -> TranscriptionStatus.Queued(body.position ?: 0)
                            "processing" -> TranscriptionStatus.Processing(body.progress ?: 0f)
                            "completed" -> {
                                val result = body.result
                                if (result != null) {
                                    TranscriptionStatus.Completed(
                                        TranscriptionResult(
                                            narrative = result.narrative,
                                            expenses = result.expenses.map { it.toDomain() },
                                            detectedLanguages = result.detectedLanguages ?: emptyList(),
                                            confidence = result.confidence ?: 0.9f,
                                            processingTimeMs = result.processingTimeMs ?: 0L,
                                            metadata = result.metadata
                                        )
                                    )
                                } else {
                                    TranscriptionStatus.Failed("Result not available")
                                }
                            }
                            "failed" -> TranscriptionStatus.Failed(body.error ?: "Unknown error")
                            "cancelled" -> TranscriptionStatus.Cancelled
                            else -> TranscriptionStatus.Failed("Unknown status: ${body.status}")
                        }
                        Resource.Success(status)
                    } else {
                        Resource.Error(
                            message = "Empty response from status API",
                            code = ErrorCode.API_ERROR
                        )
                    }
                } else {
                    Resource.Error(
                        message = "Failed to get status: ${response.message()}",
                        code = ErrorCode.API_ERROR
                    )
                }
            } catch (e: Exception) {
                Resource.Error(
                    message = e.message ?: "Failed to get transcription status",
                    code = ErrorCode.NETWORK_ERROR,
                    exception = e
                )
            }
        }
    }

    override suspend fun cancelTranscription(requestId: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelTranscription(requestId)

                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(
                        message = "Failed to cancel: ${response.message()}",
                        code = ErrorCode.API_ERROR
                    )
                }
            } catch (e: Exception) {
                Resource.Error(
                    message = e.message ?: "Failed to cancel transcription",
                    code = ErrorCode.NETWORK_ERROR,
                    exception = e
                )
            }
        }
    }
}
