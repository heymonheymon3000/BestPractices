package com.example.bestpractcies.openapi.repository.main

import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.api.main.network.responses.BlogCreateUpdateResponse
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao
import com.example.bestpractcies.openapi.repository.safeApiCall
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState
import com.example.bestpractcies.openapi.util.*
import com.example.bestpractcies.openapi.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@FlowPreview
@MainScope
class CreateBlogRepositoryImpl
@Inject
constructor(
        val openApiMainService: OpenApiMainService,
        val blogPostDao: BlogPostDao,
        val sessionManager: SessionManager
): CreateBlogRepository {

    private val TAG: String = "AppDebug"

    override fun createNewBlogPost(
            authToken: AuthToken,
            title: RequestBody,
            body: RequestBody,
            image: MultipartBody.Part?,
            stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            openApiMainService.createBlog(
                    "Token ${authToken.token!!}",
                    title,
                    body,
                    image
            )
        }

        emit(
                object: ApiResponseHandler<CreateBlogViewState, BlogCreateUpdateResponse>(
                        response = apiResult,
                        stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<CreateBlogViewState> {

                        // If they don't have a paid membership account it will still return a 200
                        // Need to account for that
                        if (!resultObj.response.equals(RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                            val updatedBlogPost = resultObj.toBlogPost()
                            blogPostDao.insert(updatedBlogPost)
                        }
                        return DataState.data(
                                response = Response(
                                        message = resultObj.response,
                                        uiComponentType = UIComponentType.Dialog(),
                                        messageType = MessageType.Success()
                                ),
                                data = null,
                                stateEvent = stateEvent
                        )
                    }
                }.getResult()
        )
    }

}

