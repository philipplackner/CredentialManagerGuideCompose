package com.plcoding.credentialmanagerguidecompose

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AccountService {
    @GET("get-register-options")
    suspend fun getRequestJson(@Query("username") username: String): String

    @POST("verify-registration")
    suspend fun verifyRegistration(@Query("username") username: String, @Body responseJson: String): Response<String>

    @GET("get-authentication-options")
    suspend fun getAuthenticationOptions(@Query("username") username: String): String

    @POST("verify-authentication")
    suspend fun verifyAuthentication(@Query("username") username: String, @Body responseJson: String): Response<String>
}