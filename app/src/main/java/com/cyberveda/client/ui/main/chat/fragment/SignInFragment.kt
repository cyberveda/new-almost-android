package com.cyberveda.client.ui.main.chat.fragment


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyberveda.client.R
import com.cyberveda.client.messagingmvvm.Utils.AuthUtil
import com.cyberveda.client.messagingmvvm.ui.main.MainActivityMessaging
import com.cyberveda.client.ui.displayToast
import com.firebase.ui.auth.AuthUI

import kotlinx.android.synthetic.main.fragment_sign_in.*


class SignInFragment : Fragment() {

    private val TAG = "lgx_SignInFragment"

    private val RC_SIGN_IN = 1

    private val signInProviders =
        listOf(
            AuthUI.IdpConfig.EmailBuilder()
                .setAllowNewAccounts(true)
                .setRequireName(true)
                .build()
        )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Chat"

        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: 29: ")
//        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
//
//        (activity as AppCompatActivity).supportActionBar?.title = "Example 1"
        Toast.makeText(
            context,
            "onViewCreated",
            Toast.LENGTH_SHORT
        ).show()

        Log.d(TAG, "onViewCreated: 74:onViewCreated ")
        checkIfFirebaseUserIsLoggedIn()

        account_sign_in.setOnClickListener {
            goToMainActivityMessaging()

        }
    }


    fun goToMainActivityMessaging() {

        val intent = Intent(this@SignInFragment.context, MainActivityMessaging::class.java)

        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    private fun checkIfFirebaseUserIsLoggedIn() {
        if (AuthUtil.getAuthId().isNotEmpty()) {

            account_sign_in.text = getString(R.string.continue_to_chat)

            goToMainActivityMessaging()

        } else {
            account_sign_in.text = getString(R.string.continue_to_chat)
        }

    }


    fun login() {

        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(signInProviders)
            .setLogo(R.drawable.ic_check_green_24dp)
            .build()
        startActivityForResult(intent, RC_SIGN_IN)

    }


}
