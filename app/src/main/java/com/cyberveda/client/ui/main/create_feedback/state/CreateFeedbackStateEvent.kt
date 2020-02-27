package com.cyberveda.client.ui.main.create_feedback.state

import okhttp3.MultipartBody


sealed class CreateFeedbackStateEvent {

    data class CreateNewFeedbackEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part
    ): CreateFeedbackStateEvent()

    class None: CreateFeedbackStateEvent()
}