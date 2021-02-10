package com.example.bestpractcies.openapi.repository.main

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.api.main.network.responses.BlogListSearchResponse
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao
import com.example.bestpractcies.openapi.repository.JobManager
import com.example.bestpractcies.openapi.repository.NetworkBoundResource
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.util.ApiSuccessResponse
import com.example.bestpractcies.openapi.util.DateUtils
import com.example.bestpractcies.openapi.util.GenericApiResponse
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
        val openApiMainService: OpenApiMainService,
        val blogPostDao: BlogPostDao,
        val sessionManager: SessionManager
): JobManager("BlogRepository")
{

    @InternalCoroutinesApi
    fun searchBlogPosts(
            authToken: AuthToken,
            query: String
    ): LiveData<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
        ) {
            // if network is down, view cache only and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()){ viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                    response: ApiSuccessResponse<BlogListSearchResponse>
            ) {

                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for(blogPostResponse in response.body.results){
                    blogPostList.add(
                            BlogPost(
                                    pk = blogPostResponse.pk,
                                    title = blogPostResponse.title,
                                    slug = blogPostResponse.slug,
                                    body = blogPostResponse.body,
                                    image = blogPostResponse.image,
                                    date_updated = DateUtils.convertServerStringDateToLong(
                                            blogPostResponse.date_updated
                                    ),
                                    username = blogPostResponse.username
                            )
                    )
                }
                updateLocalDb(blogPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return openApiMainService.searchListBlogPosts(
                        "Token ${authToken.token!!}",
                        query = query
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.getAllBlogPosts()
                        .switchMap {
                            object: LiveData<BlogViewState>(){
                                override fun onActive() {
                                    super.onActive()
                                    value = BlogViewState(
                                            BlogViewState.BlogFields(
                                                    blogList = it
                                            )
                                    )
                                }
                            }
                        }
            }

            @SuppressLint("BinaryOperationInTimber")
            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                // loop through list and update the local db
                if(cacheObject != null){
                    withContext(Dispatchers.IO) {
                        for(blogPost in cacheObject){
                            try{
//                                // Launch each insert as a separate job to be executed in parallel
//                                val j = launch {
//                                    Timber.d("updateLocalDb: inserting blog: $blogPost")
//                                    blogPostDao.insert(blogPost)
//                                }
//                                j.join() // wait for completion before proceeding to next


                                // Launch each insert as a separate job to be executed in parallel
                                launch {
                                    Timber.d("updateLocalDb: inserting blog: $blogPost")
                                    blogPostDao.insert(blogPost)
                                }
                            }catch (e: Exception){
                                Timber.e("updateLocalDb: error updating cache data on blog post with slug: ${blogPost.slug}. " +
                                        "${e.message}")
                                // Could send an error report here or something but I don't think you should throw an error to the UI
                                // Since there could be many blog posts being inserted/updated.
                            }
                        }
                    }
                }
                else{
                    Timber.d("updateLocalDb: blog post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }
}
















