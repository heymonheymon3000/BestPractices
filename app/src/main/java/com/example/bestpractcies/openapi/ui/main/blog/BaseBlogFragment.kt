package com.example.bestpractcies.openapi.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.Injectable
import com.example.bestpractcies.openapi.ui.DataStateChangeListener
import com.example.bestpractcies.openapi.ui.UICommunicationListener
import com.example.bestpractcies.openapi.ui.main.MainDependencyProvider
import com.example.bestpractcies.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.BlogViewModel
import com.example.bestpractcies.openapi.viewModels.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import timber.log.Timber
import javax.inject.Inject

abstract class BaseBlogFragment : Fragment(), Injectable {
     lateinit var stateChangeListener: DataStateChangeListener

    lateinit var dependencyProvider: MainDependencyProvider

    lateinit var viewModel: BlogViewModel

    lateinit var uiCommunicationListener: UICommunicationListener

    fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getVMProviderFactory()).get(BlogViewModel::class.java)
        }?: throw Exception("Invalid Activity")


        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.blogFragment, activity as AppCompatActivity)

    }

    fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if(isViewModelInitialized()){
            val viewState = viewModel.viewState.value

            //clear the list. Don't want to save a large list to bundle.
            viewState?.blogFields?.blogList = ArrayList()

            outState.putParcelable(
                    BLOG_VIEW_STATE_BUNDLE_KEY,
                    viewState
            )
        }
        super.onSaveInstanceState(outState)
    }

    /*
              @fragmentId is id of fragment from graph to be EXCLUDED from action back bar nav
            */
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

        try{
            dependencyProvider = context as MainDependencyProvider
        }catch(e: ClassCastException){
            Timber.e("$context must implement DependencyProvider" )
        }

    }


}