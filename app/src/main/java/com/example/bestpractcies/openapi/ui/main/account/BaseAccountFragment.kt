package com.example.bestpractcies.openapi.ui.main.account

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.ui.DataStateChangeListener
import com.example.bestpractcies.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.example.bestpractcies.openapi.ui.main.account.state.AccountViewState
import timber.log.Timber

abstract class BaseAccountFragment constructor(
        @LayoutRes
        private val layoutRes: Int
): Fragment(layoutRes){

    lateinit var stateChangeListener: DataStateChangeListener

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        cancelActiveJobs()
//
//        // Restore state after process death
//        savedInstanceState?.let { inState ->
//            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { viewState ->
//                viewModel.setViewState(viewState)
//            }
//        }
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putParcelable(
//                ACCOUNT_VIEW_STATE_BUNDLE_KEY,
//                viewModel.viewState.value
//        )
//        super.onSaveInstanceState(outState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.accountFragment, activity as AppCompatActivity)
    }

    abstract fun cancelActiveJobs()

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity){
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
                activity,
                findNavController(),
                appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Timber.e("$context must implement DataStateChangeListener" )
        }
    }


}