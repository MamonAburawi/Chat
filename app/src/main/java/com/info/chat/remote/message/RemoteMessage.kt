package com.info.chat.remote.message

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.info.chat.data.message.*
import com.info.chat.screens.chat.toDataClass
import com.info.chat.utils.StorageUtil
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RemoteMessage() {

    companion object{
        const val TAG = "RemoteMessage"
    }

    private val _fireStoreRoot = FirebaseFirestore.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _fireStorageRoot = FirebaseStorage.getInstance()

    private fun messagesCollection() =  _fireStoreRoot.collection("messages")

    private val userId = _auth.currentUser?.uid!!

    private val messagesLiveData = MutableLiveData<List<Message>>()

    fun loadDirectMessages(receiverId: String): LiveData<List<Message>> {
        val messagesList = ArrayList<Message>()
        messagesCollection().addSnapshotListener { querySnapShot, error ->
            if (error == null) {
                querySnapShot?.documents?.forEach {
                    if (it.id == "${userId}_${receiverId}" || it.id == "${receiverId}_${userId}") {
                        //this is the chat document we should read messages array
                        val messagesFromFirestore =
                            it.get("messages") as List<HashMap<String, Any>>?
                                ?: throw Exception("My cast can't be done")
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

                        if (messagesList.isNotEmpty())
                            messagesLiveData.value = messagesList

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