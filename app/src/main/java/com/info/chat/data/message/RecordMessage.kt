package com.info.chat.data.message

import com.google.firebase.Timestamp


/**3*/
data class RecordMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    var duration: String?,
    val uri: String?,
    var currentProgress: String?,
    var isPlaying: Boolean?

) : Message

