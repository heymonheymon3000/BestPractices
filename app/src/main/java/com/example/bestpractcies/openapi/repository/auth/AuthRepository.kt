package com.example.bestpractcies.openapi.repository.auth


import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.ui.auth.state.AuthViewState
import com.example.bestpractcies.openapi.util.DataState
import com.example.bestpractcies.openapi.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@FlowPreview
@AuthScope
interface AuthRepository {

    fun attemptLogin(
            stateEvent: StateEvent,
            email: String,
            password: String
    ): Flow<DataState<AuthViewState>>

    fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>>

    fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>>

    fun saveAuthenticatedUserToPrefs(email: String)

    fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState>

}
