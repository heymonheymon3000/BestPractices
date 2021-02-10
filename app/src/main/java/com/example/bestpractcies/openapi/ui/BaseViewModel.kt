package com.example.bestpractcies.openapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {
    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    private val _isConnectedToInternet: MutableLiveData<Boolean> = MutableLiveData()

    val viewState: LiveData<ViewState>
        get() = _viewState

    val isConnectedToInternet: LiveData<Boolean>
        get() = _isConnectedToInternet

    val dataState: LiveData<DataState<ViewState>> = Transformations
        .switchMap(_stateEvent){stateEvent ->
            stateEvent?.let {
                handleStateEvent(stateEvent)
            }
        }

    fun setStateEvent(event: StateEvent){
        _stateEvent.value = event
    }

    fun getCurrentViewStateOrNew(): ViewState{
        return viewState.value?.let<ViewState, ViewState> {
            it
        }?: initNewViewState()
    }

    abstract fun handleStateEvent(stateEvent: StateEvent): LiveData<DataState<ViewState>>

    abstract fun initNewViewState(): ViewState
}