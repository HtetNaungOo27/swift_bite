package com.example.foodhub_android.data.remote

import retrofit2.Response

sealed class ApiResponse<out T> {

    data class Success<out T>(val data: T) : ApiResponse<T>()

    data class Error(
        val code: Int,
        val message: String?
    ) : ApiResponse<Nothing>() {
        fun formatMsg(): String = "Error: $code ${message.orEmpty()}"
    }

    data class Exception(
        val exception: kotlin.Exception
    ) : ApiResponse<Nothing>()
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): ApiResponse<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            response.body()?.let { body ->
                ApiResponse.Success(body)
            } ?: ApiResponse.Error(
                code = response.code(),
                message = "Response body is empty"
            )
        } else {
            val serverMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ApiResponse.Error(
                code = response.code(),
                message = when (response.code()) {
                    401 -> "Your session has expired. Please sign in again."
                    else -> serverMessage ?: "Request failed (${response.code()})"
                }
            )
        }
    } catch (exception: kotlin.Exception) {
        ApiResponse.Exception(exception)
    }
}
