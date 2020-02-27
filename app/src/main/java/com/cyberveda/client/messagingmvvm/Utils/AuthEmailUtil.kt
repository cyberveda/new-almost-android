package com.cyberveda.client.messagingmvvm.Utils


import com.google.firebase.auth.FirebaseAuth

object AuthEmailUtil {

    val firebaseAuthInstance: FirebaseAuth by lazy {
        println("firebaseAuthInstance.:")
        FirebaseAuth.getInstance()
    }


    fun getAuthEmail(): String {


        return firebaseAuthInstance.currentUser?.email.toString()


    }
}