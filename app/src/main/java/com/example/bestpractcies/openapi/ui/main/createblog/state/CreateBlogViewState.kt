package com.example.bestpractcies.openapi.ui.main.createblog.state

import android.net.Uri

data class CreateBlogViewState(

    // CreateBlogFragment vars
    var blogFields: NewBlogFields = NewBlogFields()

)
{
    data class NewBlogFields(
        var newBlogTitle: String? = null,
        var newBlogBody: String? = null,
        var newImageUri: Uri? = null
    )
}