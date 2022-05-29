package com.info.chat.data.message

import com.google.firebase.Timestamp

interface Message {
    val id: String?
    val from: String?
    val created_at: Timestamp?
    var type: String
    val to: String?
    val senderName: String?
    val isSeen: Boolean
    val isLoading: Boolean
    val isError: Boolean
}
