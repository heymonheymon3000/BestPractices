package com.example.bestpractcies.openapi.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.di.main.MainScope
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.ui.AreYouSureCallback
import com.example.bestpractcies.openapi.ui.UIMessage
import com.example.bestpractcies.openapi.ui.UIMessageType
import com.example.bestpractcies.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent.*
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.*
import com.example.bestpractcies.openapi.util.DateUtils
import com.example.bestpractcies.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.android.synthetic.main.fragment_view_blog.*
import timber.log.Timber
import javax.inject.Inject

@MainScope
class ViewBlogFragment
@Inject
constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val requestManager: RequestManager
): BaseBlogFragment(R.layout.fragment_view_blog)
{

    val viewModel: BlogViewModel by viewModels{
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()
        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    /**
     * !IMPORTANT!
     * Must save ViewState b/c in event of process death the LiveData in ViewModel will be lost
     */
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.blogFields?.blogList = ArrayList()

        outState.putParcelable(
                BLOG_VIEW_STATE_BUNDLE_KEY,
                viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppBar()

        delete_button.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    fun confirmDeleteRequest(){
        val callback: AreYouSureCallback = object: AreYouSureCallback {

            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {
                // ignore
            }

        }
        uiCommunicationListener.onUIMessageReceived(
                UIMessage(
                        getString(R.string.are_you_sure_delete),
                        UIMessageType.AreYouSureDialog(callback)
                )
        )
    }

    fun deleteBlogPost(){
        viewModel.setStateEvent(
                DeleteBlogPostEvent()
        )
    }

    fun checkIsAuthorOfBlogPost(){
        viewModel.setIsAuthorOfBlogPost(false) // reset
        viewModel.setStateEvent(CheckAuthorOfBlogPost())
    }

    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            stateChangeListener.onDataStateChange(dataState)

            if(dataState != null){
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setIsAuthorOfBlogPost(
                                viewState.viewBlogFields.isAuthorOfBlogPost
                        )
                    }
                    data.response?.peekContent()?.let{ response ->
                        if(response.message.equals(SUCCESS_BLOG_DELETED)){
                            viewModel.removeDeletedBlogPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewBlogFields.blogPost?.let{ blogPost ->
                setBlogProperties(blogPost)
            }

            if(viewState.viewBlogFields.isAuthorOfBlogPost){
                adaptViewToAuthorMode()
            }
        })
    }

    fun adaptViewToAuthorMode(){
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    fun setBlogProperties(blogPost: BlogPost){
        requestManager
                .load(blogPost.image)
                .into(blog_image)
        blog_title.text = blogPost.title
        blog_author.text = blogPost.username
        blog_update_date.text = DateUtils.convertLongToStringDate(blogPost.date_updated)
        blog_body.text = blogPost.body
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(viewModel.isAuthorOfBlogPost()){
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(viewModel.isAuthorOfBlogPost()){
            when(item.itemId){
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateBlogFragment(){
        try{
            // prep for next fragment
            viewModel.setUpdatedBlogFields(
                    viewModel.getBlogPost().title,
                    viewModel.getBlogPost().body,
                    viewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        }catch (e: Exception){
            // send error report or something. These fields should never be null. Not possible
            Timber.e("Exception: ${e.message}")
        }
    }
}













