package com.tpc.nudj.repository.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tpc.nudj.R
import javax.inject.Inject

class GoogleSignInClient @Inject constructor(){

    suspend fun signIn(context: Context): String? {
        val credentialManager = CredentialManager.create(context)
        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.WEB_CLIENT_ID))
                        .setAutoSelectEnabled(false)
                        .build()
                )
                .build()

            val response = credentialManager.getCredential(request = request, context = context)
            handleSignIn(response)
        } catch (e: Exception) {
            Log.e("GoogleSignInClient", "Error during Google sign-in: ${e.message}", e)
            null
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): String? {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        }
        return null
    }
}
