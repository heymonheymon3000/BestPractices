package com.example.bestpractcies.openapi.ui.main.blog.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.example.bestpractcies.openapi.persistence.BlogQueryUtils
import com.example.bestpractcies.openapi.repository.main.BlogRepository
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseViewModel
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.Loading
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent.*
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.util.AbsentLiveData
import com.example.bestpractcies.openapi.util.PreferenceKeys.Companion.BLOG_FILTER
import com.example.bestpractcies.openapi.util.PreferenceKeys.Companion.BLOG_ORDER
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.MediaType
import okhttp3.RequestBody
import timber.log.Timber
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
        private val sessionManager: SessionManager,
        private val blogRepository: BlogRepository,
        private val sharedPreferences: SharedPreferences,
        private val editor: SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>(){

    init {
        setBlogFilter(
                sharedPreferences.getString(
                        BLOG_FILTER,
                        BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
                )
        )

        sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(it)
        }
    }

    @InternalCoroutinesApi
    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when(stateEvent){
            is BlogSearchEvent ->{
                Timber.d("Blog Search Event...")
                clearLayoutManagerState()
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                            authToken = authToken,
                            query = getSearchQuery(),
                            filterAndOrder = getOrder() + getFilter(),
                            page = getPage()

                    )
                }?: AbsentLiveData.create()
            }

            is RestoreBlogListFromCache -> {
                return blogRepository.restoreBlogListFromCache(
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                )
            }

            is CheckAuthorOfBlogPost -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.isAuthorOfBlogPost(
                            authToken = authToken,
                            slug = getSlug()
                    )
                }?: AbsentLiveData.create()
            }

            is DeleteBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                            authToken = authToken,
                            blogPost = getBlogPost()
                    )
                }?: AbsentLiveData.create()
            }

            is UpdateBlogPostEvent -> {

                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.title
                    )
                    val body = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.body
                    )

                    blogRepository.updateBlogPost(
                            authToken = authToken,
                            slug = getSlug(),
                            title = title,
                            body = body,
                            image = stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }

            is None ->{
                return object: LiveData<DataState<BlogViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }

            else -> {
                return object: LiveData<DataState<BlogViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}











