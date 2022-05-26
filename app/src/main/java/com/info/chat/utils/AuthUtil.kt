package com.info.chat.utils

import com.google.firebase.auth.FirebaseAuth

object AuthUtil {

    val firebaseAuthInstance: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    fun getAuthId(): String {
        return firebaseAuthInstance.currentUser!!.uid
    }
}