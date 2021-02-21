package com.example.bestpractcies.openapi.repository.main

import android.util.Log
import com.example.bestpractcies.openapi.api.GenericResponse
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.repository.NetworkBoundResource
import com.example.bestpractcies.openapi.repository.safeApiCall
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import com.example.bestpractcies.openapi.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
@MainScope
class AccountRepositoryImpl
@Inject
constructor(
        val openApiMainService: OpenApiMainService,
        val accountPropertiesDao: AccountPropertiesDao,
        val sessionManager: SessionManager
): AccountRepository
{

    private val TAG: String = "AppDebug"

    override fun getAccountProperties(
            authToken: AuthToken,
            stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>> {
        return object: NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    openApiMainService
                            .getAccountProperties("Token ${authToken.token!!}")
                },
                cacheCall = {
                    accountPropertiesDao.searchByPk(authToken.account_pk!!)
                }

        ){
            override suspend fun updateCache(networkObject: AccountProperties) {
                Timber.d("updateCache: $networkObject ")
                accountPropertiesDao.updateAccountProperties(
                        networkObject.pk,
                        networkObject.email,
                        networkObject.username
                )
            }

            override fun handleCacheSuccess(
                    resultObj: AccountProperties
            ): DataState<AccountViewState> {
                return DataState.data(
                        response = null,
                        data = AccountViewState(
                                accountProperties = resultObj
                        ),
                        stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun saveAccountProperties(
            authToken: AuthToken,
            email: String,
            username: String,
            stateEvent: StateEvent
    ) = flow{
        val apiResult = safeApiCall(IO){
            openApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    email,
                    username
            )
        }
        emit(
                object: ApiResponseHandler<AccountViewState, GenericResponse>(
                        response = apiResult,
                        stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(
                            resultObj: GenericResponse
                    ): DataState<AccountViewState> {

                        val updatedAccountProperties = openApiMainService
                                .getAccountProperties("Token ${authToken.token!!}")

                        accountPropertiesDao.updateAccountProperties(
                                pk = updatedAccountProperties.pk,
                                email = updatedAccountProperties.email,
                                username = updatedAccountProperties.username
                        )

                        return DataState.data(
                                data = null,
                                response = Response(
                                        message = resultObj.response,
                                        uiComponentType = UIComponentType.Toast(),
                                        messageType = MessageType.Success()
                                ),
                                stateEvent = stateEvent
                        )
                    }

                }.getResult()
        )
    }

    override fun updatePassword(
            authToken: AuthToken,
            currentPassword: String,
            newPassword: String,
            confirmNewPassword: String,
            stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            openApiMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
            )
        }
        emit(
                object: ApiResponseHandler<AccountViewState, GenericResponse>(
                        response = apiResult,
                        stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(
                            resultObj: GenericResponse
                    ): DataState<AccountViewState> {

                        return DataState.data(
                                data = null,
                                response = Response(
                                        message = resultObj.response,
                                        uiComponentType = UIComponentType.Toast(),
                                        messageType = MessageType.Success()
                                ),
                                stateEvent = stateEvent
                        )
                    }
                }.getResult()
        )
    }

}