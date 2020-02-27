package com.cyberveda.client.messagingmvvm.ui.findUser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cyberveda.client.messagingmvvm.Utils.AuthEmailUtil
import com.cyberveda.client.messagingmvvm.Utils.AuthUtil
import com.cyberveda.client.messagingmvvm.Utils.FirestoreUtil
import com.cyberveda.client.messagingmvvm.model.User
import com.cyberveda.client.messagingmvvm.ui.incoming_requests.FRIENDS
import com.google.firebase.firestore.EventListener

class FindUserViewModel : ViewModel() {
    private val TAG = "lgx_FindUserViewModel"

    private val userDocumentsMutableLiveData = MutableLiveData<MutableList<User?>>()


    fun loadUsers(): LiveData<MutableList<User?>> {


        val docRef = FirestoreUtil.firestoreInstance.collection("users")
        docRef.get()
            .addOnSuccessListener { querySnapshot ->
                //add any user that isn't logged in user to result
                val result = mutableListOf<User?>()

                Log.d(TAG, "loadUsers: 29:  ${AuthEmailUtil.getAuthEmail()}")

                if (AuthEmailUtil.getAuthEmail() == "admin@gmail.com") {

                    Log.d(TAG, "loadUsers: 32: ${AuthEmailUtil.getAuthEmail()}")

                    Log.d(TAG, "loadUsers: 31: HERE ${AuthUtil.getAuthId()}")
                    Log.d(TAG, "loadUsers: 32: ")

                    for (document in querySnapshot.documents) {

                        if (!document.get("uid").toString().equals(AuthUtil.getAuthId())) {
                            val user = document.toObject(User::class.java)

                            Log.d(TAG, "loadUsers: 39: $user")

                            result.add(user)
                        }

                    }
                } else {
                    for (document in querySnapshot.documents) {


                        if (!document.get("uid").toString().equals(AuthUtil.getAuthId())) {

                            val user = document.toObject(User::class.java)

                            if (user?.email == "admin@gmail.com") {


                                Log.d(TAG, "loadUsers: 53: ${user?.email}")
                                result.add(user)

                            }

                            else {
                                if (user?.email == "admin@gmail.com") {
                                    result.add(user)
                                }

                                Log.d(TAG, "loadUsers: 54: HERE OTHERS $user")
                                Log.d(TAG, "loadUsers: 58: ${user?.email}")
                            }

                        }

                    }

                }

                // remove friends of logged in user from result list
                docRef.whereArrayContains(FRIENDS, AuthUtil.getAuthId())
                    .addSnapshotListener(
                        EventListener { querySnapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException == null) {
                                val documents = querySnapshot?.documents
                                if (documents != null) {
                                    for (document in documents) {
                                        val user = document.toObject(User::class.java)
                                        result.remove(user)

                                    }

                                    userDocumentsMutableLiveData.value = result


                                }
                            } else {
                                userDocumentsMutableLiveData.value = null
                            }
                        })


            }
            .addOnFailureListener { exception ->
                userDocumentsMutableLiveData.value = null
            }

        return userDocumentsMutableLiveData
    }


}
