package com.cyberveda.client.messagingmvvm.model


data class User(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val profile_picture_url: String? = null,
    var sentRequests: List<String>? = null,
    var receivedRequests: List<String>? = null,
    var friends: List<String>? = null,
    var bio: String? = null,
    var age: Int? = null,
    var gender: String? = null,
    var maritalStatus: String? = null,
    var education: String? = null
)