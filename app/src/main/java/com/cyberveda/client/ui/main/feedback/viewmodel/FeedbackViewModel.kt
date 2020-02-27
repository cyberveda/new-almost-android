package com.cyberveda.client.ui.main.feedback.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.cyberveda.client.persistence.FeedbackQueryUtils
import com.cyberveda.client.repository.main.FeedbackRepository
import com.cyberveda.client.session.SessionManager
import com.cyberveda.client.ui.BaseViewModel
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.Loading
import com.cyberveda.client.ui.main.feedback.state.FeedbackStateEvent
import com.cyberveda.client.ui.main.feedback.state.FeedbackStateEvent.*
import com.cyberveda.client.ui.main.feedback.state.FeedbackViewState
import com.cyberveda.client.util.AbsentLiveData
import com.cyberveda.client.util.PreferenceKeys.Companion.FEEDBACK_FILTER
import com.cyberveda.client.util.PreferenceKeys.Companion.FEEDBACK_ORDER
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class FeedbackViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val feedbackRepository: FeedbackRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<FeedbackStateEvent, FeedbackViewState>() {

    init {
        setFeedbackFilter(
            sharedPreferences.getString(
                FEEDBACK_FILTER,
                FeedbackQueryUtils.FEEDBACK_FILTER_DATE_UPDATED
            )
        )
        setFeedbackOrder(
            sharedPreferences.getString(
                FEEDBACK_ORDER,
                FeedbackQueryUtils.FEEDBACK_ORDER_ASC
            )
        )
    }

    override fun handleStateEvent(stateEvent: FeedbackStateEvent): LiveData<DataState<FeedbackViewState>> {
        when (stateEvent) {

            is FeedbackSearchEvent -> {
                clearLayoutManagerState()
                return feedbackRepository.searchFeedbackPosts(
//                        authToken = authToken,
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage()
                )

//                sessionManager.cachedToken.value?.let { authToken ->
//                    Log.d(TAG, "Feedback Search Event: got auth token.")
//                    feedbackRepository.searchFeedbackPosts(
////                        authToken = authToken,
//                        query = getSearchQuery(),
//                        filterAndOrder = getOrder() + getFilter(),
//                        page = getPage()
//                    )
//                }?: AbsentLiveData.create()
            }

            is RestoreFeedbackListFromCache -> {
                return feedbackRepository.restoreFeedbackListFromCache(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage()
                )
            }

            is CheckAuthorOfFeedbackPost -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    feedbackRepository.isAuthorOfFeedbackPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                } ?: AbsentLiveData.create()
            }

            is DeleteFeedbackPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    feedbackRepository.deleteFeedbackPost(
                        authToken = authToken,
                        feedbackPost = getFeedbackPost()
                    )
                } ?: AbsentLiveData.create()
            }

            is UpdateFeedbackPostEvent -> {

                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    feedbackRepository.updateFeedbackPost(
                        authToken = authToken,
                        slug = getSlug(),
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                } ?: AbsentLiveData.create()
            }

            is None -> {
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    override fun initNewViewState(): FeedbackViewState {
        return FeedbackViewState()
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(FEEDBACK_FILTER, filter)
        editor.apply()

        editor.putString(FEEDBACK_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs() {
        feedbackRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
        Log.d(TAG, "CLEARED...")
    }


}











