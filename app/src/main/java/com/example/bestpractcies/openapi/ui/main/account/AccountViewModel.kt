package com.example.bestpractcies.openapi.ui.main.account

import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.repository.main.AccountRepositoryImpl
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseViewModel
import com.example.bestpractcies.openapi.util.DataState

import com.example.bestpractcies.openapi.ui.main.account.state.AccountStateEvent.*
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import javax.inject.Inject

import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.util.*
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class AccountViewModel
@Inject
constructor(
        val sessionManager: SessionManager,
        val accountRepository: AccountRepositoryImpl
)
    : BaseViewModel<AccountViewState>()
{

    override fun handleNewData(data: AccountViewState) {
        data.accountProperties?.let { accountProperties ->
            setAccountPropertiesData(accountProperties)
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<AccountViewState>> = when(stateEvent){

                is GetAccountPropertiesEvent -> {
                    accountRepository.getAccountProperties(
                            stateEvent = stateEvent,
                            authToken = authToken
                    )
                }

                is UpdateAccountPropertiesEvent -> {
                    accountRepository.saveAccountProperties(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            email = stateEvent.email,
                            username = stateEvent.username
                    )
                }

                is ChangePasswordEvent -> {
                    accountRepository.updatePassword(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            currentPassword = stateEvent.currentPassword,
                            newPassword = stateEvent.newPassword,
                            confirmNewPassword = stateEvent.confirmNewPassword
                    )
                }

                else -> {
                    flow{
                        emit(
                                DataState.error<AccountViewState>(
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

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout(){
        sessionManager.logout()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
