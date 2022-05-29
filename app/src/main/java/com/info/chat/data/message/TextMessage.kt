package com.info.chat.data.message

import com.google.firebase.Timestamp
import com.info.chat.utils.MessageType


/**0*/
data class TextMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override val to: String?,
    override val senderName: String?,
    val text: String?,
    override val isSeen: Boolean = false,
    override val isLoading: Boolean = false,
    override val isError: Boolean = false,
    override var type: String = MessageType.TEXT.name
) : Message
