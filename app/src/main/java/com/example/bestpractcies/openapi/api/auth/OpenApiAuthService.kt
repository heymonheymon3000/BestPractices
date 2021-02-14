package com.example.bestpractcies.openapi.api.auth

import com.example.bestpractcies.openapi.api.auth.network.responses.LoginResponse
import com.example.bestpractcies.openapi.api.auth.network.responses.RegistrationResponse
import com.example.bestpractcies.openapi.di.auth.AuthScope
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

@AuthScope
interface OpenApiAuthService {
    @POST("account/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("account/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password2") password2: String
    ): RegistrationResponse
}