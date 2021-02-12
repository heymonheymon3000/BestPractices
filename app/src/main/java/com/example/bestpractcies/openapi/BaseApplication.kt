package com.example.bestpractcies.openapi

import android.app.Application
import com.example.bestpractcies.BuildConfig
import com.example.bestpractcies.openapi.di.AppComponent
import com.example.bestpractcies.openapi.di.DaggerAppComponent
import com.example.bestpractcies.openapi.di.auth.AuthComponent
import com.example.bestpractcies.openapi.di.main.MainComponent
import timber.log.Timber

class BaseApplication: Application() {

    lateinit var appComponent: AppComponent

    private var authComponent: AuthComponent? = null

    private var mainComponent: MainComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()


        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun releaseMainComponent(){
        mainComponent = null
    }

    fun mainComponent(): MainComponent {
        if(mainComponent == null){
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

    fun releaseAuthComponent(){
        authComponent = null
    }

    fun authComponent(): AuthComponent {
        if(authComponent == null){
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    private fun initAppComponent(){
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

}