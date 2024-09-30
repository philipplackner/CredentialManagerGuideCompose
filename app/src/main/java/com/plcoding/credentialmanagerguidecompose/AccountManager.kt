package com.plcoding.credentialmanagerguidecompose

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.*
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AccountManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val accountService = Retrofit.Builder()
        .baseUrl("http://localhost:8000/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(AccountService::class.java)

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun signUp(username: String): SignUpResult {
        // Step 1: https://developer.android.com/identity/sign-in/credential-manager#create-passkey
        var requestJson = accountService.getRequestJson(username)
        requestJson = Base64.decode(requestJson).decodeToString()
        Log.d(TAG, "Request JSON: $requestJson")

        val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
            // Contains the request in JSON format. Uses the standard WebAuthn
            // web JSON spec.
            requestJson = requestJson
        )

        Log.d(TAG, "Creating credential...")
        try {
            val createCredentialResult = credentialManager.createCredential(context = context,
                request = createPublicKeyCredentialRequest
            )
            val b64 = Base64.encode((createCredentialResult as CreatePublicKeyCredentialResponse).registrationResponseJson.toByteArray())
            val verifyResult = accountService.verifyRegistration(username, createCredentialResult.registrationResponseJson)
            return if (verifyResult.isSuccessful) {
                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                SignUpResult.Success(username)
            } else {
                val error = verifyResult.errorBody()?.string()
                Log.e(TAG, "Registration failed: $error")
                Toast.makeText(context, "Registration failed: check log", Toast.LENGTH_SHORT).show()
                SignUpResult.Failure
            }
        } catch(e: CreateCredentialException) {
            e.printStackTrace()
            Toast.makeText(context, "Exception occurs, check log...", Toast.LENGTH_SHORT).show()
            return SignUpResult.Failure
        } catch(e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Exception occurs, check log...", Toast.LENGTH_SHORT).show()
            return SignUpResult.Failure
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun signIn(username: String): SignInResult {
        return try {
            var requestJson = accountService.getAuthenticationOptions(username)
            requestJson = Base64.decode(requestJson).decodeToString()
            Log.d(TAG, "Request JSON: $requestJson")
            val credentialResponse = credentialManager.getCredential(context = context,
                request = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson))

            ))
            val credential = credentialResponse.credential as? PublicKeyCredential
                ?: return SignInResult.Failure
            val result = accountService.verifyAuthentication(username, credential.authenticationResponseJson)
            if (result.isSuccessful) {
                SignInResult.Success(username)
            } else {
                val error = result.errorBody()?.string()
                Log.e(TAG, "Sign in failed: $error")
                Toast.makeText(context, "Sign in failed: check log", Toast.LENGTH_SHORT).show()
                SignInResult.Failure
            }
        } catch(e: GetCredentialCancellationException) {
            e.printStackTrace()
            SignInResult.Cancelled
        } catch(e: NoCredentialException) {
            e.printStackTrace()
            SignInResult.NoCredentials
        } catch(e: GetCredentialException) {
            e.printStackTrace()
            SignInResult.Failure
        }
    }

    companion object {
        private const val TAG = "AccountManager"
    }
}