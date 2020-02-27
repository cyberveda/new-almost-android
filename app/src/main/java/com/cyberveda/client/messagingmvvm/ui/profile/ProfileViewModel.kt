package com.cyberveda.client.messagingmvvm.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cyberveda.client.messagingmvvm.Utils.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class ProfileViewModel(val app: Application) : AndroidViewModel(app) {

    val uploadImageLoadStateMutableLiveData = MutableLiveData<LoadState>()
    val newImageUriMutableLiveData = MutableLiveData<Uri>()
    private lateinit var mStorageRef: StorageReference


    private var userDocRef: DocumentReference? = AuthUtil.getAuthId().let {
        FirestoreUtil.firestoreInstance.collection("users").document(it)
    }

    var bioLoadState = MutableLiveData<LoadState>()
    var ageLoadState = MutableLiveData<LoadState>()
    var genderLoadState = MutableLiveData<LoadState>()
    var maritalStatusLoadState = MutableLiveData<LoadState>()
    var educationLoadState = MutableLiveData<LoadState>()


    fun updateBio(bio: String) {


        userDocRef?.update("bio", bio)
            ?.addOnSuccessListener {
                bioLoadState.value = LoadState.SUCCESS

            }
            ?.addOnFailureListener {
                bioLoadState.value = LoadState.FAILURE
            }

    }

//    fun isAgeCorrect(it: Int): LiveData<Boolean> {
//
//        val pattern: Pattern = Pattern.compile(emailRegex)
//        val matcher: Matcher = pattern.matcher(it)
//        emailMatch.value = matcher.matches()
//
//        return emailMatch
//    }

    fun updateAge(age: Int) {


        userDocRef?.update("age", age)
            ?.addOnSuccessListener {
                ageLoadState.value = LoadState.SUCCESS

            }
            ?.addOnFailureListener {
                ageLoadState.value = LoadState.FAILURE
            }

    }

    fun updateGender(gender: String) {


        userDocRef?.update("gender", gender)
            ?.addOnSuccessListener {
                genderLoadState.value = LoadState.SUCCESS

            }
            ?.addOnFailureListener {
                genderLoadState.value = LoadState.FAILURE
            }

    }

    fun updateMaritalStatus(maritalStatus: String) {


        userDocRef?.update("maritalStatus", maritalStatus)
            ?.addOnSuccessListener {
                maritalStatusLoadState.value = LoadState.SUCCESS

            }
            ?.addOnFailureListener {
                maritalStatusLoadState.value = LoadState.FAILURE
            }

    }

    fun updateEducation(education: String) {


        userDocRef?.update("education", education)
            ?.addOnSuccessListener {
                educationLoadState.value = LoadState.SUCCESS

            }
            ?.addOnFailureListener {
                educationLoadState.value = LoadState.FAILURE
            }

    }

    fun uploadProfileImageByUri(data: Uri?) {
        //show upload ui
        uploadImageLoadStateMutableLiveData.value = LoadState.LOADING

        mStorageRef = StorageUtil.storageInstance.reference
        val ref = mStorageRef.child("profile_pictures/" + data?.lastPathSegment + Date().time)
        var uploadTask = data?.let { ref.putFile(it) }

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                uploadImageLoadStateMutableLiveData.value = LoadState.FAILURE
            }
            ref.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveImageUriInFirebase(downloadUri)
            } else {
                uploadImageLoadStateMutableLiveData.value = LoadState.FAILURE
            }
        }
    }


    fun uploadImageAsBytearray(bytes: ByteArray) {

        //show upload ui
        uploadImageLoadStateMutableLiveData.value = LoadState.LOADING

        mStorageRef = StorageUtil.storageInstance.reference
        val ref = mStorageRef.child("profile_pictures/" + System.currentTimeMillis())
        var uploadTask = bytes.let { ref.putBytes(it) }

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                uploadImageLoadStateMutableLiveData.value = LoadState.FAILURE
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveImageUriInFirebase(downloadUri)
            } else {
                uploadImageLoadStateMutableLiveData.value = LoadState.FAILURE
            }
        }

    }

    //save download uri of image in the user document
    private fun saveImageUriInFirebase(downloadUri: Uri?) {

        AuthUtil.getAuthId().let {
            FirestoreUtil.firestoreInstance.collection("users").document(it)
                .update(PROFILE_PICTURE_URL, downloadUri.toString())
                .addOnSuccessListener {
                    uploadImageLoadStateMutableLiveData.value = LoadState.SUCCESS
                    newImageUriMutableLiveData.value = downloadUri

                }
                .addOnFailureListener {
                    uploadImageLoadStateMutableLiveData.value = LoadState.FAILURE
                }
        }

    }




}
