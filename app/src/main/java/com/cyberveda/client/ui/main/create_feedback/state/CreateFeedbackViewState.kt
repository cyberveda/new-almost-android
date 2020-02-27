package com.cyberveda.client.ui.main.create_feedback.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val CREATE_FEEDBACK_VIEW_STATE_BUNDLE_KEY = "com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackViewState"

@Parcelize
data class CreateFeedbackViewState(

    // CreateFeedbackFragment vars
    var feedbackFields: NewFeedbackFields = NewFeedbackFields()

) : Parcelable {

    @Parcelize
    data class NewFeedbackFields(
        var newFeedbackTitle: String? = null,
        var newFeedbackBody: String? = null,
        var newImageUri: Uri? = null
    ) : Parcelable
}