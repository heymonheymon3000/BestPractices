package com.example.bestpractcies.openapi.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.bestpractcies.openapi.fragments.main.account.AccountFragmentFactory
import com.example.bestpractcies.openapi.fragments.main.blog.BlogFragmentFactory
import com.example.bestpractcies.openapi.fragments.main.create_blog.CreateBlogFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object MainFragmentsModule {

    @JvmStatic
    @MainScope
    @Provides
    @Named("AccountFragmentFactory")
    fun provideAccountFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return AccountFragmentFactory(
            viewModelFactory
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("BlogFragmentFactory")
    fun provideBlogFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestOptions: RequestOptions,
        requestManager: RequestManager
    ): FragmentFactory {
        return BlogFragmentFactory(
            viewModelFactory,
            requestOptions,
            requestManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("CreateBlogFragmentFactory")
    fun provideCreateBlogFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager
    ): FragmentFactory {
        return CreateBlogFragmentFactory(
            viewModelFactory,
            requestManager
        )
    }

}