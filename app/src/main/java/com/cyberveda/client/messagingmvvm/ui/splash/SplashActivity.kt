package com.cyberveda.client.messagingmvvm.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyberveda.client.messagingmvvm.ui.main.MainActivityMessaging

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //show logo till app finish loading
        startActivity(Intent(this, MainActivityMessaging::class.java))
        finish()


    }
}
