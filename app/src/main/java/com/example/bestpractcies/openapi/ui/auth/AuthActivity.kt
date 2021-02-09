package com.example.bestpractcies.openapi.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.ui.BaseActivity
import com.example.bestpractcies.openapi.ui.auth.state.AuthStateEvent
import com.example.bestpractcies.openapi.ui.auth.state.AuthStateEvent.*
import com.example.bestpractcies.openapi.ui.main.MainActivity
import com.example.bestpractcies.openapi.viewModels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_auth.*
import timber.log.Timber
import javax.inject.Inject

class AuthActivity :
    BaseActivity(),
    NavController.OnDestinationChangedListener {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

    override fun displayProgressBar(bool: Boolean) {
        if(bool) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)

        subscribeObservers()

    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    override fun expandAppBar() {
        // ignore
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun subscribeObservers(){
        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let { authViewState ->
                        authViewState.authToken?.let {
                            Timber.d("AuthActivity, DataState: $it")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer{ authViewState ->
            Timber.d("AuthActivity, subscribeObservers: AuthViewState: $authViewState")

            authViewState.authToken?.let{
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer{ dataState ->
            Timber.d("AuthActivity, subscribeObservers: AuthDataState: $dataState")

            dataState.let{ authToken ->
                if(authToken != null && authToken.account_pk != -1 && authToken.token != null){
                    navMainActivity()
                }
            }
        })
    }

    private fun checkPreviousAuthUser(){
        viewModel.setStateEvent(CheckPreviousAuthEvent())
    }

    private fun onFinishCheckPreviousAuthUser(){
        fragment_container.visibility = View.VISIBLE
    }

    private fun navMainActivity(){
        Timber.d("navMainActivity: called.")

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}