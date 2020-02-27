package com.cyberveda.client.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cyberveda.client.R
import com.cyberveda.client.models.AccountProperties
import com.cyberveda.client.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : BaseAccountFragment() {
    private val TAG = "lgx_AccountFragment"

    var authTokenEmail: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        change_password.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }

        logout_button.setOnClickListener {
            viewModel.logout()
        }







        subscribeObservers()
    }

    private fun subscribeObservers() {
        Log.d(TAG, "subscribeObservers: 46: called")
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            Log.d(TAG, "AccountFragment: DataState: $dataState")
            stateChangeListener.onDataStateChange(dataState)
            if (dataState != null) {
                dataState.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { viewState ->
                            viewState.accountProperties?.let { accountProperties ->
                                Log.d(TAG, "AccountFragment, DataState: ${accountProperties}")
                                viewModel.setAccountPropertiesData(accountProperties)
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "AccountFragment, ViewState: ${viewState}")
            if (viewState != null) {
                viewState.accountProperties?.let {
                    setAccountDataFields(it)
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: 77: called")
        viewModel.setStateEvent(AccountStateEvent.GetAccountPropertiesEvent())

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: 85: called")

    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        email?.setText(accountProperties.email)
        username?.setText(accountProperties.username)

        if (accountProperties.email == "guest@gmail.com") {
            require_login_feedback_text?.text =
                "You are logged in as a guest - To post feedback, press 'Logout' button and Login as a member."
            feedback_button_to_layout.isEnabled = false

        } else {
            feedback_button_to_layout.isEnabled = true
            feedback_button_to_layout.setBackgroundResource(R.drawable.red_button_drawable)
            feedback_button_to_layout.setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_createFeedbackFragment)
            }

        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}