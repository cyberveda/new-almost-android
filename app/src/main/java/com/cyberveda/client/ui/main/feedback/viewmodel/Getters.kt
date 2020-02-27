package com.cyberveda.client.ui.main.feedback.viewmodel

import android.net.Uri
import com.cyberveda.client.models.FeedbackPost

fun FeedbackViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.feedbackFields.filter
    }
}

fun FeedbackViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.feedbackFields.order
    }
}

fun FeedbackViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let {
        return it.feedbackFields.searchQuery
    }
}

fun FeedbackViewModel.getPage(): Int{
    getCurrentViewStateOrNew().let {
        return it.feedbackFields.page
    }
}

fun FeedbackViewModel.getSlug(): String{
    getCurrentViewStateOrNew().let {
        it.viewFeedbackFields.feedbackPost?.let {
            return it.slug
        }
    }
    return ""
}

fun FeedbackViewModel.isAuthorOfFeedbackPost(): Boolean{
    getCurrentViewStateOrNew().let {
        return it.viewFeedbackFields.isAuthorOfFeedbackPost
    }
}


fun FeedbackViewModel.getFeedbackPost(): FeedbackPost {
    getCurrentViewStateOrNew().let {
        return it.viewFeedbackFields.feedbackPost?.let {
            return it
        }?: getDummyFeedbackPost()
    }
}

fun FeedbackViewModel.getDummyFeedbackPost(): FeedbackPost{
    return FeedbackPost(-1, "" , "", "", "", 1, "")
}

fun FeedbackViewModel.getUpdatedFeedbackUri(): Uri? {
    getCurrentViewStateOrNew().let {
        it.updatedFeedbackFields.updatedImageUri?.let {
            return it
        }
    }
    return null
}








