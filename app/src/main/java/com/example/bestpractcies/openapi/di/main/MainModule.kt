package com.example.bestpractcies.openapi.di.main

import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.persistence.AppDatabase
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao
import com.example.bestpractcies.openapi.repository.main.*
import com.example.bestpractcies.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit


@Module
object MainModule {

    @JvmStatic
    @MainScope
    @Provides
    fun provideOpenApiAccountService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
                .build()
                .create(OpenApiMainService::class.java)
    }

    @FlowPreview
    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
            openApiMainService: OpenApiMainService,
            accountPropertiesDao: AccountPropertiesDao,
            sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepositoryImpl(
                openApiMainService,
                accountPropertiesDao,
                sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
            openApiMainService: OpenApiMainService,
            blogPostDao: BlogPostDao,
            sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepositoryImpl(openApiMainService, blogPostDao, sessionManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
            openApiMainService: OpenApiMainService,
            blogPostDao: BlogPostDao,
            sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepositoryImpl(openApiMainService, blogPostDao, sessionManager)
    }
}