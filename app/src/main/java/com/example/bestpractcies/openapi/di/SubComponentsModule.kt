package com.example.bestpractcies.openapi.di

import com.example.bestpractcies.openapi.di.auth.AuthComponent
import com.example.bestpractcies.openapi.di.main.MainComponent
import dagger.Module

@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule