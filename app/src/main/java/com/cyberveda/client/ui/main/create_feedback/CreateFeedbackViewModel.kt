package com.cyberveda.client.ui.main.create_feedback

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.cyberveda.client.repository.main.CreateFeedbackRepository
import com.cyberveda.client.session.SessionManager
import com.cyberveda.client.ui.BaseViewModel
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.Loading
import com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackStateEvent
import com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackStateEvent.*
import com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackViewState
import com.cyberveda.client.ui.main.create_feedback.state.CreateFeedbackViewState.*
import com.cyberveda.client.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateFeedbackViewModel
@Inject
constructor(
    val createFeedbackRepository: CreateFeedbackRepository,
    val sessionManager: SessionManager
): BaseViewModel<CreateFeedbackStateEvent, CreateFeedbackViewState>() {

    override fun handleStateEvent(
        stateEvent: CreateFeedbackStateEvent
    ): LiveData<DataState<CreateFeedbackViewState>> {

        when(stateEvent){

            is CreateNewFeedbackEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(MediaType.parse("text/plain"), stateEvent.title)
                    val body = RequestBody.create(MediaType.parse("text/plain"), stateEvent.body)

                    createFeedbackRepository.createNewFeedbackPost(
                        authToken,
                        title,
                        body,
                        stateEvent.image
                    )
                }?: AbsentLiveData.create()
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

    override fun initNewViewState(): CreateFeedbackViewState {
        return CreateFeedbackViewState()
    }

    fun setNewFeedbackFields(title: String?, body: String?, uri: Uri?){
        val update = getCurrentViewStateOrNew()
        val newFeedbackFields = update.feedbackFields
        title?.let{ newFeedbackFields.newFeedbackTitle = it }
        body?.let{ newFeedbackFields.newFeedbackBody = it }
        uri?.let{ newFeedbackFields.newImageUri = it }
        update.feedbackFields = newFeedbackFields
        _viewState.value = update
    }

    fun clearNewFeedbackFields(){
        val update = getCurrentViewStateOrNew()
        update.feedbackFields = NewFeedbackFields()
        setViewState(update)
    }

    fun cancelActiveJobs(){
        createFeedbackRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}











