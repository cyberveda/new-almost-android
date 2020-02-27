package com.cyberveda.client.ui.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.cyberveda.client.R
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.ui.auth.state.AuthStateEvent
import com.cyberveda.client.util.Lgx
import kotlinx.android.synthetic.main.fragment_launcher.*
import kotlinx.android.synthetic.main.fragment_login.*

class LauncherFragment : BaseAuthFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_launcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register.setOnClickListener {
            Lgx.d("LGX", "register clicked")
            navRegistration()
        }

        login.setOnClickListener {
            navLogin()
        }

        app_logo.setOnClickListener {
            guestLogin()
        }

        forgot_password.setOnClickListener {
            navForgotPassword()
        }

        focusable_view.requestFocus() // reset focus

//        Log.d(TAG, "LauncherFragment: ${viewModel}")
    }

    fun guestLogin(){
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                "guest@gmail.com",
                "A123456z"
            )
        )
    }

    fun navLogin(){
        findNavController().navigate(R.id.action_launcherFragment_to_loginFragment)
    }

//    fun directToMain(){
//        viewModel.setAuthToken(AuthToken(-99, "dksj"))
//
//    }

    fun navRegistration(){
        findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
    }


    fun navForgotPassword(){
        findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
    }

}





















