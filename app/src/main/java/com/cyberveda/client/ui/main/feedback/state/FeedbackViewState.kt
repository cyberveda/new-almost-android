package com.cyberveda.client.ui.main.feedback.state

import android.net.Uri
import android.os.Parcelable
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.FEEDBACK_ORDER_ASC
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import kotlinx.android.parcel.Parcelize

const val FEEDBACK_VIEW_STATE_BUNDLE_KEY = "com.cyberveda.client.ui.main.feedback.state.FeedbackViewState"

@Parcelize
data class FeedbackViewState (

    // FeedbackFragment vars
    var feedbackFields: FeedbackFields = FeedbackFields(),

    // ViewFeedbackFragment vars
    var viewFeedbackFields: ViewFeedbackFields = ViewFeedbackFields(),

    // UpdateFeedbackFragment vars
    var updatedFeedbackFields: UpdatedFeedbackFields = UpdatedFeedbackFields()

): Parcelable {

    @Parcelize
    data class FeedbackFields(
        var feedbackList: List<FeedbackPost> = ArrayList<FeedbackPost>(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = FEEDBACK_ORDER_ASC,
        var layoutManagerState: Parcelable? = null
    ) : Parcelable

    @Parcelize
    data class ViewFeedbackFields(
        var feedbackPost: FeedbackPost? = null,
        var isAuthorOfFeedbackPost: Boolean = false
    ) : Parcelable

    @Parcelize
    data class UpdatedFeedbackFields(
        var updatedFeedbackTitle: String? = null,
        var updatedFeedbackBody: String? = null,
        var updatedImageUri: Uri? = null
    ) : Parcelable
}








