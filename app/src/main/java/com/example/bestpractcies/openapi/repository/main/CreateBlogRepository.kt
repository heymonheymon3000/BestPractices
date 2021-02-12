package com.example.bestpractcies.openapi.repository.main

import androidx.lifecycle.LiveData
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.api.main.network.responses.BlogCreateUpdateResponse
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao
import com.example.bestpractcies.openapi.repository.JobManager
import com.example.bestpractcies.openapi.repository.NetworkBoundResource
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.Response
import com.example.bestpractcies.openapi.ui.ResponseType
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState
import com.example.bestpractcies.openapi.util.AbsentLiveData
import com.example.bestpractcies.openapi.util.ApiSuccessResponse
import com.example.bestpractcies.openapi.util.DateUtils
import com.example.bestpractcies.openapi.util.GenericApiResponse
import com.example.bestpractcies.openapi.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
        val openApiMainService: OpenApiMainService,
        val blogPostDao: BlogPostDao,
        val sessionManager: SessionManager
): JobManager("CreateBlogRepository") {

    @InternalCoroutinesApi
    fun createNewBlogPost(
            authToken: AuthToken,
            title: RequestBody,
            body: RequestBody,
            image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
                NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                        sessionManager.isConnectedToTheInternet(),
                        true,
                        true,
                        false
                ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {

                // If they don't have a paid membership account it will still return a 200
                // Need to account for that
                if (!response.body.response.equals(RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                    val updatedBlogPost = BlogPost(
                            response.body.pk,
                            response.body.title,
                            response.body.slug,
                            response.body.body,
                            response.body.image,
                            DateUtils.convertServerStringDateToLong(response.body.date_updated),
                            response.body.username
                    )
                    updateLocalDb(updatedBlogPost)
                }

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                            DataState.data(
                                    null,
                                    Response(response.body.response, ResponseType.Dialog())
                            )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                        "Token ${authToken.token!!}",
                        title,
                        body,
                        image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }
}
















