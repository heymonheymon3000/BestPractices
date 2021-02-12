package com.example.bestpractcies.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.example.bestpractcies.openapi.api.GenericResponse
import com.example.bestpractcies.openapi.api.main.OpenApiMainService
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.repository.JobManager
import com.example.bestpractcies.openapi.repository.NetworkBoundResource
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.Response
import com.example.bestpractcies.openapi.ui.ResponseType
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import com.example.bestpractcies.openapi.util.AbsentLiveData
import com.example.bestpractcies.openapi.util.ApiSuccessResponse
import com.example.bestpractcies.openapi.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject


@MainScope
class AccountRepository
@Inject
constructor(
        val openApiMainService: OpenApiMainService,
        val accountPropertiesDao: AccountPropertiesDao,
        val sessionManager: SessionManager
): JobManager("AccountRepository")
{
    @InternalCoroutinesApi
    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
        ){

            // if network is down, view the cache and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()){ viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                        .switchMap {
                            object: LiveData<AccountViewState>(){
                                override fun onActive() {
                                    super.onActive()
                                    value = AccountViewState(it)
                                }
                            }
                        }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                            cacheObject.pk,
                            cacheObject.email,
                            cacheObject.username
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService
                        .getAccountProperties(
                                "Token ${authToken.token!!}"
                        )
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }
        }.asLiveData()
    }

    @InternalCoroutinesApi
    fun saveAccountProperties(authToken: AuthToken, accountProperties: AccountProperties): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null) // The update does not return a CacheObject

                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        ))
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                        "Token ${authToken.token!!}",
                        accountProperties.email,
                        accountProperties.username
                )
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                        accountProperties.pk,
                        accountProperties.email,
                        accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    @InternalCoroutinesApi
    fun updatePassword(authToken: AuthToken, currentPassword: String, newPassword: String, confirmNewPassword: String): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                            DataState.data(null,
                                    Response(response.body.response, ResponseType.Toast())
                            ))
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                        "Token ${authToken.token!!}",
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                )
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }
}