package com.info.chat.screens.chat

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.info.chat.data.message.*
import com.info.chat.remote.message.RemoteMessage
import com.info.chat.repository.message.MessageRepository
import com.info.chat.utils.eventbus_events.PermissionEvent
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class ChatViewModel(private val senderId: String?, private val receiverId: String) : ViewModel() {

    companion object{
        const val TAG = "ChatViewModel"
    }

    private val messageRepository = MessageRepository(RemoteMessage())

    val fileUri = MutableLiveData<Map<String, Any?>>()
    val imageUri = MutableLiveData<Uri>()
    val recordUri = MutableLiveData<Uri>()


    private val _isSnackBarVisible = MutableLiveData<String?>()
    val isSnackBarVisible = _isSnackBarVisible


    val messages = messageRepository.loadDirectMessages(receiverId)


    fun showSnackBar(text: String){
        _isSnackBarVisible.value = text
    }


    fun sendMessage(message: Message){
        viewModelScope.launch {
            messageRepository.sendMessage(message)
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


    fun downloadFile(activity: Activity, message: Message) {
        //check for storage permission then download if granted
        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    //download file
                    val downloadManager =
                        activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri = Uri.parse((message as FileMessage).uri)
                    val request = DownloadManager.Request(uri)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        uri.lastPathSegment
                    )
                    downloadManager.enqueue(request)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                    //notify parent activity that permission denied to show toast for manual permission giving
                    showSnackBar("Permission is needed for this feature to work")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    //notify parent activity that permission denied to show toast for manual permission giving
                    EventBus.getDefault().post(PermissionEvent())
                }
            }).check()
    }


}



