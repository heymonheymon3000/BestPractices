package com.example.bestpractcies.openapi.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.ui.*
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.getUpdatedBlogUri
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.onBlogPostUpdateSuccess
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.setUpdatedBlogFields
import com.example.bestpractcies.openapi.ui.main.createblog.state.CreateBlogStateEvent
import com.example.bestpractcies.openapi.util.Constants
import com.example.bestpractcies.openapi.util.ErrorHandling
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_blog.*
import kotlinx.android.synthetic.main.fragment_update_blog.*
import kotlinx.android.synthetic.main.fragment_update_blog.blog_body
import kotlinx.android.synthetic.main.fragment_update_blog.blog_image
import kotlinx.android.synthetic.main.fragment_update_blog.blog_title
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

class UpdateBlogFragment : BaseBlogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()

        image_container.setOnClickListener {
            if(stateChangeListener.isStoragePermissionGranted()){
                pickFromGallery()
            }
        }
    }

    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if(dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let{ data ->
                    data.data?.getContentIfNotHandled()?.let{ viewState ->

                        // if this is not null, the blogpost was updated
                        viewState.viewBlogFields.blogPost?.let{ blogPost ->
                            viewModel.onBlogPostUpdateSuccess(blogPost).let {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedBlogFields.let{ updatedBlogFields ->
                setBlogProperties(
                        updatedBlogFields.updatedBlogTitle,
                        updatedBlogFields.updatedBlogBody,
                        updatedBlogFields.updatedImageUri
                )
            }
        })
    }

    fun setBlogProperties(title: String?, body: String?, image: Uri?){
        dependencyProvider.getGlideRequestManager()
                .load(image)
                .into(blog_image)
        blog_title.setText(title)
        blog_body.setText(body)
    }

    private fun saveChanges(){
        var multipartBody: MultipartBody.Part? = null
        viewModel.getUpdatedBlogUri()?.let{ imageUri ->
            imageUri.path?.let{filePath ->
                val imageFile = File(filePath)
                Timber.d("UpdateBlogFragment, imageFile: file: $imageFile")
                if(imageFile.exists()){
                    val requestBody =
                            RequestBody.create(
                                    MediaType.parse("image/*"),
                                    imageFile
                            )
                    // name = field name in serializer
                    // filename = name of the image file
                    // requestBody = file with file type information
                    multipartBody = MultipartBody.Part.createFormData(
                            "image",
                            imageFile.name,
                            requestBody
                    )
                }
            }
        }
        viewModel.setStateEvent(
                BlogStateEvent.UpdateBlogPostEvent(
                        blog_title.text.toString(),
                        blog_body.text.toString(),
                        multipartBody
                )
        )
        stateChangeListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, Constants.GALLERY_REQUEST_CODE)
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

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
                uri = null,
                title = blog_title.text.toString(),
                body = blog_body.text.toString()
        )
    }

    private fun launchImageCrop(uri: Uri){
        context?.let{
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(it, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Timber.d("CROP: RESULT OK")
            when (requestCode) {

                Constants.GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let{
                            launchImageCrop(uri)
                        }
                    }?: showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Timber.e("CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Timber.d("CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: $resultUri")
                    viewModel.setUpdatedBlogFields(
                            title = null,
                            body = null,
                            uri = resultUri
                    )
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Timber.d("CROP: ERROR")
                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
    }

    private fun publishNewBlog(){
        var multipartBody: MultipartBody.Part? = null
        viewModel.viewState.value?.updatedBlogFields?.updatedImageUri?.let{ imageUri ->
            imageUri.path?.let{filePath ->
                val imageFile = File(filePath)
                Timber.d("UpdateBlogFragment, imageFile: file: $imageFile")
                val requestBody =
                        RequestBody.create(
                                MediaType.parse("image/*"),
                                imageFile
                        )
                // name = field name in serializer
                // filename = name of the image file
                // requestBody = file with file type information
                multipartBody = MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        requestBody
                )
            }
        }

        multipartBody?.let {

            viewModel.setStateEvent(
                    BlogStateEvent.UpdateBlogPostEvent(
                            blog_title.text.toString(),
                            blog_body.text.toString(),
                            it
                    )
            )
            stateChangeListener.hideSoftKeyboard()
        }?: showErrorDialog(ErrorHandling.ERROR_MUST_SELECT_IMAGE)

    }

    private fun showErrorDialog(errorMessage: String){
        stateChangeListener.onDataStateChange(
                DataState(
                        Event(StateError(Response(errorMessage, ResponseType.Dialog()))),
                        Loading(isLoading = false),
                        Data(Event.dataEvent(null), null)
                )
        )
    }

}