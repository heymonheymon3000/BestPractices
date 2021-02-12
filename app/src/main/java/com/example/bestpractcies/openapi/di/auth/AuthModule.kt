package com.example.bestpractcies.openapi.di.auth

import android.content.SharedPreferences
import com.example.bestpractcies.openapi.api.auth.OpenApiAuthService
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.auth.AuthTokenDao
import com.example.bestpractcies.openapi.repository.auth.AuthRepository
import com.example.bestpractcies.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object AuthModule{

    @JvmStatic
    @AuthScope
    @Provides
    fun provideOpenApiAuthService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
        ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            preferences,
            editor
        )
    }
}