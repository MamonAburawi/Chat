package com.info.chat.utils


import com.facebook.CallbackManager
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates


enum class MessageType{
    TEXT, IMAGE, TEXT_IMAGE, FILE,TEXT_FILE,RECORD,TEXT_RECORD
}

class ConnectionChangeEvent (val message: String)

class CallbackManagerEvent(val callbackManager: CallbackManager/* Additional fields if needed */)

class UpdateRecycleItemEvent(val adapterPosition: Int)

var positionDelegate: Int by Delegates.observable(-1) { prop, old, new ->
    println("<positionDelegate>.:${old},,,,$new")
    if (old != new && old != -1)    //if old =-1 or old=new don't update item
        EventBus.getDefault().post(UpdateRecycleItemEvent(old))
}


fun <T,R> Collection<T>.findDiffElements(elements: Collection<T>,selector:(T)->R?) =
    filter{t -> elements.none{selector(it) == selector(t)}}


fun <T,R> Collection<T>.findCommonElements(elements: Collection<T>,selector:(T)->R?) =
    filter{t -> elements.none{selector(it) != selector(t)}}


fun getMessageId(): String {
    return getRandomID(12)
}


private fun getRandomID(l: Int) = List(l) {
    (('a'..'z') + ('A'..'Z')).random()
}.joinToString("")






