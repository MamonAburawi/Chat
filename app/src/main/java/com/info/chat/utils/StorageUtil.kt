package com.info.chat.utils

import com.google.firebase.storage.FirebaseStorage

object StorageUtil {

    val storageInstance: FirebaseStorage by lazy {
        println("StorageUtil.:")
        FirebaseStorage.getInstance()

    }
}