package com.example.bestpractcies.openapi.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.example.bestpractcies.openapi.ui.main.account.state.AccountStateEvent.*
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import kotlinx.android.synthetic.main.fragment_update_account.*
import timber.log.Timber
import javax.inject.Inject

@MainScope
class UpdateAccountFragment
@Inject
constructor(
        private val viewModelFactory: ViewModelProvider.Factory
): BaseAccountFragment(R.layout.fragment_update_account) {

    val viewModel: AccountViewModel by viewModels{
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()
        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            if(dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                Timber.d("UpdateAccountFragment, DataState: $dataState")
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            if(viewState != null){
                viewState.accountProperties?.let{
                    Timber.d("UpdateAccountFragment, ViewState: $it")
                    setAccountDataFields(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties){
        if(input_email.text.isNullOrBlank()){
            input_email.setText(accountProperties.email)
        }
        if(input_username.text.isNullOrBlank()){
            input_username.setText(accountProperties.username)
        }
    }

    private fun saveChanges(){
        viewModel.setStateEvent(
            UpdateAccountPropertiesEvent(
                    input_email.text.toString(),
                    input_username.text.toString()
            )
        )
        stateChangeListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}