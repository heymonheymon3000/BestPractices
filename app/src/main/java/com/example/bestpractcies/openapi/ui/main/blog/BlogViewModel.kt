package com.example.bestpractcies.openapi.ui.main.blog

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.repository.main.BlogRepository
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseViewModel
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent.*
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.util.AbsentLiveData
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
        private val sessionManager: SessionManager,
        private val blogRepository: BlogRepository,
        private val sharedPreferences: SharedPreferences,
        private val requestManager: RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState>(){
    @InternalCoroutinesApi
    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when(stateEvent){

            is BlogSearchEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                            authToken,
                            viewState.value!!.blogFields.searchQuery
                    )
                }?: AbsentLiveData.create()
            }

            is None ->{
                AbsentLiveData.create()
            }
            else -> {
                // none
                AbsentLiveData.create()
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun setQuery(query: String){
        val update = getCurrentViewStateOrNew()
        update.blogFields.searchQuery = query
        _viewState.value = update
    }

    fun setBlogListData(blogList: List<BlogPost>){
        val update = getCurrentViewStateOrNew()
        update.blogFields.blogList = blogList
        _viewState.value = update
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











