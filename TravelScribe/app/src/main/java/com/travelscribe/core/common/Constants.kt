package com.travelscribe.core.common

/**
 * Application-wide constants.
 */
object Constants {

    /**
     * Database configuration.
     */
    object Database {
        const val NAME = "travelscribe_database"
        const val VERSION = 1
    }

    /**
     * API configuration.
     */
    object Api {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 60L
        const val WRITE_TIMEOUT_SECONDS = 60L
        const val MAX_RETRIES = 3
    }

    /**
     * Audio recording configuration.
     */
    object Audio {
        const val SAMPLE_RATE = 44100
        const val BIT_RATE = 128000
        const val CHANNELS = 1
        const val MAX_DURATION_MS = 10 * 60 * 1000L // 10 minutes
        const val MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024L // 50 MB
        const val AMPLITUDE_UPDATE_INTERVAL_MS = 100L
        const val AUDIO_DIRECTORY = "recordings"
    }

    /**
     * Pagination configuration.
     */
    object Pagination {
        const val DEFAULT_PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = 40
    }

    /**
     * Date and time formats.
     */
    object DateFormat {
        const val ISO_DATE = "yyyy-MM-dd"
        const val ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DISPLAY_DATE = "MMM dd, yyyy"
        const val DISPLAY_TIME = "HH:mm"
        const val DISPLAY_DATE_TIME = "MMM dd, yyyy HH:mm"
    }

    /**
     * Navigation routes.
     */
    object NavRoutes {
        const val TRIPS = "trips"
        const val TRIP_DETAIL = "trip/{tripId}"
        const val DAY_DETAIL = "trip/{tripId}/day/{dayId}"
        const val RECORDING = "trip/{tripId}/day/{dayId}/record"
        const val LOG_DETAIL = "log/{logId}"
        const val SETTINGS = "settings"

        fun tripDetail(tripId: Long) = "trip/$tripId"
        fun dayDetail(tripId: Long, dayId: Long) = "trip/$tripId/day/$dayId"
        fun recording(tripId: Long, dayId: Long) = "trip/$tripId/day/$dayId/record"
        fun logDetail(logId: Long) = "log/$logId"
    }

    /**
     * Argument keys for navigation.
     */
    object NavArgs {
        const val TRIP_ID = "tripId"
        const val DAY_ID = "dayId"
        const val LOG_ID = "logId"
    }

    /**
     * Request codes and permission constants.
     */
    object Permissions {
        const val REQUEST_CODE_AUDIO = 1001
    }

    /**
     * Supported languages for transcription.
     */
    object Languages {
        const val HINDI = "hi"
        const val ENGLISH = "en"
        val SUPPORTED = listOf(HINDI, ENGLISH)
        const val TARGET_LANGUAGE = ENGLISH
    }
}
