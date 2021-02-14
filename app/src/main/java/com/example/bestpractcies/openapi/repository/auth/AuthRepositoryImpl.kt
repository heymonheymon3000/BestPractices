package com.example.bestpractcies.openapi.repository.auth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import com.example.bestpractcies.openapi.api.auth.OpenApiAuthService
import com.example.bestpractcies.openapi.api.auth.network.responses.LoginResponse
import com.example.bestpractcies.openapi.api.auth.network.responses.RegistrationResponse
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.auth.AuthTokenDao
import com.example.bestpractcies.openapi.repository.buildError
import com.example.bestpractcies.openapi.repository.safeApiCall
import com.example.bestpractcies.openapi.repository.safeCacheCall
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.auth.state.AuthViewState
import com.example.bestpractcies.openapi.ui.auth.state.LoginFields
import com.example.bestpractcies.openapi.ui.auth.state.RegistrationFields
import com.example.bestpractcies.openapi.util.*
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.example.bestpractcies.openapi.util.ErrorHandling.Companion.INVALID_CREDENTIALS
import com.example.bestpractcies.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
@AuthScope
class AuthRepositoryImpl
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
): AuthRepository
{

    private val TAG: String = "AppDebug"

    @SuppressLint("LogNotTimber")
    override fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>> = flow {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if(loginFieldErrors == LoginFields.LoginError.none()){
            val apiResult = safeApiCall(IO){
                openApiAuthService.login(email, password)
            }
            emit(
                object: ApiResponseHandler<AuthViewState, LoginResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: LoginResponse): DataState<AuthViewState> {

                        Log.d(TAG, "handleSuccess ")

                        // Incorrect login credentials counts as a 200 response from server, so need to handle that
                        if(resultObj.response == GENERIC_AUTH_ERROR){
                            return DataState.error(
                                response = Response(
                                    INVALID_CREDENTIALS,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        accountPropertiesDao.insertOrIgnore(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                ""
                            )
                        )

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result = authTokenDao.insert(authToken)
                        if(result < 0){
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)

                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }

                }.getResult()
            )
        }
        else{
            Log.d(TAG, "emitting error: $loginFieldErrors")
            emit(
                buildError<AuthViewState>(
                    loginFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = flow {
        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(registrationFieldErrors == RegistrationFields.RegistrationError.none()){

            val apiResult = safeApiCall(IO){
                openApiAuthService.register(
                    email,
                    username,
                    password,
                    confirmPassword
                )
            }
            emit(
                object: ApiResponseHandler<AuthViewState, RegistrationResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(resultObj: RegistrationResponse): DataState<AuthViewState> {
                        if(resultObj.response == GENERIC_AUTH_ERROR){
                            return DataState.error(
                                response = Response(
                                    resultObj.errorMessage,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        val result1 = accountPropertiesDao.insertAndReplace(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                resultObj.username
                            )
                        )
                        // will return -1 if failure
                        if(result1 < 0){
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_ACCOUNT_PROPERTIES,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result2 = authTokenDao.insert(authToken)
                        if(result2 < 0){
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)
                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }
                }.getResult()
            )

        }
        else{
            emit(
                buildError<AuthViewState>(
                    registrationFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }

    }


    override fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>> = flow {
        Timber.d("checkPreviousAuthUser: ")
        val previousAuthUserEmail: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()) {
            Timber.d("checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        }
        else{
            val apiResult = safeCacheCall(IO){
                accountPropertiesDao.searchByEmail(previousAuthUserEmail)
            }
            emit(
                object: CacheResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {

                        if (resultObj.pk > -1) {
                            authTokenDao.searchByPk(resultObj.pk).let { authToken ->
                                if (authToken != null) {
                                    if (authToken.token != null) {
                                        return DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            ),
                                            response = null,
                                            stateEvent = stateEvent
                                        )
                                    }
                                }
                            }
                        }
                        Timber.d("createCacheRequestAndReturn: AuthToken not found...")
                        return DataState.error(
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                UIComponentType.None(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }.getResult()
            )
        }
    }

    override fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    override fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState> {

        return DataState.error(
            response = Response(
                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                UIComponentType.None(),
                MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }



}