package com.cyberveda.client.ui.main.feedback.viewmodel

import android.net.Uri
import android.os.Parcelable
import com.cyberveda.client.models.FeedbackPost

fun FeedbackViewModel.setQuery(query: String){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.searchQuery = query
    setViewState(update)
}

fun FeedbackViewModel.setFeedbackListData(feedbackList: List<FeedbackPost>){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.feedbackList = feedbackList
    setViewState(update)
}

fun FeedbackViewModel.setFeedbackPost(feedbackPost: FeedbackPost){
    val update = getCurrentViewStateOrNew()
    update.viewFeedbackFields.feedbackPost = feedbackPost
    setViewState(update)
}

fun FeedbackViewModel.setIsAuthorOfFeedbackPost(isAuthorOfFeedbackPost: Boolean){
    val update = getCurrentViewStateOrNew()
    update.viewFeedbackFields.isAuthorOfFeedbackPost = isAuthorOfFeedbackPost
    setViewState(update)
}

fun FeedbackViewModel.setQueryExhausted(isExhausted: Boolean){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun FeedbackViewModel.setQueryInProgress(isInProgress: Boolean){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.isQueryInProgress = isInProgress
    setViewState(update)
}


// Filter can be "date_updated" or "username"
fun FeedbackViewModel.setFeedbackFilter(filter: String?){
    filter?.let{
        val update = getCurrentViewStateOrNew()
        update.feedbackFields.filter = filter
        setViewState(update)
    }
}

// Order can be "-" or ""
// Note: "-" = DESC, "" = ASC
fun FeedbackViewModel.setFeedbackOrder(order: String){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.order = order
    setViewState(update)
}

fun FeedbackViewModel.setLayoutManagerState(layoutManagerState: Parcelable){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.layoutManagerState = layoutManagerState
    setViewState(update)
}

fun FeedbackViewModel.clearLayoutManagerState(){
    val update = getCurrentViewStateOrNew()
    update.feedbackFields.layoutManagerState = null
    setViewState(update)
}

fun FeedbackViewModel.removeDeletedFeedbackPost(){
    val update = getCurrentViewStateOrNew()
    val list = update.feedbackFields.feedbackList.toMutableList()
    for(i in 0..(list.size - 1)){
        if(list[i] == getFeedbackPost()){
            list.remove(getFeedbackPost())
            break
        }
    }
    setFeedbackListData(list)
}

fun FeedbackViewModel.updateListItem(newFeedbackPost: FeedbackPost){
    val update = getCurrentViewStateOrNew()
    val list = update.feedbackFields.feedbackList.toMutableList()
    for(i in 0..(list.size - 1)){
        if(list[i].pk == newFeedbackPost.pk){
            list[i] = newFeedbackPost
            break
        }
    }
    update.feedbackFields.feedbackList = list
    setViewState(update)
}


fun FeedbackViewModel.onFeedbackPostUpdateSuccess(feedbackPost: FeedbackPost){
    setUpdatedFeedbackFields(
        uri = null,
        title = feedbackPost.title,
        body = feedbackPost.body
    ) // update UpdateFeedbackFragment (not really necessary since navigating back)
    setFeedbackPost(feedbackPost) // update ViewFeedbackFragment
    updateListItem(feedbackPost) // update FeedbackFragment
}


/**
 * Only change the values passed into constructor
 */
fun FeedbackViewModel.setUpdatedFeedbackFields(
    title: String?,
    body: String?,
    uri: Uri?
){
    val update = getCurrentViewStateOrNew()
    val updatedFeedbackFields = update.updatedFeedbackFields
    title?.let{ updatedFeedbackFields.updatedFeedbackTitle = it }
    body?.let{ updatedFeedbackFields.updatedFeedbackBody = it }
    uri?.let{ updatedFeedbackFields.updatedImageUri = it }
    update.updatedFeedbackFields = updatedFeedbackFields
    setViewState(update)
}






