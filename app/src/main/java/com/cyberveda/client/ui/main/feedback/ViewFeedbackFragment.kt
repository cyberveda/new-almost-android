package com.cyberveda.client.ui.main.feedback

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cyberveda.client.R
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.ui.AreYouSureCallback
import com.cyberveda.client.ui.UIMessage
import com.cyberveda.client.ui.UIMessageType
import com.cyberveda.client.ui.main.feedback.state.FeedbackStateEvent.*
import com.cyberveda.client.ui.main.feedback.viewmodel.*
import com.cyberveda.client.util.DateUtils
import com.cyberveda.client.util.SuccessHandling.Companion.SUCCESS_FEEDBACK_DELETED
import kotlinx.android.synthetic.main.fragment_view_feedback.*

class ViewFeedbackFragment : BaseFeedbackFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfFeedbackPost()
        stateChangeListener.expandAppBar()

        delete_button.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    fun confirmDeleteRequest(){
        val callback: AreYouSureCallback = object: AreYouSureCallback {

            override fun proceed() {
                deleteFeedbackPost()
            }

            override fun cancel() {
                // ignore
            }

        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    fun deleteFeedbackPost(){
        viewModel.setStateEvent(
            DeleteFeedbackPostEvent()
        )
    }

    fun checkIsAuthorOfFeedbackPost(){
        viewModel.setIsAuthorOfFeedbackPost(false) // reset
        viewModel.setStateEvent(CheckAuthorOfFeedbackPost())
    }

    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            stateChangeListener.onDataStateChange(dataState)

            if(dataState != null){
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setIsAuthorOfFeedbackPost(
                            viewState.viewFeedbackFields.isAuthorOfFeedbackPost
                        )
                    }
                    data.response?.peekContent()?.let{ response ->
                        if(response.message.equals(SUCCESS_FEEDBACK_DELETED)){
                            viewModel.removeDeletedFeedbackPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewFeedbackFields.feedbackPost?.let{ feedbackPost ->
                setFeedbackProperties(feedbackPost)
            }

            if(viewState.viewFeedbackFields.isAuthorOfFeedbackPost){
                adaptViewToAuthorMode()
            }
        })
    }

    fun adaptViewToAuthorMode(){
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    fun setFeedbackProperties(feedbackPost: FeedbackPost){
        dependencyProvider.getGlideRequestManager()
            .load(feedbackPost.image)
            .into(feedback_image)
        feedback_title.setText(feedbackPost.title)
        feedback_author.setText(feedbackPost.username)
        feedback_update_date.setText(DateUtils.convertLongToStringDate(feedbackPost.date_updated))
        feedback_body.setText(feedbackPost.body)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(viewModel.isAuthorOfFeedbackPost()){
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(viewModel.isAuthorOfFeedbackPost()){
            when(item.itemId){
                R.id.edit -> {
                    navUpdateFeedbackFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateFeedbackFragment(){
        try{
            // prep for next fragment
            viewModel.setUpdatedFeedbackFields(
                viewModel.getFeedbackPost().title,
                viewModel.getFeedbackPost().body,
                viewModel.getFeedbackPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewFeedbackFragment_to_updateFeedbackFragment)
        }catch (e: Exception){
            // send error report or something. These fields should never be null. Not possible
            Log.e(TAG, "Exception: ${e.message}")
        }
    }
}













