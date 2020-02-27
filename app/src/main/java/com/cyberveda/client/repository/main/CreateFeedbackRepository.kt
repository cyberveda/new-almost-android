package com.cyberveda.client.repository.main

import androidx.lifecycle.LiveData
import com.cyberveda.client.api.main.OpenApiMainService
import com.cyberveda.client.api.main.responses.FeedbackCreateUpdateResponse
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.persistence.FeedbackPostDao
import com.cyberveda.client.repository.JobManager
import com.cyberveda.client.repository.NetworkBoundResource
import com.cyberveda.client.session.SessionManager
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.Response
import com.cyberveda.client.ui.ResponseType
import com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackViewState
import com.cyberveda.client.util.AbsentLiveData
import com.cyberveda.client.util.ApiSuccessResponse
import com.cyberveda.client.util.DateUtils
import com.cyberveda.client.util.GenericApiResponse
import com.cyberveda.client.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CYBERVEDA_MEMBER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateFeedbackRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val feedbackPostDao: FeedbackPostDao,
    val sessionManager: SessionManager
): JobManager("CreateFeedbackRepository") {

    private val TAG = "lgx_CreateFeedbackRepository"
    fun createNewFeedbackPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateFeedbackViewState>> {
        return object :
            NetworkBoundResource<FeedbackCreateUpdateResponse, FeedbackPost, CreateFeedbackViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<FeedbackCreateUpdateResponse>) {

                // If they don't have a paid membership account it will still return a 200
                // Need to account for that
                if (!response.body.response.equals(RESPONSE_MUST_BECOME_CYBERVEDA_MEMBER)) {
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
                }

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(response.body.response, ResponseType.Dialog())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<FeedbackCreateUpdateResponse>> {
                return openApiMainService.createFeedback(
                    "Token ${authToken.token!!}",
                    title,
                    body,
                    image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateFeedbackViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: FeedbackPost?) {
                cacheObject?.let {
                    feedbackPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewFeedbackPost", job)
            }

        }.asLiveData()
    }
}
















