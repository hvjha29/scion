package com.travelscribe.domain.usecase.transcription

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.repository.TranscriptionRepository
import com.travelscribe.domain.repository.TravelDayRepository
import com.travelscribe.domain.repository.TravelLogRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for transcribing audio and creating a travel log.
 * Orchestrates the full flow from audio recording to saved travel log.
 */
class TranscribeAudioUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val travelDayRepository: TravelDayRepository,
    private val travelLogRepository: TravelLogRepository
) {
    /**
     * Transcribes audio and creates a travel log.
     *
     * @param tripId The trip ID
     * @param date The date for the travel day
     * @param audioFilePath Path to the audio file
     * @param audioDurationMs Duration of the audio in milliseconds
     * @param sourceLanguages Source languages in the audio
     * @return Resource containing the created travel log
     */
    suspend operator fun invoke(
        tripId: Long,
        date: LocalDate,
        audioFilePath: String,
        audioDurationMs: Long,
        sourceLanguages: List<String> = listOf("hi", "en")
    ): Resource<TravelLog> {
        // 1. Get or create the travel day
        val dayResult = travelDayRepository.getOrCreateTravelDay(tripId, date)
        val travelDay = when (dayResult) {
            is Resource.Success -> dayResult.data
            is Resource.Error -> return Resource.Error(
                message = dayResult.message,
                code = dayResult.code,
                exception = dayResult.exception
            )
            is Resource.Loading -> return Resource.Error("Unexpected loading state")
        }

        // 2. Call the transcription API
        val transcriptionResult = transcriptionRepository.transcribeAudio(
            audioFilePath = audioFilePath,
            sourceLanguages = sourceLanguages,
            targetLanguage = "en"
        )

        val transcription = when (transcriptionResult) {
            is Resource.Success -> transcriptionResult.data
            is Resource.Error -> return Resource.Error(
                message = transcriptionResult.message,
                code = transcriptionResult.code,
                exception = transcriptionResult.exception
            )
            is Resource.Loading -> return Resource.Error("Unexpected loading state")
        }

        // 3. Create the travel log with transcription results
        val travelLog = TravelLog(
            dayId = travelDay.id,
            rawAudioPath = audioFilePath,
            audioDurationMs = audioDurationMs,
            transcribedText = transcription.narrative,
            originalLanguages = transcription.detectedLanguages,
            expenses = transcription.expenses,
            isEdited = false
        )

        // 4. Save to database
        return travelLogRepository.createTravelLog(travelLog)
    }
}
