package com.example.bestpractcies.openapi.ui.main.createblog

import android.net.Uri
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.repository.main.CreateBlogRepositoryImpl

import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseViewModel
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogStateEvent

import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState.*
import com.example.bestpractcies.openapi.util.*
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

@UseExperimental(ExperimentalCoroutinesApi::class)
@FlowPreview
@MainScope
class CreateBlogViewModel
@Inject
constructor(
        val createBlogRepository: CreateBlogRepositoryImpl,
        val sessionManager: SessionManager
): BaseViewModel<CreateBlogViewState>() {


    override fun handleNewData(data: CreateBlogViewState) {

        setNewBlogFields(
                data.blogFields.newBlogTitle,
                data.blogFields.newBlogBody,
                data.blogFields.newImageUri
        )
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<CreateBlogViewState>> = when(stateEvent){

                is CreateBlogStateEvent.CreateNewBlogEvent -> {
                    val title = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.title
                    )
                    val body = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.body
                    )

                    createBlogRepository.createNewBlogPost(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            title = title,
                            body = body,
                            image = stateEvent.image
                    )
                }

                else -> {
                    flow{
                        emit(
                                DataState.error<CreateBlogViewState>(
                                        response = Response(
                                                message = INVALID_STATE_EVENT,
                                                uiComponentType = UIComponentType.None(),
                                                messageType = MessageType.Error()
                                        ),
                                        stateEvent = stateEvent
                                )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }
    }

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?){
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let{ newBlogFields.newBlogTitle = it }
        body?.let{ newBlogFields.newBlogBody = it }
        uri?.let{ newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }

    fun clearNewBlogFields(){
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}





