package com.info.chat.data.message

import com.google.firebase.Timestamp


/**0*/
data class TextMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    val text: String?
) : MessageApi
