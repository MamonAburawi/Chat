package com.info.chat.repository.message

import android.net.Uri
import com.info.chat.remote.message.RemoteMessage

class MessageRepository(private val remoteMessage: RemoteMessage) {


    fun loadMessages(recipientId: String) = remoteMessage.loadDirectMessages(recipientId)

    suspend fun uploadRecord(filePath: String) = remoteMessage.uploadRecord(filePath)

    suspend fun uploadFile(filePath: Uri) = remoteMessage.uploadFile(filePath)

    suspend fun uploadImage(imagePath: Uri) = remoteMessage.uploadImage(imagePath)
}