package com.example.bestpractcies.openapi.fragments.auth

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.ui.auth.ForgotPasswordFragment
import com.example.bestpractcies.openapi.ui.auth.LauncherFragment
import com.example.bestpractcies.openapi.ui.auth.LoginFragment
import com.example.bestpractcies.openapi.ui.auth.RegisterFragment
import javax.inject.Inject

@AuthScope
class AuthFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            LauncherFragment::class.java.name -> {
                LauncherFragment(viewModelFactory)
            }

            LoginFragment::class.java.name -> {
                LoginFragment(viewModelFactory)
            }

            RegisterFragment::class.java.name -> {
                RegisterFragment(viewModelFactory)
            }

            ForgotPasswordFragment::class.java.name -> {
                ForgotPasswordFragment(viewModelFactory)
            }

            else -> {
                LauncherFragment(viewModelFactory)
            }
        }


}