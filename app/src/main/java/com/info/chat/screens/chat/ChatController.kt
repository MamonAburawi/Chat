//package com.info.chat.screens.chat
//
//import android.content.Context
//import android.media.MediaPlayer
//import android.os.CountDownTimer
//import android.util.Log
//import android.view.LayoutInflater
//import android.widget.ImageView
//import android.widget.ProgressBar
//import androidx.databinding.DataBindingUtil
//import com.airbnb.epoxy.TypedEpoxyController
//import com.google.firebase.auth.FirebaseAuth
//import com.info.chat.*
//import com.info.chat.data.message.*
//import com.info.chat.databinding.ItemIncomingAudioBinding
//import com.info.chat.utils.eventbus_events.UpdateRecycleItemEvent
//import org.greenrobot.eventbus.EventBus
//import java.io.IOException
//import kotlin.properties.Delegates
//
//
//class ChatController(private val context: Context): TypedEpoxyController<List<MessageApi>>() {
//
//    lateinit var clickListener: MessageClickListener
//    private var player = MediaPlayer()
//    private lateinit var countDownTimer: CountDownTimer
//
//    var positionDelegate: Int by Delegates.observable(-1) { prop, old, new ->
//        println("<positionDelegate>.:${old},,,,$new")
//        if (old != new && old != -1)    //if old =-1 or old=new don't update item
//            EventBus.getDefault().post(UpdateRecycleItemEvent(old))
//
//    }
//
//
//    interface MessageClickListener {
//        fun onMessageClick(position: Int, messageApi: MessageApi)
//    }
//
//    private val userId = FirebaseAuth.getInstance().currentUser?.uid!!
//
//    companion object{
//        const val TAG = "ChatController"
//
//        private const val TYPE_MESSAGE = 0.0
//        private const val TYPE_IMAGE = 1.0
//        private const val TYPE_FILE = 2.0
//        private const val TYPE_RECORD = 3.0
//        private const val TYPE_RECORD_PLACEHOLDER = 4.0
//    }
//
//    override fun buildModels(data: List<MessageApi>?) {
//        data?.forEachIndexed { index, message ->
//
//            Log.d(TAG,"type: ${message.type}")
//            when(message.type){
//                TYPE_MESSAGE -> {
//                    if (userId == message.from){ // sender
//                        sentMessage(message as TextMessage,index)
//                    }else{ // recipient
//                        receivedMessage(message as TextMessage, index)
//                    }
//                }
//                TYPE_IMAGE ->{
//                    if (userId == message.from){ // sender
//                        sentImage(message as ImageMessage , index)
//                    }else{ // recipient
//                        receivedImage(message as ImageMessage,index)
//                    }
//                }
//                TYPE_FILE ->{
//                    if (userId == message.from){ // sender
//                        sentFile(message as FileMessage, index)
//                    }else{ // recipient
//                        receivedFile(message as FileMessage, index)
//                    }
//                }
//                TYPE_RECORD ->{
//                    if (userId == message.from){ // sender
//                        sentRecord(message as RecordMessage, index)
//                    }else{ // recipient
//                        receivedRecord(message as RecordMessage, index)
//                    }
//                }
//
//            }
//
//        }
//    }
//
//
//    private fun sentMessage(message: TextMessage, index: Int){
//        sentMessage {
//            id(message.text)
//            message(message)
//            clickListener { v->
//                clickListener.onMessageClick(index,message)
//            }
//        }
//    }
//
//    private fun receivedMessage(message: TextMessage, index: Int){
//        incomingMessage {
//            id(message.text)
//            message(message)
//            clickListener { v->
//                clickListener.onMessageClick(index,message)
//            }
//        }
//    }
//
//    private fun sentImage(message: ImageMessage, index: Int){
//        sentImage {
//            id(message.uri)
//            message(message)
//            clickListener { v->
//                clickListener.onMessageClick(index,message)
//            }
//        }
//    }
//
//    private fun receivedImage(message: ImageMessage,position: Int){
//        incomingImage {
//            id(message.uri)
//            message(message)
//            clickListener{ v->
//                clickListener.onMessageClick(position,message)
//            }
//        }
//    }
//
//    private fun sentFile(message: FileMessage, index: Int){
//        sentFile {
//            id(message.uri)
//            message(message)
//            clickListener { v->
//                clickListener.onMessageClick(index,message)
//            }
//        }
//    }
//
//    private fun receivedFile(message: FileMessage, index: Int){
//        incomingFile {
//            id(message.uri)
//            message(message)
//            clickListener { v->
//                clickListener.onMessageClick(index,message)
//            }
//        }
//    }
//
//    private fun sentRecord(recordMessage: RecordMessage, index: Int){
//        sentAudio {
//
//            recordMessage.isPlaying = false
//
//            id(recordMessage.uri)
//            message(recordMessage)
//            clickListener { v->
//                clickListener.onMessageClick(index,recordMessage)
//            }
//        }
//    }
//
//    private fun receivedRecord(recordMessage: RecordMessage, index: Int){
//
//        incomingAudio {
//            val inflater = LayoutInflater.from(context)
//            val binding = DataBindingUtil.inflate<ItemIncomingAudioBinding>(inflater,R.layout.item_incoming_audio,null,false)
//
//            recordMessage.isPlaying = false
//
////            binding.playPauseImage.setImageResource(R.drawable.ic_play_arrow_black_24dp)
////            binding.progressbar.max = 0
////            binding.durationTextView.text = ""
//
//            id(recordMessage.uri)
//            message(recordMessage)
//            play { v->
//
//                startPlaying(
//                    recordMessage.uri!!,
//                    index,
//                    recordMessage,
//                    v as ImageView,
//                    binding.progressbar)
//            }
//        }
//    }
//
//
//
//
//
//
//
//
//
////    binding.playPauseImage.setOnClickListener {
////        startPlaying(
////            item.uri!!,
////            adapterPosition,
////            recordMessage,
////            binding.playPauseImage,
////            binding.progressbar
////        )
////
////
////    }
////
//
//
//    private fun startPlaying(
//        audioUri: String,
//        adapterPosition: Int,
//        recordMessage: RecordMessage,
//        playPauseImage: ImageView,
//        progressbar: ProgressBar
//    ) {
//        //update last clicked item to be reset
//        positionDelegate = adapterPosition
//
//        //show temporary loading while audio is downloaded
//        playPauseImage.setImageResource(R.drawable.loading_animation)
//
//        if (recordMessage.isPlaying == null || recordMessage.isPlaying == false) {
//
//            stopPlaying()
//            recordMessage.isPlaying = false
//
//            player.apply {
//                try {
//                    setDataSource(audioUri)
//                    prepareAsync()
//                } catch (e: IOException) {
//                    println("ChatFragment.startPlaying:prepare failed")
//                }
//
//                setOnPreparedListener {
//                    //media downloaded and will play
//
//                    recordMessage.isPlaying = true
//                    //play the record
//                    start()
//
//                    //change image to stop and show progress of record
//                    progressbar.max = player.duration
//                    playPauseImage.setImageResource(R.drawable.ic_stop_black_24dp)
//
//
//                    //count down timer to show record progess but on when record is playing
//                    countDownTimer = object : CountDownTimer(player.duration.toLong(), 50) {
//                        override fun onFinish() {
//
//                            progressbar.progress = (player.duration)
//                            playPauseImage.setImageResource(R.drawable.ic_play_arrow_black_24dp)
//
//                        }
//
//                        override fun onTick(millisUntilFinished: Long) {
//
//                            progressbar.progress = (player.duration.minus(millisUntilFinished)).toInt()
//
//
//                        }
//
//                    }.start()
//                }
//            }
//
//        } else {
//            //stop the record
//            playPauseImage.setImageResource(R.drawable.ic_play_arrow_black_24dp)
//            stopPlaying()
//            recordMessage.isPlaying = false
//            progressbar.progress = 0
//
//        }
//
//
//    }
//
//
//    private fun stopPlaying() {
//        if (::countDownTimer.isInitialized)
//            countDownTimer.cancel()
//        player.reset()
//    }
//
//
//
//
//
//}