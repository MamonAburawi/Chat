package com.info.chat.data.message

import com.google.firebase.Timestamp

interface MessageApi {
    val id: String?
    val from: String?
    val created_at: Timestamp?
    val type: Double?
    val to: String?
    val senderName: String?
}
