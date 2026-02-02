package com.travelscribe.data.remote.api

import com.travelscribe.data.remote.dto.TranscriptionRequestDto
import com.travelscribe.data.remote.dto.TranscriptionResponseDto
import com.travelscribe.data.remote.dto.TranscriptionStatusDto
import com.travelscribe.data.remote.dto.UploadResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit API service for LLM transcription endpoints.
 */
interface LlmApiService {

    /**
     * Transcribes audio to structured text and expenses.
     *
     * @param request The transcription request containing audio data
     * @return Response containing narrative text and extracted expenses
     */
    @POST("api/v1/transcribe")
    suspend fun transcribeAudio(
        @Body request: TranscriptionRequestDto
    ): Response<TranscriptionResponseDto>

    /**
     * Uploads an audio file to the server.
     *
     * @param audio The audio file as multipart
     * @return Response containing the uploaded file URL
     */
    @Multipart
    @POST("api/v1/upload")
    suspend fun uploadAudio(
        @Part audio: MultipartBody.Part
    ): Response<UploadResponseDto>

    /**
     * Gets the status of an async transcription request.
     *
     * @param requestId The transcription request ID
     * @return Response containing the current status
     */
    @GET("api/v1/transcription/{requestId}/status")
    suspend fun getTranscriptionStatus(
        @Path("requestId") requestId: String
    ): Response<TranscriptionStatusDto>

    /**
     * Cancels an ongoing transcription request.
     *
     * @param requestId The transcription request ID
     * @return Empty response indicating success
     */
    @POST("api/v1/transcription/{requestId}/cancel")
    suspend fun cancelTranscription(
        @Path("requestId") requestId: String
    ): Response<Unit>
}
