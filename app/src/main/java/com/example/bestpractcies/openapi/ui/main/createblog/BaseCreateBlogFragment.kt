package com.example.bestpractcies.openapi.ui.main.createblog

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.bestpractcies.openapi.ui.DataStateChangeListener
import com.example.bestpractcies.openapi.ui.UICommunicationListener
import timber.log.Timber

abstract class BaseCreateBlogFragment
constructor(
        @LayoutRes
        private val layoutRes: Int
): Fragment() {

    lateinit var stateChangeListener: DataStateChangeListener
    lateinit var uiCommunicationListener: UICommunicationListener

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

        try{
            uiCommunicationListener = context as UICommunicationListener
        }catch(e: ClassCastException){
            Timber.e("$context must implement UICommunicationListener" )
        }
    }
}