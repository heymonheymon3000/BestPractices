package com.example.bestpractcies.openapi.di

import android.app.Application
import com.example.bestpractcies.openapi.di.auth.AuthComponent
import com.example.bestpractcies.openapi.di.main.MainComponent
import com.example.bestpractcies.openapi.session.SessionManager
import com.example.bestpractcies.openapi.ui.BaseActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SubComponentsModule::class
    ]
)
interface AppComponent  {

    val sessionManager: SessionManager

    @Component.Builder
    interface Builder{

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(baseActivity: BaseActivity)

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory

}











