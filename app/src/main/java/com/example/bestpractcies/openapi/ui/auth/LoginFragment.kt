package com.example.bestpractcies.openapi.ui.auth

import android.os.Bundle
import android.view.View

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import com.example.bestpractcies.openapi.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.input_email
import kotlinx.android.synthetic.main.fragment_register.input_password
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AuthScope
class LoginFragment
@Inject
constructor(
        viewModelFactory: ViewModelProvider.Factory
): BaseAuthFragment(R.layout.fragment_login, viewModelFactory) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()

        login_button.setOnClickListener {
            login()
        }

    }

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{
            it.loginFields?.let{
                it.login_email?.let{input_email.setText(it)}
                it.login_password?.let{input_password.setText(it)}
            }
        })
    }

    fun login(){
        saveLoginFields()
        viewModel.setStateEvent(
                LoginAttemptEvent(
                        input_email.text.toString(),
                        input_password.text.toString()
                )
        )
    }

    private fun saveLoginFields(){
        viewModel.setLoginFields(
                LoginFields(
                        input_email.text.toString(),
                        input_password.text.toString()
                )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveLoginFields()
    }

}








