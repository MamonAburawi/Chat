package com.info.chat.remote.message

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.info.chat.data.message.*
import com.info.chat.remote.message.RemoteMessage.Companion.convert
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RemoteMessage() {

    companion object{
        const val TAG = "RemoteMessage"
        const val MESSAGES_FIELD = "messages"



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
            val gson = Gson()
            val json = gson.toJson(this)
            return gson.fromJson(json, object : TypeToken<O>() {}.type)
        }


    }

    private val _fireStoreRoot = FirebaseFirestore.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _fireStorageRoot = FirebaseStorage.getInstance()

    private fun messagesCollection() =  _fireStoreRoot.collection("messages")

    private val userId = _auth.currentUser?.uid!!

    private val messagesLiveData = MutableLiveData<List<Message>>()

    suspend fun sendMessage(message: Message) {
        //todo add last message date field to chat members document so we can sort home chats with

        val senderId = message.from
        val receiverId = message.to
        //so we don't create multiple nodes for same chat
        messagesCollection().document("${senderId}_${receiverId}").get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    //this node exists send your message
                    messagesCollection().document("${senderId}_${receiverId}")
                        .update(MESSAGES_FIELD, FieldValue.arrayUnion(message.serializeToMap()))

                } else {
                    //senderId_receiverId node doesn't exist check receiverId_senderId
                    messagesCollection().document("${receiverId}_${senderId}").get()
                        .addOnSuccessListener { documentSnapshot2 ->

                            if (documentSnapshot2.exists()) {
                                messagesCollection().document("${receiverId}_${senderId}")
                                    .update(MESSAGES_FIELD, FieldValue.arrayUnion(message.serializeToMap()))
                            } else {
                                //no previous chat history(senderId_receiverId & receiverId_senderId both don't exist)
                                //so we create document senderId_receiverId then messages array then add messageMap to messages
                                messagesCollection().document("${senderId}_${receiverId}")
                                    .set(
                                        mapOf(MESSAGES_FIELD to mutableListOf<Message>()),
                                        SetOptions.merge()
                                    ).addOnSuccessListener {
                                        //this node exists send your message
                                        messagesCollection().document("${senderId}_${receiverId}")
                                            .update(MESSAGES_FIELD,FieldValue.arrayUnion(message.serializeToMap()))

                                        //add ids of chat members
                                        messagesCollection().document("${senderId}_${receiverId}")
                                            .update(MESSAGES_FIELD, FieldValue.arrayUnion(senderId, receiverId))

                                    }
                            }
                        }
                }
            }

    }







    fun loadDirectMessages(receiverId: String): LiveData<List<Message>> {
        val messagesList = ArrayList<Message>()
        messagesCollection().addSnapshotListener { querySnapShot, error ->
            if (error == null) {
                querySnapShot?.documents?.forEach {
                    if (it.id == "${userId}_${receiverId}" || it.id == "${receiverId}_${userId}") {
                        //this is the chat document we should read messages array
                        val messagesFromFirestore = it.get("messages") as List<HashMap<String, Any>>? ?:
                        throw Exception("My cast can't be done")
                        messagesList.clear() // to prevent duplication
                        messagesFromFirestore.forEach { messageHashMap ->

                            // here we set every single message in specific data class.
                            val message = when (messageHashMap["type"] as Double?) {
                                0.0 -> { messageHashMap.toDataClass<TextMessage>() }
                                1.0 -> { messageHashMap.toDataClass<ImageMessage>() }
                                2.0 -> { messageHashMap.toDataClass<FileMessage>() }
                                3.0 -> { messageHashMap.toDataClass<RecordMessage>() }
                                else -> { throw Exception("unknown type") }
                            }

                            messagesList.add(message)
                        }

                        if (messagesList.isNotEmpty()){

                            Log.d(TAG,"message: ${messagesList.size}")
                            messagesLiveData.value = messagesList
                        }


                    }

                }
            }
        }

        return messagesLiveData
    }



    suspend fun uploadRecord(filePath: String): Uri? {
        val ref = _fireStorageRoot.reference.child("records/" + Date().time)
        val uploadTask = ref.putFile(Uri.fromFile(File(filePath)))

        val uriRef = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
               Log.d(TAG,"UpLoading Record: error: ${task.exception?.message}")
            }
            ref.downloadUrl
        }
        return uriRef.await()
    }


    suspend fun uploadFile(filePath: Uri): Uri? {
        val ref = _fireStorageRoot.reference.child("chat_files/$filePath")
        return uploadFileByUri(filePath,ref)
    }

    suspend fun uploadImage(imagePath: Uri): Uri? {
        val ref = _fireStorageRoot.reference.child("chat_pictures/$imagePath")
        return uploadFileByUri(imagePath,ref)
    }



    private suspend fun uploadFileByUri(uri: Uri, ref: StorageReference): Uri?{
        val uploadTask = uri.let { ref.putFile(it) }
        val task = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                //error
                Log.d(TAG,"UpLoading: error: ${task.exception?.message}")
            }
            ref.downloadUrl
        }
        return task.await()
    }






}