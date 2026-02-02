package com.travelscribe.core.common

/**
 * A generic wrapper class for handling success/error states in a functional way.
 * Used throughout the domain layer to represent operation outcomes.
 *
 * @param T The type of data in case of success
 */
sealed class Resource<out T> {
    
    /**
     * Represents a successful operation with data.
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * Represents a failed operation with error details.
     */
    data class Error(
        val message: String,
        val code: ErrorCode = ErrorCode.UNKNOWN,
        val exception: Throwable? = null
    ) : Resource<Nothing>()
    
    /**
     * Represents an ongoing operation.
     */
    data class Loading(val progress: Float? = null) : Resource<Nothing>()

    /**
     * Returns true if this is a Success.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an Error.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns true if this is Loading.
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if Success, null otherwise.
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Returns the data if Success, throws exception otherwise.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: Exception(message)
        is Loading -> throw IllegalStateException("Resource is still loading")
    }

    /**
     * Returns the data if Success, default value otherwise.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = getOrNull() ?: default

    /**
     * Returns the error message if Error, null otherwise.
     */
    fun errorMessageOrNull(): String? = (this as? Error)?.message

    /**
     * Maps the data if Success, preserves Error/Loading otherwise.
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Flat maps the data if Success, preserves Error/Loading otherwise.
     */
    inline fun <R> flatMap(transform: (T) -> Resource<R>): Resource<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }

    /**
     * Executes action if Success.
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes action if Error.
     */
    inline fun onError(action: (Error) -> Unit): Resource<T> {
        if (this is Error) action(this)
        return this
    }

    /**
     * Executes action if Loading.
     */
    inline fun onLoading(action: (Float?) -> Unit): Resource<T> {
        if (this is Loading) action(progress)
        return this
    }

    companion object {
        /**
         * Creates a Success Resource.
         */
        fun <T> success(data: T): Resource<T> = Success(data)

        /**
         * Creates an Error Resource.
         */
        fun error(
            message: String,
            code: ErrorCode = ErrorCode.UNKNOWN,
            exception: Throwable? = null
        ): Resource<Nothing> = Error(message, code, exception)

        /**
         * Creates a Loading Resource.
         */
        fun loading(progress: Float? = null): Resource<Nothing> = Loading(progress)
    }
}

/**
 * Standard error codes for the application.
 */
enum class ErrorCode {
    UNKNOWN,
    NETWORK_ERROR,
    SERVER_ERROR,
    API_ERROR,
    TIMEOUT,
    NO_INTERNET,
    UNAUTHORIZED,
    NOT_FOUND,
    VALIDATION_ERROR,
    STORAGE_ERROR,
    PERMISSION_DENIED,
    AUDIO_ERROR,
    TRANSCRIPTION_FAILED,
    DATABASE_ERROR
}

/**
 * Extension to convert Kotlin Result to Resource.
 */
fun <T> Result<T>.toResource(): Resource<T> = fold(
    onSuccess = { Resource.Success(it) },
    onFailure = { Resource.Error(it.message ?: "Unknown error", exception = it) }
)
