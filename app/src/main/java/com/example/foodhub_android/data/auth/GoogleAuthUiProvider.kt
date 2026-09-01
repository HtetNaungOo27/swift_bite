package com.example.foodhub_android.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.foodhub_android.GoogleServerClientID
import com.example.foodhub_android.data.models.GoogleAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

//class GoogleAuthUiProvider {
//    suspend fun signIn(
//        activityContext: Context,
//        credentialManager: CredentialManager
//    ): GoogleAccount{
//        val creds = credentialManager.getCredential(
//            activityContext,
//            getCredentialRequest()
//        ).credential
//        return handleCredentials(creds)
//    }
//
//    fun handleCredentials(creds: Credential): GoogleAccount{
//        when{
//            creds is CustomCredential && creds.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
//                val googleIdTokenCredential = creds as GoogleIdTokenCredential
//                Log.d("GoogleAuthUiProvider", "GoogleIdTokenCredential : $googleIdTokenCredential")
//                return GoogleAccount(
//                    token = googleIdTokenCredential.idToken,
//                    displayName = googleIdTokenCredential.displayName?: "",
//                    profileImageUrl = googleIdTokenCredential.profilePictureUri.toString()
//
//                )
//            }
//            else -> {
//                throw IllegalStateException("Invalid Credential type.")
//            }
//        }
//
//
//    }
//    private fun getCredentialRequest(): GetCredentialRequest {
//        return GetCredentialRequest.Builder()
//            .addCredentialOption(
//                GetSignInWithGoogleOption.Builder(
//                    GoogleServerClientID)
//                    .build()
//            )
//            .build()
//    }
//}

class GoogleAuthUiProvider {

    suspend fun signIn(
        activityContext: Context,
        credentialManager: CredentialManager
    ): GoogleAccount {
        val credential = try {
            credentialManager.getCredential(
                context = activityContext,
                request = getCredentialRequest()
            ).credential
        } catch (_: NoCredentialException) {
            // Some devices do not return an account for the explicit button option
            // until the broader account chooser has been requested once.
            credentialManager.getCredential(
                context = activityContext,
                request = getAccountChooserRequest()
            ).credential
        }

        return handleCredential(credential)
    }

    private fun handleCredential(credential: Credential): GoogleAccount {
        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            Log.d(
                "GoogleAuthUiProvider",
                "Signed in as ${googleCredential.id}"
            )

            return GoogleAccount(
                token = googleCredential.idToken,
                displayName = googleCredential.displayName.orEmpty(),
                profileImageUrl =
                    googleCredential.profilePictureUri?.toString().orEmpty()
            )
        }

        throw IllegalStateException(
            "Unsupported credential type: ${credential.type}"
        )
    }

    private fun getCredentialRequest(): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(
                    GoogleServerClientID
                ).build()
            )
            .build()
    }

    private fun getAccountChooserRequest(): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(GoogleServerClientID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
    }
}
