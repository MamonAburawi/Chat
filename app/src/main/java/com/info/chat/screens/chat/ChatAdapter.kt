
package com.info.chat.screens.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.info.chat.R
import com.info.chat.data.message.*
import com.info.chat.databinding.*
import com.info.chat.utils.AuthUtil
import com.info.chat.utils.MediaPlayer


/**
you can set the view type for specific message type by using the constant values without change the message type.
 * **/


class ChatAdapter(private val context: Context?) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallbackMessages()) {


    lateinit var clickListener: MessageClickListener


    interface MessageClickListener {
        fun onMessageClick(position: Int, message: Message)
    }



    companion object {
        private const val TAG = "ChatAdapter"

        lateinit var messageList: MutableList<Message>

        private const val TYPE_SENT_MESSAGE = 0
        private const val TYPE_RECEIVED_MESSAGE = 1
        private const val TYPE_SENT_IMAGE_MESSAGE = 2
        private const val TYPE_RECEIVED_IMAGE_MESSAGE = 3
        private const val TYPE_SENT_FILE_MESSAGE = 4
        private const val TYPE_RECEIVED_FILE_MESSAGE = 5
        private const val TYPE_SENT_RECORD = 6
        private const val TYPE_RECEIVED_RECORD = 7
        private const val TYPE_SENT_RECORD_PLACEHOLDER = 8
        private const val TYPE_SENT_IMAGE_LOADING = 9
        private const val TYPE_SENT_IMAGE_MESSAGE_ERROR = 10 // make this for any error happening during sending or when the client offline.
        private const val TYPE_SENT_SEEN_IMAGE_MESSAGE_PLACEHOLDER = 11

    }


    // here you can set the layout type depending on view type of the item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_SENT_MESSAGE -> { SentMessageViewHolder.from(parent) }
            TYPE_RECEIVED_MESSAGE -> { ReceivedMessageViewHolder.from(parent) }
            TYPE_SENT_IMAGE_MESSAGE -> { SentImageMessageViewHolder.from(parent) }
            TYPE_SENT_IMAGE_LOADING -> { SentImageLoadingViewHolder.from(parent) }
            TYPE_RECEIVED_IMAGE_MESSAGE -> { ReceivedImageMessageViewHolder.from(parent) }
            TYPE_SENT_FILE_MESSAGE -> { SentFileMessageViewHolder.from(parent) }
            TYPE_RECEIVED_FILE_MESSAGE -> { ReceivedFileMessageViewHolder.from(parent) }
            TYPE_SENT_RECORD -> { SentRecordViewHolder.from(parent) }
            TYPE_RECEIVED_RECORD -> { ReceivedRecordViewHolder.from(parent) }
            TYPE_SENT_RECORD_PLACEHOLDER -> { SentRecordPlaceHolderViewHolder.from(parent) }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentMessageViewHolder -> { holder.bind(clickListener,  getItem(position) as TextMessage) }
            is ReceivedMessageViewHolder -> { holder.bind(clickListener, getItem(position) as TextMessage) }
            is SentImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is SentImageLoadingViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is SentImageErrorViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is ReceivedImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is ReceivedFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
            is SentFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
            is SentRecordViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            is ReceivedRecordViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            is SentRecordPlaceHolderViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }


    override fun getItemViewType(position: Int): Int {

        val currentMessage = getItem(position)

        if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 0.0) {
            return TYPE_SENT_MESSAGE
        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 0.0) {
            return TYPE_RECEIVED_MESSAGE
        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 1.0) {
            return TYPE_SENT_IMAGE_MESSAGE
        }
        else if (currentMessage.type == 9.0){
            return TYPE_SENT_IMAGE_LOADING
        }
        else if (currentMessage.type == 10.0){
            return TYPE_SENT_IMAGE_MESSAGE_ERROR
        }


        else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 1.0) {
            return TYPE_RECEIVED_IMAGE_MESSAGE
        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 2.0) {
            return TYPE_SENT_FILE_MESSAGE
        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 2.0) {
            return TYPE_RECEIVED_FILE_MESSAGE
        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 3.0) {
            return TYPE_SENT_RECORD
        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 3.0) {
            return TYPE_RECEIVED_RECORD
        } else if (currentMessage.type == 8.0) {
            return TYPE_SENT_RECORD_PLACEHOLDER
        } else {

            throw IllegalArgumentException("Invalid ItemViewType")
        }

    }


    //----------------SentMessageViewHolder------------
    class SentMessageViewHolder private constructor(val binding: ItemSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: TextMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): SentMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentMessageBinding.inflate(layoutInflater, parent, false)
                return SentMessageViewHolder(binding)
            }

        }


    }

    /**----------------ReceivedMessageViewHolder------------**/
    class ReceivedMessageViewHolder private constructor(val binding: ItemIncomingMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: TextMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }


        companion object {

            fun from(parent: ViewGroup): ReceivedMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemIncomingMessageBinding.inflate(layoutInflater, parent, false)
                return ReceivedMessageViewHolder(binding)
            }

        }
    }

    /**----------------SentImageMessageViewHolder------------**/
    class SentImageMessageViewHolder private constructor(val binding: ItemSentImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: ImageMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): SentImageMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentImageBinding.inflate(layoutInflater, parent, false)
                return SentImageMessageViewHolder(binding)
            }

        }
    }


    /**----------------SentImageLoadingViewHolder------------**/
    class SentImageLoadingViewHolder private constructor(val binding: ItemSentImageLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: ImageMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): SentImageLoadingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentImageLoadingBinding.inflate(layoutInflater, parent, false)
                return SentImageLoadingViewHolder(binding)
            }

        }
    }


    /**----------------SentImageLoadingViewHolder------------**/
    class SentImageErrorViewHolder private constructor(val binding: ItemSentImageErrorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: ImageMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): SentImageErrorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentImageErrorBinding.inflate(layoutInflater, parent, false)
                return SentImageErrorViewHolder(binding)
            }

        }
    }



    /**----------------ReceivedImageMessageViewHolder------------**/
    class ReceivedImageMessageViewHolder private constructor(val binding: ItemIncomingImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: ImageMessage) {

            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): ReceivedImageMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemIncomingImageBinding.inflate(layoutInflater, parent, false)
                return ReceivedImageMessageViewHolder(binding)
            }

        }
    }


    /**----------------SentFileMessageViewHolder------------**/
    class SentFileMessageViewHolder private constructor(val binding: ItemSentFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: FileMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {

            fun from(parent: ViewGroup): SentFileMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentFileBinding.inflate(layoutInflater, parent, false)
                return SentFileMessageViewHolder(binding)
            }

        }
    }


    /**----------------ReceivedFileMessageViewHolder------------**/
    class ReceivedFileMessageViewHolder private constructor(val binding: ItemIncomingFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: FileMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }

        }

        companion object {
            fun from(parent: ViewGroup): ReceivedFileMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemIncomingFileBinding.inflate(layoutInflater, parent, false)
                return ReceivedFileMessageViewHolder(binding)
            }
        }

    }


    /**----------------SentRecordViewHolder------------**/
    class SentRecordViewHolder private constructor(val binding: ItemSentAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: RecordMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()



                //reset views (to reset other records other than the one playing)
                val recordMessage = messageList[adapterPosition] as RecordMessage
                recordMessage.isPlaying = false

                btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                progressbar.max = 0
                durationTextView.text = ""


                /** button play pause **/
                btnPlayPause.setOnClickListener {

                    MediaPlayer.startPlaying(
                        item.uri!!,
                        adapterPosition,
                        recordMessage,
                        binding.btnPlayPause,
                        binding.progressbar
                    )

                }
            }



        }

        companion object {

            fun from(parent: ViewGroup): SentRecordViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentAudioBinding.inflate(layoutInflater, parent, false)
                return SentRecordViewHolder(binding)
            }

        }


    }


    //----------------SentRecordPlaceHolderViewHolder------------
    class SentRecordPlaceHolderViewHolder private constructor(val binding: ItemSentAudioPlaceholderBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(onClick: MessageClickListener, item: RecordMessage) {
            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()
            }


        }

        companion object {
            fun from(parent: ViewGroup): SentRecordPlaceHolderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentAudioPlaceholderBinding.inflate(layoutInflater, parent, false)
                return SentRecordPlaceHolderViewHolder(binding)
            }
        }


    }


    /**----------------ReceivedRecordViewHolder------------**/
    internal class ReceivedRecordViewHolder (val binding: ItemIncomingAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick: MessageClickListener, item: RecordMessage) {

            binding.apply {
                message = item
                clickListener = onClick
                position = adapterPosition
                executePendingBindings()


                //reset views (to reset other records other than the one playing)
                val recordMessage = messageList[adapterPosition] as RecordMessage
                recordMessage.isPlaying = false


                btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                binding.progressbar.max = 0
                binding.durationTextView.text = ""


                /** button play pause **/
                btnPlayPause.setOnClickListener {
                    MediaPlayer.startPlaying(
                        item.uri!!,
                        adapterPosition,
                        recordMessage,
                        btnPlayPause,
                        progressbar
                    )


                }
            }


        }

        companion object {

            fun from(parent: ViewGroup): ReceivedRecordViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemIncomingAudioBinding.inflate(layoutInflater, parent, false)
                return ReceivedRecordViewHolder(binding)
            }

        }
    }





    class DiffCallbackMessages : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.created_at == newItem.created_at

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem

    }


}

//
//package com.info.chat.screens.chat
//
//import android.content.Context
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.info.chat.R
//import com.info.chat.data.message.*
//import com.info.chat.databinding.*
//import com.info.chat.utils.AuthUtil
//import com.info.chat.utils.MediaPlayer
//
//
//class ChatAdapter(private val context: Context?, private val clickListener: MessageClickListener) :
//    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallbackMessages()) {
//
//
//    interface MessageClickListener {
//        fun onMessageClick(position: Int, message: Message)
//    }
//
//
//    companion object {
//        private const val TYPE_SENT_MESSAGE = 0
//        private const val TYPE_RECEIVED_MESSAGE = 1
//        private const val TYPE_SENT_IMAGE_MESSAGE = 2
//        private const val TYPE_RECEIVED_IMAGE_MESSAGE = 3
//        private const val TYPE_SENT_FILE_MESSAGE = 4
//        private const val TYPE_RECEIVED_FILE_MESSAGE = 5
//        private const val TYPE_SENT_RECORD = 6
//        private const val TYPE_RECEIVED_RECORD = 7
//        private const val TYPE_SENT_RECORD_PLACEHOLDER = 8
//
//        lateinit var messageList: MutableList<Message>
//
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//
//        return when (viewType) {
//            TYPE_SENT_MESSAGE -> { SentMessageViewHolder.from(parent) }
//            TYPE_RECEIVED_MESSAGE -> { ReceivedMessageViewHolder.from(parent) }
//            TYPE_SENT_IMAGE_MESSAGE -> { SentImageMessageViewHolder.from(parent) }
//            TYPE_RECEIVED_IMAGE_MESSAGE -> { ReceivedImageMessageViewHolder.from(parent) }
//            TYPE_SENT_FILE_MESSAGE -> { SentFileMessageViewHolder.from(parent) }
//            TYPE_RECEIVED_FILE_MESSAGE -> { ReceivedFileMessageViewHolder.from(parent) }
//            TYPE_SENT_RECORD -> { SentRecordViewHolder.from(parent) }
//            TYPE_RECEIVED_RECORD -> { ReceivedRecordViewHolder.from(parent) }
//            TYPE_SENT_RECORD_PLACEHOLDER -> { SentRecordPlaceHolderViewHolder.from(parent) }
//            else -> throw IllegalArgumentException("Invalid view type")
//        }
//
//    }
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (holder) {
//            is SentMessageViewHolder -> { holder.bind(clickListener, getItem(position) as TextMessage) }
//            is ReceivedMessageViewHolder -> { holder.bind(clickListener, getItem(position) as TextMessage) }
//            is SentImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
//            is ReceivedImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
//            is ReceivedFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
//            is SentFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
//            is SentRecordViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
//            is ReceivedRecordViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
//            is SentRecordPlaceHolderViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
//            else -> throw IllegalArgumentException("Invalid ViewHolder type")
//        }
//    }
//
//    override fun getItemViewType(position: Int): Int {
//
//        val currentMessage = getItem(position)
//
//        if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 0.0) {
//            return TYPE_SENT_MESSAGE
//        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 0.0) {
//            return TYPE_RECEIVED_MESSAGE
//        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 1.0) {
//            return TYPE_SENT_IMAGE_MESSAGE
//        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 1.0) {
//            return TYPE_RECEIVED_IMAGE_MESSAGE
//        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 2.0) {
//            return TYPE_SENT_FILE_MESSAGE
//        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 2.0) {
//            return TYPE_RECEIVED_FILE_MESSAGE
//        } else if (currentMessage.from == AuthUtil.getAuthId() && currentMessage.type == 3.0) {
//            return TYPE_SENT_RECORD
//        } else if (currentMessage.from != AuthUtil.getAuthId() && currentMessage.type == 3.0) {
//            return TYPE_RECEIVED_RECORD
//        } else if (currentMessage.type == 8.0) {
//            return TYPE_SENT_RECORD_PLACEHOLDER
//        } else {
//
//            throw IllegalArgumentException("Invalid ItemViewType")
//        }
//
//    }
//
//
//    //----------------SentMessageViewHolder------------
//    class SentMessageViewHolder private constructor(val binding: ItemSentMessageBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: TextMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): SentMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemSentMessageBinding.inflate(layoutInflater, parent, false)
//                return SentMessageViewHolder(binding)
//            }
//
//        }
//
//
//    }
//
//    /**----------------ReceivedMessageViewHolder------------**/
//    class ReceivedMessageViewHolder private constructor(val binding: ItemIncomingMessageBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: TextMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//
//        companion object {
//
//            fun from(parent: ViewGroup): ReceivedMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemIncomingMessageBinding.inflate(layoutInflater, parent, false)
//                return ReceivedMessageViewHolder(binding)
//            }
//
//        }
//    }
//
//    /**----------------SentImageMessageViewHolder------------**/
//    class SentImageMessageViewHolder private constructor(val binding: ItemSentImageBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: ImageMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): SentImageMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemSentImageBinding.inflate(layoutInflater, parent, false)
//                return SentImageMessageViewHolder(binding)
//            }
//
//        }
//    }
//
//
//    /**----------------ReceivedImageMessageViewHolder------------**/
//    class ReceivedImageMessageViewHolder private constructor(val binding: ItemIncomingImageBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: ImageMessage) {
//
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): ReceivedImageMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemIncomingImageBinding.inflate(layoutInflater, parent, false)
//                return ReceivedImageMessageViewHolder(binding)
//            }
//
//        }
//    }
//
//
//    /**----------------SentFileMessageViewHolder------------**/
//    class SentFileMessageViewHolder private constructor(val binding: ItemSentFileBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: FileMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): SentFileMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemSentFileBinding.inflate(layoutInflater, parent, false)
//                return SentFileMessageViewHolder(binding)
//            }
//
//        }
//    }
//
//
//    /**----------------ReceivedFileMessageViewHolder------------**/
//    class ReceivedFileMessageViewHolder private constructor(val binding: ItemIncomingFileBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: FileMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//        }
//
//        companion object {
//            fun from(parent: ViewGroup): ReceivedFileMessageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemIncomingFileBinding.inflate(layoutInflater, parent, false)
//                return ReceivedFileMessageViewHolder(binding)
//            }
//        }
//
//    }
//
//
//    /**----------------SentRecordViewHolder------------**/
//    class SentRecordViewHolder private constructor(val binding: ItemSentAudioBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: RecordMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//
//
//
//                //reset views (to reset other records other than the one playing)
//                val recordMessage = messageList[adapterPosition] as RecordMessage
//                recordMessage.isPlaying = false
//
//                btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
//                progressbar.max = 0
//                durationTextView.text = ""
//
//
//                /** button play pause **/
//                btnPlayPause.setOnClickListener {
//
//                    MediaPlayer.startPlaying(
//                        item.uri!!,
//                        adapterPosition,
//                        recordMessage,
//                        binding.btnPlayPause,
//                        binding.progressbar
//                    )
//
//                }
//            }
//
//
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): SentRecordViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemSentAudioBinding.inflate(layoutInflater, parent, false)
//                return SentRecordViewHolder(binding)
//            }
//
//        }
//
//
//    }
//
//
//    //----------------SentRecordPlaceHolderViewHolder------------
//    class SentRecordPlaceHolderViewHolder private constructor(val binding: ItemSentAudioPlaceholderBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//
//        fun bind(onClick: MessageClickListener, item: RecordMessage) {
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//            }
//
//
//        }
//
//        companion object {
//            fun from(parent: ViewGroup): SentRecordPlaceHolderViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemSentAudioPlaceholderBinding.inflate(layoutInflater, parent, false)
//                return SentRecordPlaceHolderViewHolder(binding)
//            }
//        }
//
//
//    }
//
//
//    /**----------------ReceivedRecordViewHolder------------**/
//    internal class ReceivedRecordViewHolder (val binding: ItemIncomingAudioBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(onClick: MessageClickListener, item: RecordMessage) {
//
//            binding.apply {
//                message = item
//                clickListener = onClick
//                position = adapterPosition
//                executePendingBindings()
//
//
//                //reset views (to reset other records other than the one playing)
//                val recordMessage = messageList[adapterPosition] as RecordMessage
//                recordMessage.isPlaying = false
//
//
//                btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
//                binding.progressbar.max = 0
//                binding.durationTextView.text = ""
//
//
//                /** button play pause **/
//                btnPlayPause.setOnClickListener {
//                    MediaPlayer.startPlaying(
//                        item.uri!!,
//                        adapterPosition,
//                        recordMessage,
//                        btnPlayPause,
//                        progressbar
//                    )
//
//
//                }
//            }
//
//
//        }
//
//        companion object {
//
//            fun from(parent: ViewGroup): ReceivedRecordViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = ItemIncomingAudioBinding.inflate(layoutInflater, parent, false)
//                return ReceivedRecordViewHolder(binding)
//            }
//
//        }
//    }
//
//
//
//
//
//    class DiffCallbackMessages : DiffUtil.ItemCallback<Message>() {
//        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
//            return oldItem.created_at == newItem.created_at
//        }
//
//        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
//            return oldItem.equals(newItem)
//        }
//    }
//
//
//}
//
//
//
//
//
//




