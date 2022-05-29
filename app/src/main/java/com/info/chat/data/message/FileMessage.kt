package com.info.chat.data.message

import com.google.firebase.Timestamp
import com.info.chat.utils.MessageType


/**2*/
data class FileMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override var type: String = MessageType.FILE.name,
    override val to: String?,
    override val senderName: String?,
    val name: String?,
    var uri: String?,
    override val isSeen: Boolean = false,
    override val isLoading: Boolean = false,
    override val isError: Boolean = false
) : Message
