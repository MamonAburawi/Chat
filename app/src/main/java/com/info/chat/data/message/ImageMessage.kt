package com.info.chat.data.message

import com.google.firebase.Timestamp
import com.info.chat.utils.MessageType


/**1*/
data class ImageMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override var type: String = MessageType.IMAGE.name,
    override val to: String?,
    override val senderName: String?,
    var uri: String?,
    override val isSeen: Boolean = false,
    override var isLoading: Boolean = false,
    override var isError: Boolean = false

) : Message
