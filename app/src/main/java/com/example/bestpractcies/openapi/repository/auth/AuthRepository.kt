package com.example.bestpractcies.openapi.repository.auth

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.example.bestpractcies.openapi.api.auth.OpenApiAuthService
import com.example.bestpractcies.openapi.api.auth.network.responses.LoginResponse
import com.example.bestpractcies.openapi.api.auth.network.responses.RegistrationResponse
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.auth.AuthTokenDao
import com.example.bestpractcies.openapi.repository.JobManager
import com.example.bestpractcies.openapi.repository.NetworkBoundResource
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.DataState
import com.example.bestpractcies.openapi.ui.Response
import com.example.bestpractcies.openapi.ui.ResponseType
import com.example.bestpractcies.openapi.ui.auth.state.AuthViewState
import com.example.bestpractcies.openapi.ui.auth.state.LoginFields
import com.example.bestpractcies.openapi.ui.auth.state.RegistrationFields
import com.example.bestpractcies.openapi.util.AbsentLiveData
import com.example.bestpractcies.openapi.util.ApiSuccessResponse
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.example.bestpractcies.openapi.util.GenericApiResponse
import com.example.bestpractcies.openapi.util.PreferenceKeys
import com.example.bestpractcies.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.Job
import timber.log.Timber

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    private val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
): JobManager("AuthRepository") {

    @InternalCoroutinesApi
    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>>{

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if(loginFieldErrors != LoginFields.LoginError.none()){
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<LoginResponse, Any, AuthViewState>(
                sessionManager.isConnectedToTheInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = true,
                shouldLoadFromCache = false
        ){
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Timber.d("handleApiSuccessResponse: $response")

                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if(response.body.response == GENERIC_AUTH_ERROR){
                    return onErrorReturn(response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // Don't care about result here. Just insert if it doesn't exist b/c of foreign key relationship
                // with AuthToken
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result < 0){
                    return onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog()))
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }

            // not used in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            // not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

        }.asLiveData()
    }

    @InternalCoroutinesApi
    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>>{

        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(registrationFieldErrors != RegistrationFields.RegistrationError.none()){
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
                sessionManager.isConnectedToTheInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = true,
                shouldLoadFromCache = false
        ){
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {

                Timber.d("handleApiSuccessResponse: $response")

                if(response.body.response == GENERIC_AUTH_ERROR){
                    return onErrorReturn(response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        response.body.username
                    )
                )

                // will return -1 if failure
                if(result1 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog()))
                    )
                    return
                }

                // will return -1 if failure
                val result2 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result2 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                    ))
                    return
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }

            override suspend fun createCacheRequestAndReturn() {
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

        }.asLiveData()
    }

    @InternalCoroutinesApi
    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>>{

        val previousAuthUserEmail: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()){
            Timber.d("checkPreviousAuthUser: No previously authenticated user found.")
            return returnNoTokenFound()
        }
        else{
            return object: NetworkBoundResource<Void, Any, AuthViewState>(
                    sessionManager.isConnectedToTheInternet(),
                    false,
                    shouldCancelIfNoInternet = false,
                    false
            ){

                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                        Timber.d("createCacheRequestAndReturn: searching for token... account properties: $accountProperties")

                        accountProperties?.let {
                            if(accountProperties.pk > -1){
                                authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                    if(authToken != null){
                                        if(authToken.token != null){

                                            Timber.e("*********************")
                                            Timber.e("AuthToken: $authToken")
                                            Timber.e("*********************")


                                            onCompleteJob(
                                                DataState.data(
                                                    AuthViewState(authToken = authToken)
                                                )
                                            )
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        Timber.d("createCacheRequestAndReturn: AuthToken not found...")
                        onCompleteJob(
                            DataState.data(
                                null,
                                Response(
                                    RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                    }
                }

                // not used in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                }

                // not used in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> {
                    return AbsentLiveData.create()
                }

                override fun setJob(job: Job) {
                    addJob("checkPreviousAuthUser", job)
                }

                override fun loadFromCache(): LiveData<AuthViewState> {
                    return AbsentLiveData.create()
                }

                override suspend fun updateLocalDb(cacheObject: Any?) {
                }

            }.asLiveData()
        }
    }


    private fun saveAuthenticatedUserToPrefs(email: String){
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>>{
        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.data(null, Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None()))
            }
        }
    }

    private fun returnErrorResponse(errorMessage: String, responseType: ResponseType): LiveData<DataState<AuthViewState>>{
        Timber.d("returnErrorResponse: $errorMessage")

        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }
}