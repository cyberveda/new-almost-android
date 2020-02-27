package com.cyberveda.client.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.cyberveda.client.api.GenericResponse
import com.cyberveda.client.api.main.OpenApiMainService
import com.cyberveda.client.api.main.responses.FeedbackCreateUpdateResponse
import com.cyberveda.client.api.main.responses.FeedbackListSearchResponse
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.persistence.FeedbackPostDao
import com.cyberveda.client.persistence.returnOrderedFeedbackQuery
import com.cyberveda.client.repository.JobManager
import com.cyberveda.client.repository.NetworkBoundResource
import com.cyberveda.client.session.SessionManager
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.Response
import com.cyberveda.client.ui.ResponseType
import com.cyberveda.client.ui.main.feedback.state.FeedbackViewState
import com.cyberveda.client.ui.main.feedback.state.FeedbackViewState.*
import com.cyberveda.client.util.AbsentLiveData
import com.cyberveda.client.util.ApiSuccessResponse
import com.cyberveda.client.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.cyberveda.client.util.DateUtils
import com.cyberveda.client.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.cyberveda.client.util.GenericApiResponse
import com.cyberveda.client.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.cyberveda.client.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.cyberveda.client.util.SuccessHandling.Companion.SUCCESS_FEEDBACK_DELETED
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class FeedbackRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val feedbackPostDao: FeedbackPostDao,
    val sessionManager: SessionManager
) : JobManager("FeedbackRepository") {

    private val TAG = "lgx_FeedbackRepository"
    fun searchFeedbackPosts(
//        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<FeedbackViewState>> {
        return object : NetworkBoundResource<FeedbackListSearchResponse, List<FeedbackPost>, FeedbackViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            // if network is down, view cache only and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.feedbackFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.feedbackFields.feedbackList.size) {
                            viewState.feedbackFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<FeedbackListSearchResponse>
            ) {

                val feedbackPostList: ArrayList<FeedbackPost> = ArrayList()
                for (feedbackPostResponse in response.body.results) {
                    feedbackPostList.add(
                        FeedbackPost(
                            pk = feedbackPostResponse.pk,
                            title = feedbackPostResponse.title,
                            slug = feedbackPostResponse.slug,
                            body = feedbackPostResponse.body,
                            image = feedbackPostResponse.image,
                            date_updated = DateUtils.convertServerStringDateToLong(
                                feedbackPostResponse.date_updated
                            ),
                            username = feedbackPostResponse.username
                        )
                    )
                }
                updateLocalDb(feedbackPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<FeedbackListSearchResponse>> {
                return openApiMainService.searchListFeedbackPosts(

                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<FeedbackViewState> {
                return feedbackPostDao.returnOrderedFeedbackQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap {
                        object : LiveData<FeedbackViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = FeedbackViewState(
                                    FeedbackFields(
                                        feedbackList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<FeedbackPost>?) {
                // loop through list and update the local db
                if (cacheObject != null) {
                    withContext(IO) {
                        for (feedbackPost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting feedback: ${feedbackPost}")
                                    feedbackPostDao.insert(feedbackPost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updating cache data on feedback post with slug: ${feedbackPost.slug}. " +
                                            "${e.message}"
                                )
                                // Could send an error report here or something but I don't think you should throw an error to the UI
                                // Since there could be many feedback posts being inserted/updated.
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "updateLocalDb: feedback post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchFeedbackPosts", job)
            }

        }.asLiveData()
    }

    fun restoreFeedbackListFromCache(
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<FeedbackViewState>> {
        return object : NetworkBoundResource<FeedbackListSearchResponse, List<FeedbackPost>, FeedbackViewState>(
            sessionManager.isConnectedToTheInternet(),
            false,
            false,
            true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.feedbackFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.feedbackFields.feedbackList.size) {
                            viewState.feedbackFields.isQueryExhausted = true
                        }
                        onCompleteJob(
                            DataState.data(
                                viewState,
                                null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<FeedbackListSearchResponse>
            ) {
                // ignore
            }

            override fun createCall(): LiveData<GenericApiResponse<FeedbackListSearchResponse>> {
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<FeedbackViewState> {
                return feedbackPostDao.returnOrderedFeedbackQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap {
                        object : LiveData<FeedbackViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = FeedbackViewState(
                                    FeedbackFields(
                                        feedbackList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<FeedbackPost>?) {
                // ignore
            }

            override fun setJob(job: Job) {
                addJob("restoreFeedbackListFromCache", job)
            }

        }.asLiveData()
    }

    fun isAuthorOfFeedbackPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<FeedbackViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, FeedbackViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {


            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main) {

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    if (response.body.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT)) {
                        onCompleteJob(
                            DataState.data(
                                data = FeedbackViewState(
                                    viewFeedbackFields = ViewFeedbackFields(
                                        isAuthorOfFeedbackPost = false
                                    )
                                ),
                                response = null
                            )
                        )
                    } else if (response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)) {
                        onCompleteJob(
                            DataState.data(
                                data = FeedbackViewState(
                                    viewFeedbackFields = ViewFeedbackFields(
                                        isAuthorOfFeedbackPost = true
                                    )
                                ),
                                response = null
                            )
                        )
                    } else {
                        onErrorReturn(
                            ERROR_UNKNOWN,
                            shouldUseDialog = false,
                            shouldUseToast = false
                        )
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<FeedbackViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.isAuthorOfFeedbackPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            // not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfFeedbackPost", job)
            }


        }.asLiveData()
    }

    fun deleteFeedbackPost(
        authToken: AuthToken,
        feedbackPost: FeedbackPost
    ): LiveData<DataState<FeedbackViewState>> {
        return object : NetworkBoundResource<GenericResponse, FeedbackPost, FeedbackViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                if (response.body.response == SUCCESS_FEEDBACK_DELETED) {
                    updateLocalDb(feedbackPost)
                } else {
                    onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<FeedbackViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.deleteFeedbackPost(
                    "Token ${authToken.token!!}",
                    feedbackPost.slug
                )
            }

            override suspend fun updateLocalDb(cacheObject: FeedbackPost?) {
                cacheObject?.let { feedbackPost ->
                    feedbackPostDao.deleteFeedbackPost(feedbackPost)
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_FEEDBACK_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteFeedbackPost", job)
            }

        }.asLiveData()
    }

    fun updateFeedbackPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<FeedbackViewState>> {
        return object : NetworkBoundResource<FeedbackCreateUpdateResponse, FeedbackPost, FeedbackViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<FeedbackCreateUpdateResponse>
            ) {

                val updatedFeedbackPost = FeedbackPost(
                    response.body.pk,
                    response.body.title,
                    response.body.slug,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(response.body.date_updated),
                    response.body.username
                )

                updateLocalDb(updatedFeedbackPost)

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            FeedbackViewState(
                                viewFeedbackFields = ViewFeedbackFields(
                                    feedbackPost = updatedFeedbackPost
                                )
                            ),
                            Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<FeedbackViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<FeedbackCreateUpdateResponse>> {
                return openApiMainService.updateFeedback(
                    "Token ${authToken.token!!}",
                    slug,
                    title,
                    body,
                    image
                )
            }

            override suspend fun updateLocalDb(cacheObject: FeedbackPost?) {
                cacheObject?.let { feedbackPost ->
                    feedbackPostDao.updateFeedbackPost(
                        feedbackPost.pk,
                        feedbackPost.title,
                        feedbackPost.body,
                        feedbackPost.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("updateFeedbackPost", job)
            }

        }.asLiveData()
    }

}
















