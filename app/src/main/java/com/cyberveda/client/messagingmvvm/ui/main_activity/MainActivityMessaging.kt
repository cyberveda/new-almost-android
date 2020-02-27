package com.cyberveda.client.messagingmvvm.ui.main


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyberveda.client.R
import com.cyberveda.client.databinding.ActivityMainMessagingBinding
import com.cyberveda.client.messagingmvvm.Utils.CLICKED_USER
import com.cyberveda.client.messagingmvvm.Utils.eventbus_events.CallbackManagerEvent
import com.cyberveda.client.messagingmvvm.Utils.eventbus_events.ConnectionChangeEvent
import com.cyberveda.client.messagingmvvm.Utils.eventbus_events.KeyboardEvent
import com.cyberveda.client.messagingmvvm.model.User
import com.cyberveda.client.messagingmvvm.ui.main_activity.SharedViewModel
import com.facebook.CallbackManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivityMessaging : AppCompatActivity() {


    var isActivityRecreated = false
    lateinit var callbackManager: CallbackManager
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: ActivityMainMessagingBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //register to event bus to receive callbacks
        EventBus.getDefault().register(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_messaging)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        //change title text color of toolbar to white
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.whiteMessaging))


        //hide toolbar on signup,login fragments
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_messaging)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            title = when (destination.id) {
                R.id.homeFragment -> getString(R.string.home)
                R.id.contactsFragment -> getString(R.string.contacts)
                R.id.differentUserProfile -> " "
                R.id.profileFragment -> getString(R.string.my_profile)


                else -> " "
            }

            if (destination.label == "SignupFragment" || destination.label == "LoginFragment") {
                binding.toolbar.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE
            }
        }


        //setup toolbar with navigation
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.loginFragment))
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)


    }


    // Show snackbar whenever the connection state changes
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onConnectionChangeEvent(event: ConnectionChangeEvent): Unit {
        if (!isActivityRecreated) {//to not show toast on configuration changes
            Snackbar.make(binding.coordinator, event.message, Snackbar.LENGTH_LONG).show()
        }
    }


    //facebook fragment will pass callbackManager to activity to continue FB login
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallbackManagerEvent) {
        callbackManager = event.callbackManager
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyboardEvent(event: KeyboardEvent) {
        hideKeyboard()
    }


    private fun hideKeyboard() {

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.toolbar.windowToken, 0)

    }


}



