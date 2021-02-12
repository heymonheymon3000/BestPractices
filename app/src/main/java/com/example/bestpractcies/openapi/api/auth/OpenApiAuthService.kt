package com.example.bestpractcies.openapi.api.auth

import androidx.lifecycle.LiveData
import com.example.bestpractcies.openapi.api.auth.network.responses.LoginResponse
import com.example.bestpractcies.openapi.api.auth.network.responses.RegistrationResponse
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.util.GenericApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

@AuthScope
interface OpenApiAuthService {
    @POST("account/login")
    @FormUrlEncoded
    fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): LiveData<GenericApiResponse<LoginResponse>>

    @POST("account/register")
    @FormUrlEncoded
    fun register(
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password2") password2: String
    ): LiveData<GenericApiResponse<RegistrationResponse>>
}