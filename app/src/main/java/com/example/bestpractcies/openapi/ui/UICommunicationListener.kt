package com.example.bestpractcies.openapi.ui

import com.example.bestpractcies.openapi.ui.UIMessage

interface UICommunicationListener {

    fun onUIMessageReceived(uiMessage: UIMessage)
}