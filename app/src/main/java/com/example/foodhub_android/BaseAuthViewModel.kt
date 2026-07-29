package com.example.foodhub_android

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.auth.GoogleAuthUiProvider
import com.example.foodhub_android.data.models.OAuthRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseAuthViewModel(open val foodApi: FoodApi) : ViewModel() {
    private val googleAuthUiProvider = GoogleAuthUiProvider()

    abstract fun loading()
    abstract fun onGoogleError(msg: String)
    abstract suspend fun onSocialLoginSuccess(token: String)

    fun onGoogleClicked(context: ComponentActivity){
        initiateGoogleLogin(context)
    }
    private fun initiateGoogleLogin(context: ComponentActivity){
        viewModelScope.launch {
            loading()

            try {
                val response = googleAuthUiProvider.signIn(
                    activityContext = context,
                    credentialManager = CredentialManager.create(context)
                )

                val request = OAuthRequest(
                    token = response.token,
                    provider = "google"
                )
                when (val result = safeApiCall { foodApi.oAuth(request) }) {
                    is ApiResponse.Success -> onSocialLoginSuccess(result.data.token)
                    is ApiResponse.Error -> onGoogleError(
                        result.message ?: "Google sign-in failed (${result.code})"
                    )
                    is ApiResponse.Exception -> onGoogleError(
                        result.exception.message ?: "Google sign-in failed"
                    )
                }


            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(
                    "SignInViewModel",
                    "Google sign-in failed",
                    exception
                )
                onGoogleError(exception.message ?: "Google sign-in failed")
            }
        }
    }
}
