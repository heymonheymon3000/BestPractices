package com.example.bestpractcies.openapi.ui.main.account.state

import android.os.Parcelable
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import kotlinx.android.parcel.Parcelize

const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState"


@Parcelize
class AccountViewState(
        var accountProperties: AccountProperties? = null
) : Parcelable
