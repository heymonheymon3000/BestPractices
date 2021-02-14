package com.example.bestpractcies.openapi.repository.main

import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.util.DataState
import com.example.bestpractcies.openapi.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface BlogRepository {

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun deleteBlogPost(
            authToken: AuthToken,
            blogPost: BlogPost,
            stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

}