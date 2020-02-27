package com.cyberveda.client.repository.main

import androidx.lifecycle.LiveData
import com.cyberveda.client.api.main.OpenApiMainService
import com.cyberveda.client.api.main.responses.BlogCreateUpdateResponse
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.models.BlogPost
import com.cyberveda.client.persistence.BlogPostDao
import com.cyberveda.client.repository.JobManager
import com.cyberveda.client.repository.NetworkBoundResource
import com.cyberveda.client.session.SessionManager
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.Response
import com.cyberveda.client.ui.ResponseType
import com.cyberveda.client.ui.main.create_blog.state.CreateBlogViewState
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

class CreateBlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("CreateBlogRepository") {

    private val TAG = "lgx_CreateBlogRepository"
    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {

                // If they don't have a paid membership account it will still return a 200
                // Need to account for that
                if (!response.body.response.equals(RESPONSE_MUST_BECOME_CYBERVEDA_MEMBER)) {
                    val updatedBlogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )
                    updateLocalDb(updatedBlogPost)
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

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                    "Token ${authToken.token!!}",
                    title,
                    body,
                    image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }
}
















