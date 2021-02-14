package com.example.bestpractcies.openapi.repository.main

import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import com.example.bestpractcies.openapi.util.DataState
import com.example.bestpractcies.openapi.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@FlowPreview
@MainScope
interface AccountRepository {

    fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>
}