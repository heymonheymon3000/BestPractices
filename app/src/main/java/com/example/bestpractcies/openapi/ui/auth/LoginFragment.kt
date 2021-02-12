package com.example.bestpractcies.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import com.example.bestpractcies.openapi.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.input_email
import kotlinx.android.synthetic.main.fragment_register.input_password
import timber.log.Timber
import javax.inject.Inject

@AuthScope
class LoginFragment @Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): Fragment(R.layout.fragment_register) {

    val viewModel: AuthViewModel by viewModels{
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()

        login_button.setOnClickListener {
            login()
        }
    }

    private fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{ authViewState ->
            authViewState.loginFields?.let{ loginFields ->
                loginFields.login_email?.let{input_email.setText(it)}
                loginFields.login_password?.let{input_password.setText(it)}
            }
        })
    }

    fun login(){
        viewModel.setStateEvent(
            LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }
}