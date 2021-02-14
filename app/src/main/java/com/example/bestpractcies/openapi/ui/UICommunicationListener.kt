package com.example.bestpractcies.openapi.ui

import com.example.bestpractcies.openapi.util.Response
import com.example.bestpractcies.openapi.util.StateMessageCallback

interface UICommunicationListener {

    fun onResponseReceived(
            response: Response,
            stateMessageCallback: StateMessageCallback
    )

    fun displayProgressBar(isLoading: Boolean)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}