package com.example.bestpractcies.openapi.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.example.bestpractcies.openapi.ui.main.account.state.AccountStateEvent
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import kotlinx.android.synthetic.main.fragment_account.*
import timber.log.Timber
import javax.inject.Inject

@MainScope
class AccountFragment
@Inject
constructor(
        private val viewModelFactory: ViewModelProvider.Factory
): BaseAccountFragment(R.layout.fragment_account) {

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

        change_password.setOnClickListener{
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }

        logout_button.setOnClickListener {
            viewModel.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            Timber.d("AccountFragment: DataState: $dataState")
            stateChangeListener.onDataStateChange(dataState)
            if(dataState != null){
                dataState.data?.let { data ->
                    data.data?.let{ event ->
                        event.getContentIfNotHandled()?.let{ viewState ->
                            viewState.accountProperties?.let{ accountProperties ->
                                Timber.d("AccountFragment, DataState: $accountProperties")
                                viewModel.setAccountPropertiesData(accountProperties)
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState->
            Timber.d("AccountFragment, ViewState: $viewState")
            if(viewState != null){
                viewState.accountProperties?.let{
                    setAccountDataFields(it)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(AccountStateEvent.GetAccountPropertiesEvent())
    }

    private fun setAccountDataFields(accountProperties: AccountProperties){
        email?.text = accountProperties.email
        username?.text = accountProperties.username
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit -> {
                findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}