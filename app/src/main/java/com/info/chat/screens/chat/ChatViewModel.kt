package com.info.chat.screens.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.info.chat.utils.FirestoreUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.info.chat.data.message.*
import com.info.chat.remote.message.RemoteMessage
import com.info.chat.repository.message.MessageRepository
import kotlinx.coroutines.launch


class ChatViewModel(private val senderId: String?, private val receiverId: String) : ViewModel() {

    companion object{
        const val TAG = "ChatViewModel"
    }

    private val messageRepository = MessageRepository(RemoteMessage())

    private lateinit var mStorageRef: StorageReference
    private val messageCollectionReference = FirestoreUtil.firestoreInstance.collection("messages")
    private val messagesList: MutableList<Message> by lazy { mutableListOf<Message>() }
    val fileUri = MutableLiveData<Map<String, Any?>>()
    val imageUri = MutableLiveData<Uri>()
    val recordUri = MutableLiveData<Uri>()

    val messages: LiveData<List<Message>> get() =
        messageRepository.loadMessages(receiverId)





    fun sendMessage(message: Message) {
        //todo add last message date field to chat members document so we can sort home chats with

        //so we don't create multiple nodes for same chat
        messageCollectionReference.document("${senderId}_${receiverId}").get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    //this node exists send your message
                    messageCollectionReference.document("${senderId}_${receiverId}")
                        .update("messages", FieldValue.arrayUnion(message.serializeToMap()))

                } else {
                    //senderId_receiverId node doesn't exist check receiverId_senderId
                    messageCollectionReference.document("${receiverId}_${senderId}").get()
                        .addOnSuccessListener { documentSnapshot2 ->

                            if (documentSnapshot2.exists()) {
                                messageCollectionReference.document("${receiverId}_${senderId}")
                                    .update(
                                        "messages",
                                        FieldValue.arrayUnion(message.serializeToMap())
                                    )
                            } else {
                                //no previous chat history(senderId_receiverId & receiverId_senderId both don't exist)
                                //so we create document senderId_receiverId then messages array then add messageMap to messages
                                messageCollectionReference.document("${senderId}_${receiverId}")
                                    .set(
                                        mapOf("messages" to mutableListOf<Message>()),
                                        SetOptions.merge()
                                    ).addOnSuccessListener {
                                        //this node exists send your message
                                        messageCollectionReference.document("${senderId}_${receiverId}")
                                            .update(
                                                "messages",
                                                FieldValue.arrayUnion(message.serializeToMap())
                                            )

                                        //add ids of chat members
                                        messageCollectionReference.document("${senderId}_${receiverId}")
                                            .update(
                                                "chat_members",
                                                FieldValue.arrayUnion(senderId, receiverId)
                                            )

                                    }
                            }
                        }
                }
            }

    }


    fun uploadFile(filePath: Uri) {
        viewModelScope.launch {
            val downloadUri = messageRepository.uploadFile(filePath)
            fileUri.value = mapOf<String, Any?>(
                "downloadUri" to downloadUri,
                "fileName" to filePath
            )
        }
    }


    fun uploadRecord(filePath: String) {
        viewModelScope.launch {
            val uri = messageRepository.uploadRecord(filePath)
            recordUri.value = uri
        }
    }

    fun uploadImage(imagePath: Uri) {
        viewModelScope.launch {
            val uri = messageRepository.uploadImage(imagePath)
            imageUri.value = uri
        }
    }


}


val gson = Gson()

//convert a data class to a map
fun <T> T.serializeToMap(): Map<String, Any> {
    return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
    return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}



