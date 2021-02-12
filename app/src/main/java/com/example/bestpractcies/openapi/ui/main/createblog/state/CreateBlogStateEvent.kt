package com.example.bestpractcies.openapi.ui.main.createblog.state

import okhttp3.MultipartBody


sealed class CreateBlogStateEvent {

    data class CreateNewBlogEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part
    ): CreateBlogStateEvent()

    class None: CreateBlogStateEvent()
}