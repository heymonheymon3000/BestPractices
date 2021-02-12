import android.annotation.SuppressLint
import android.util.Log
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogStateEvent.*
import com.example.bestpractcies.openapi.ui.main.blog.state.BlogViewState
import com.example.bestpractcies.openapi.ui.main.blog.viewmodel.*
import timber.log.Timber


fun BlogViewModel.resetPage(){
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.refreshFromCache(){
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreBlogListFromCache())
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
    Timber.e("BlogViewModel: loadFirstPage: ${getSearchQuery()}")
}

private fun BlogViewModel.incrementPageNumber(){
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page // get current page
    update.blogFields.page = page + 1
    setViewState(update)
}

fun BlogViewModel.nextPage(){
    if(!getIsQueryInProgress() && !getIsQueryExhausted()){
        Timber.d("BlogViewModel: Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}

fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState){
    Timber.d("BlogViewModel, DataState: $viewState")
    Timber.d("BlogViewModel, DataState: isQueryInProgress?: ${viewState.blogFields.isQueryInProgress}")
    Timber.d("BlogViewModel, DataState: isQueryExhausted?: ${viewState.blogFields.isQueryExhausted}")
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setBlogListData(viewState.blogFields.blogList)
}


