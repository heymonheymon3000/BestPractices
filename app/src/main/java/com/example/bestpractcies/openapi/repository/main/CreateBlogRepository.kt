package com.example.bestpractcies.openapi.repository.main

import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState
import com.example.bestpractcies.openapi.util.DataState
import com.example.bestpractcies.openapi.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface CreateBlogRepository {

    fun createNewBlogPost(
            authToken: AuthToken,
            title: RequestBody,
            body: RequestBody,
            image: MultipartBody.Part?,
            stateEvent: StateEvent
    ): Flow<DataState<CreateBlogViewState>>
}