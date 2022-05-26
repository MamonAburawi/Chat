package com.info.chat.data.message

import com.google.firebase.Timestamp
import com.info.chat.data.User


data class ChatParticipant(
    var particpant: User? = null,
    var lastMessage: String? = null,
    var lastMessageDate: Map<String, Double>? = null,
    var isLoggedUser: Boolean? = null,
    var lastMessageType: Double? = null
)

