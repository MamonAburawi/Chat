package com.info.chat.data.message

import com.google.firebase.Timestamp
import com.info.chat.utils.MessageType


/**3*/
data class RecordMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override var type: String = MessageType.RECORD.name,
    override val to: String?,
    override val senderName: String?,
    var duration: String?,
    val uri: String?,
    var currentProgress: String?,
    var isPlaying: Boolean?,
    override val isSeen: Boolean = false,
    override val isLoading: Boolean = false,
    override val isError: Boolean = false

) : Message

