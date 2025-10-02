package com.example.wordapp.data.error

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling for the application
 */

sealed class ApiException(message: String) : Exception(message) {
    object NetworkException : ApiException("No internet connection")
    object TimeoutException : ApiException("Request timeout")
    object ServerException : ApiException("Server error occurred")
    object UnauthorizedException : ApiException("Invalid API key")
    object RateLimitException : ApiException("API rate limit exceeded")
    data class HttpException(val code: Int, val errorMessage: String) : ApiException("HTTP $code: $errorMessage")
    data class UnknownException(val originalException: Exception) : ApiException("Unknown error: ${originalException.message}")
}

object ErrorHandler {
    
    fun handleApiError(throwable: Throwable): ApiException {
        return when (throwable) {
            is UnknownHostException -> ApiException.NetworkException
            is SocketTimeoutException -> ApiException.TimeoutException
            is IOException -> ApiException.NetworkException
            is HttpException -> {
                when (throwable.code()) {
                    401 -> ApiException.UnauthorizedException
                    429 -> ApiException.RateLimitException
                    in 500..599 -> ApiException.ServerException
                    else -> ApiException.HttpException(throwable.code(), throwable.message())
                }
            }
            else -> ApiException.UnknownException(Exception(throwable))
        }
    }
    
    fun getErrorMessage(exception: ApiException): String {
        return when (exception) {
            is ApiException.NetworkException -> "Please check your internet connection and try again"
            is ApiException.TimeoutException -> "Request timed out. Please try again"
            is ApiException.ServerException -> "Server is experiencing issues. Please try again later"
            is ApiException.UnauthorizedException -> "Invalid API credentials. Please contact support"
            is ApiException.RateLimitException -> "Too many requests. Please wait a moment and try again"
            is ApiException.HttpException -> exception.errorMessage
            is ApiException.UnknownException -> "Something went wrong. Please try again"
        }
    }
    
    fun getUserFriendlyMessage(throwable: Throwable): String {
        val apiException = handleApiError(throwable)
        return getErrorMessage(apiException)
    }
}