package com.example.bestpractcies.openapi.di.main

import com.example.bestpractcies.openapi.api.auth.OpenApiAuthService
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.repository.main.AccountRepository
import com.example.bestpractcies.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiAccountService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
                .build()
                .create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
            openApiMainService: OpenApiMainService,
            accountPropertiesDao: AccountPropertiesDao,
            sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
                openApiMainService,
                accountPropertiesDao,
                sessionManager
        )
    }
}