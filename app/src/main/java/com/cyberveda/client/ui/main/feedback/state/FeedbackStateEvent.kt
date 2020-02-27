package com.cyberveda.client.ui.main.feedback.state

import okhttp3.MultipartBody

sealed class FeedbackStateEvent {

    class FeedbackSearchEvent : FeedbackStateEvent()

    class RestoreFeedbackListFromCache: FeedbackStateEvent()

    class CheckAuthorOfFeedbackPost: FeedbackStateEvent()

    class DeleteFeedbackPostEvent: FeedbackStateEvent()

    data class UpdateFeedbackPostEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part?
    ): FeedbackStateEvent()

    class None: FeedbackStateEvent()
}