package com.example.bestpractcies.openapi.di

import com.example.bestpractcies.openapi.di.main.MainModule
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.di.main.MainViewModelModule
import com.example.bestpractcies.openapi.di.auth.AuthFragmentBuildersModule
import com.example.bestpractcies.openapi.di.auth.AuthModule
import com.example.bestpractcies.openapi.di.auth.AuthScope
import com.example.bestpractcies.openapi.di.auth.AuthViewModelModule
import com.example.bestpractcies.openapi.di.main.MainFragmentBuildersModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.example.bestpractcies.openapi.ui.auth.AuthActivity
import com.example.bestpractcies.openapi.ui.main.MainActivity

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
            modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}