package com.example.bestpractcies.openapi.di.main

import androidx.lifecycle.ViewModel
import com.example.bestpractcies.openapi.di.ViewModelKey
import com.example.bestpractcies.openapi.ui.auth.AuthViewModel
import com.example.bestpractcies.openapi.ui.main.account.AccountViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel
}