package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * NetworkResult sealed class for wrapping API responses.
 * Provides Success, Error, and Loading states for unidirectional data flow.
 */
sealed class NetworkResult<out T> {
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * Error state with message and optional exception
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()
    
    /**
     * Loading state
     */
    data object Loading : NetworkResult<Nothing>()
    
    /**
     * Check if the result is successful
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if the result is an error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Check if the result is loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Get data if available, null otherwise
     */
    fun getDataOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
    
    /**
     * Map the success data to another type
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
}
