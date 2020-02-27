import android.util.Log
import com.cyberveda.client.ui.main.feedback.state.FeedbackStateEvent.*
import com.cyberveda.client.ui.main.feedback.state.FeedbackViewState
import com.cyberveda.client.ui.main.feedback.viewmodel.FeedbackViewModel
import com.cyberveda.client.ui.main.feedback.viewmodel.setFeedbackListData
import com.cyberveda.client.ui.main.feedback.viewmodel.setQueryExhausted
import com.cyberveda.client.ui.main.feedback.viewmodel.setQueryInProgress


fun FeedbackViewModel.resetPage(){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.page = 1
    setViewState(update)
}

fun FeedbackViewModel.refreshFromCache(){
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreFeedbackListFromCache())
}

fun FeedbackViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(FeedbackSearchEvent())
    Log.e(TAG, "FeedbackViewModel: loadFirstPage: ${viewState.value!!.feedbackFields.searchQuery}")
}

private fun FeedbackViewModel.incrementPageNumber(){
    val update = getCurrentViewStateOrNew()
    val page = update.copy().feedbackFields.page // get current page
    update.feedbackFields.page = page + 1
    setViewState(update)
}

fun FeedbackViewModel.nextPage(){
    if(!viewState.value!!.feedbackFields.isQueryInProgress
        && !viewState.value!!.feedbackFields.isQueryExhausted){
        Log.d(TAG, "FeedbackViewModel: Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(FeedbackSearchEvent())
    }
}

fun FeedbackViewModel.handleIncomingFeedbackListData(viewState: FeedbackViewState){
    Log.d(TAG, "FeedbackViewModel, DataState: ${viewState}")
    Log.d(TAG, "FeedbackViewModel, DataState: isQueryInProgress?: " +
            "${viewState.feedbackFields.isQueryInProgress}")
    Log.d(TAG, "FeedbackViewModel, DataState: isQueryExhausted?: " +
            "${viewState.feedbackFields.isQueryExhausted}")
    setQueryInProgress(viewState.feedbackFields.isQueryInProgress)
    setQueryExhausted(viewState.feedbackFields.isQueryExhausted)
    setFeedbackListData(viewState.feedbackFields.feedbackList)
}


