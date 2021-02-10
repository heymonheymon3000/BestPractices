package com.example.bestpractcies.openapi.ui.main.blog.state

import com.example.bestpractcies.openapi.models.main.blog.BlogPost

data class BlogViewState (
    // BlogFragment vars
    var blogFields: BlogFields = BlogFields()
)
{
    data class BlogFields(
            var blogList: List<BlogPost> = ArrayList<BlogPost>(),
            var searchQuery: String = ""
    )
}
