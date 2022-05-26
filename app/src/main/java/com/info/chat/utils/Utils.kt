package com.info.chat.utils

import com.facebook.CallbackManager

class ConnectionChangeEvent (val message: String)

class CallbackManagerEvent(val callbackManager: CallbackManager/* Additional fields if needed */)




fun <T,R> Collection<T>.findDiffElements(elements: Collection<T>,selector:(T)->R?) =
    filter{t -> elements.none{selector(it) == selector(t)}}



fun <T,R> Collection<T>.findCommonElements(elements: Collection<T>,selector:(T)->R?) =
    filter{t -> elements.none{selector(it) != selector(t)}}