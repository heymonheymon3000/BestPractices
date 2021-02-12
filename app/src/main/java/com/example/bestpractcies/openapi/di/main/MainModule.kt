package com.example.bestpractcies.openapi.di.main

import com.example.bestpractcies.openapi.api.auth.OpenApiAuthService
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.persistence.AppDatabase
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao
import com.example.bestpractcies.openapi.repository.main.AccountRepository
import com.example.bestpractcies.openapi.repository.main.BlogRepository
import com.example.bestpractcies.openapi.repository.main.CreateBlogRepository
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

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
            openApiMainService: OpenApiMainService,
            blogPostDao: BlogPostDao,
            sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateBlogRepository(
            openApiMainService: OpenApiMainService,
            blogPostDao: BlogPostDao,
            sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
    }
}