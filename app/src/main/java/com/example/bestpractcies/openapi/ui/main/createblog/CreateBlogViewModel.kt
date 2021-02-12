package com.example.bestpractcies.openapi.ui.main.createblog

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bestpractcies.openapi.repository.main.CreateBlogRepository
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseViewModel
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.Loading
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogStateEvent
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogViewState.*
import com.example.bestpractcies.openapi.util.AbsentLiveData
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
        val createBlogRepository: CreateBlogRepository,
        val sessionManager: SessionManager
): BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {

    @InternalCoroutinesApi
    override fun handleStateEvent(
            stateEvent: CreateBlogStateEvent
    ): LiveData<DataState<CreateBlogViewState>> {

        when(stateEvent){

            is CreateBlogStateEvent.CreateNewBlogEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(MediaType.parse("text/plain"), stateEvent.title)
                    val body = RequestBody.create(MediaType.parse("text/plain"), stateEvent.body)

                    createBlogRepository.createNewBlogPost(
                            authToken,
                            title,
                            body,
                            stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }

            is CreateBlogStateEvent.None -> {
                return object: LiveData<DataState<CreateBlogViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                                null,
                                Loading(false),
                                null
                        )
                    }
                }
            }
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
        _viewState.value = update
    }


    fun clearNewBlogFields(){
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs(){
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData(){
        setStateEvent(CreateBlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}












