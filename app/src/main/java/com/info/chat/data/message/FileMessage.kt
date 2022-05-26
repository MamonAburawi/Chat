package com.info.chat.data.message

import com.google.firebase.Timestamp


/**2*/
data class FileMessage(
    override val id: String?,
    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    val name: String?,
    val uri: String?
) : MessageApi
