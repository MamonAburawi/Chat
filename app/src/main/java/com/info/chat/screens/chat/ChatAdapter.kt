
package com.info.chat.screens.chat

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.info.chat.R
import com.info.chat.data.message.*
import com.info.chat.databinding.*
import com.info.chat.utils.AuthUtil
import com.info.chat.utils.MediaPlayer
import com.info.chat.utils.MessageType


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

        // text message views
        const val TYPE_SENT_MESSAGE = 0
        const val TYPE_RECEIVED_MESSAGE = 1
        const val TYPE_SENT_MESSAGE_LOADING = 2
        const val TYPE_SENT_MESSAGE_ERROR = 3

        // image message views
        const val TYPE_SENT_IMAGE_MESSAGE = 4
        const val TYPE_RECEIVED_IMAGE_MESSAGE = 5
        const val TYPE_SENT_IMAGE_MESSAGE_LOADING= 6
        const val TYPE_SENT_IMAGE_MESSAGE_ERROR = 7

        // file message views
        const val TYPE_SENT_FILE_MESSAGE = 8
        const val TYPE_RECEIVED_FILE_MESSAGE = 9
        const val TYPE_SENT_FILE_MESSAGE_LOADING = 10
        const val TYPE_SENT_FILE_MESSAGE_ERROR = 11

        // record message views
        const val TYPE_SENT_RECORD = 12
        const val TYPE_RECEIVED_RECORD = 13
        const val TYPE_SENT_RECORD_LOADING = 14
        const val TYPE_SENT_RECORD_ERROR = 15

    }


    // here you can set the layout type depending on view type of the item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            // message view
            TYPE_SENT_MESSAGE -> { SentMessageViewHolder.from(parent) }
            TYPE_SENT_MESSAGE_LOADING ->{SentMessageLoadingViewHolder.from(parent) }
            TYPE_SENT_MESSAGE_ERROR ->{SentMessageErrorViewHolder.from(parent) }
            TYPE_RECEIVED_MESSAGE -> { ReceivedMessageViewHolder.from(parent) }

            // image message view
            TYPE_SENT_IMAGE_MESSAGE -> { SentImageMessageViewHolder.from(parent) }
            TYPE_RECEIVED_IMAGE_MESSAGE -> { ReceivedImageMessageViewHolder.from(parent) }
            TYPE_SENT_IMAGE_MESSAGE_LOADING -> { SentImageLoadingViewHolder.from(parent) }
            TYPE_SENT_IMAGE_MESSAGE_ERROR -> { SentImageLoadingViewHolder.from(parent) }

            // file message view
            TYPE_SENT_FILE_MESSAGE -> { SentFileMessageViewHolder.from(parent) }
            TYPE_RECEIVED_FILE_MESSAGE -> { ReceivedFileMessageViewHolder.from(parent) }
            TYPE_SENT_FILE_MESSAGE_LOADING -> { SentFileMessageLoadingViewHolder.from(parent) }
            TYPE_SENT_FILE_MESSAGE_ERROR -> { SentFileMessageErrorViewHolder.from(parent) }

            // record message view
            TYPE_SENT_RECORD -> { SentRecordMessageViewHolder.from(parent) }
            TYPE_RECEIVED_RECORD -> { ReceivedRecordMessageViewHolder.from(parent) }
            TYPE_SENT_RECORD_LOADING -> { SentRecordMessageLoadingViewHolder.from(parent) }
            TYPE_SENT_RECORD_ERROR -> { SentRecordMessageErrorViewHolder.from(parent) }

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            // text message
            is SentMessageViewHolder -> { holder.bind(clickListener,  getItem(position) as TextMessage) }
            is ReceivedMessageViewHolder -> { holder.bind(clickListener, getItem(position) as TextMessage) }
            is SentMessageLoadingViewHolder -> { holder.bind(clickListener,  getItem(position) as TextMessage) }
            is SentMessageErrorViewHolder -> { holder.bind(clickListener,  getItem(position) as TextMessage) }

            // image message
            is SentImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is ReceivedImageMessageViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is SentImageLoadingViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }
            is SentImageErrorViewHolder -> { holder.bind(clickListener, getItem(position) as ImageMessage) }

            // file message
            is SentFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
            is ReceivedFileMessageViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
            is SentFileMessageLoadingViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }
            is SentFileMessageErrorViewHolder -> { holder.bind(clickListener, getItem(position) as FileMessage) }

            // record message
            is SentRecordMessageViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            is ReceivedRecordMessageViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            is SentRecordMessageLoadingViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }
            is SentRecordMessageErrorViewHolder -> { holder.bind(clickListener, getItem(position) as RecordMessage) }

            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }


    override fun getItemViewType(position: Int): Int {

        val currentMessage = getItem(position)
        val userId = AuthUtil.getAuthId()

        // text message
        if (currentMessage.from == userId && currentMessage.type == MessageType.TEXT.name) {
            return TYPE_SENT_MESSAGE
        } else if (currentMessage.from != userId && currentMessage.type == MessageType.TEXT.name) {
            return TYPE_RECEIVED_MESSAGE
        } else if ( currentMessage.type == TYPE_SENT_MESSAGE_LOADING.toString()) {
            return TYPE_SENT_MESSAGE_LOADING
        } else if (currentMessage.type == TYPE_SENT_MESSAGE_ERROR.toString()) {
            return TYPE_SENT_MESSAGE_ERROR
        }


        // image message
        else if (currentMessage.from == userId && currentMessage.type == MessageType.IMAGE.name) {
            return TYPE_SENT_IMAGE_MESSAGE
        } else if (currentMessage.from != userId && currentMessage.type == MessageType.IMAGE.name) {
            return TYPE_RECEIVED_IMAGE_MESSAGE
        } else if (currentMessage.type == TYPE_SENT_IMAGE_MESSAGE_LOADING.toString()){
            return TYPE_SENT_IMAGE_MESSAGE_LOADING
        } else if (currentMessage.type == TYPE_SENT_IMAGE_MESSAGE_ERROR.toString()){
            return TYPE_SENT_IMAGE_MESSAGE_ERROR
        }


        // file message
        else if (currentMessage.from == userId && currentMessage.type == MessageType.FILE.name) {
            return TYPE_SENT_FILE_MESSAGE
        } else if (currentMessage.from != userId && currentMessage.type == MessageType.FILE.name) {
            return TYPE_RECEIVED_FILE_MESSAGE
        } else if (currentMessage.type == TYPE_SENT_FILE_MESSAGE_LOADING.toString()){
            return TYPE_SENT_FILE_MESSAGE_LOADING
        } else if (currentMessage.type == TYPE_SENT_FILE_MESSAGE_ERROR.toString()){
            return TYPE_SENT_FILE_MESSAGE_ERROR
        }


        // record message
        else if (currentMessage.from == userId && currentMessage.type == MessageType.RECORD.name) {
            return TYPE_SENT_RECORD
        } else if (currentMessage.from != userId && currentMessage.type == MessageType.RECORD.name) {
            return TYPE_RECEIVED_RECORD
        } else if (currentMessage.type == TYPE_SENT_RECORD_LOADING.toString() ) {
            return TYPE_SENT_RECORD_LOADING
        }

        else {
            throw IllegalArgumentException("Invalid ItemViewType")
        }

    }


    /**----------------SentMessageViewHolder------------**/
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
    class ReceivedMessageViewHolder private constructor(val binding: ItemReceivedMessageBinding) :
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
                val binding = ItemReceivedMessageBinding.inflate(layoutInflater, parent, false)
                return ReceivedMessageViewHolder(binding)
            }

        }
    }



    /**----------------SentMessageLoadingViewHolder------------**/
    class SentMessageLoadingViewHolder private constructor(val binding: ItemSentMessageLoadingBinding) :
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

            fun from(parent: ViewGroup): SentMessageLoadingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentMessageLoadingBinding.inflate(layoutInflater, parent, false)
                return SentMessageLoadingViewHolder(binding)
            }

        }
    }


    /**----------------SentMessageErrorViewHolder------------**/
    class SentMessageErrorViewHolder private constructor(val binding: ItemSentMessageErrorBinding) :
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

            fun from(parent: ViewGroup): SentMessageErrorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentMessageErrorBinding.inflate(layoutInflater, parent, false)
                return SentMessageErrorViewHolder(binding)
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
    class ReceivedImageMessageViewHolder private constructor(val binding: ItemReceivedImageBinding) :
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
                val binding = ItemReceivedImageBinding.inflate(layoutInflater, parent, false)
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
    class ReceivedFileMessageViewHolder private constructor(val binding: ItemReceivedFileBinding) :
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
                val binding = ItemReceivedFileBinding.inflate(layoutInflater, parent, false)
                return ReceivedFileMessageViewHolder(binding)
            }
        }

    }


    /**----------------SentFileMessageLoadingViewHolder------------**/
    class SentFileMessageLoadingViewHolder private constructor(val binding: ItemSentFileLoadingBinding) :
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
            fun from(parent: ViewGroup): SentFileMessageLoadingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentFileLoadingBinding.inflate(layoutInflater, parent, false)
                return SentFileMessageLoadingViewHolder(binding)
            }
        }

    }


    /**----------------SentFileErrorViewHolder------------**/
    class SentFileMessageErrorViewHolder private constructor(val binding: ItemSentFileErrorBinding) :
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

            fun from(parent: ViewGroup): SentFileMessageErrorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentFileErrorBinding.inflate(layoutInflater, parent, false)
                return SentFileMessageErrorViewHolder(binding)
            }

        }
    }



    /**----------------SentRecordViewHolder------------**/
    class SentRecordMessageViewHolder private constructor(val binding: ItemSentRecordBinding) :
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

            fun from(parent: ViewGroup): SentRecordMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentRecordBinding.inflate(layoutInflater, parent, false)
                return SentRecordMessageViewHolder(binding)
            }

        }


    }


    /**----------------SentRecordLoadingViewHolder------------**/
    class SentRecordMessageLoadingViewHolder private constructor(val binding: ItemSentRecordLoadingBinding) :
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
            fun from(parent: ViewGroup): SentRecordMessageLoadingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentRecordLoadingBinding.inflate(layoutInflater, parent, false)
                return SentRecordMessageLoadingViewHolder(binding)
            }
        }


    }


    /**----------------SentRecordLoading------------**/
    class SentRecordMessageErrorViewHolder private constructor(val binding: ItemSentRecordErrorBinding) :
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
            fun from(parent: ViewGroup): SentRecordMessageErrorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSentRecordErrorBinding.inflate(layoutInflater, parent, false)
                return SentRecordMessageErrorViewHolder(binding)
            }
        }


    }



    /**----------------ReceivedRecordMessageViewHolder------------**/
    internal class ReceivedRecordMessageViewHolder (val binding: ItemReceivedRecordBinding) :
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

            fun from(parent: ViewGroup): ReceivedRecordMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemReceivedRecordBinding.inflate(layoutInflater, parent, false)
                return ReceivedRecordMessageViewHolder(binding)
            }

        }
    }





    class DiffCallbackMessages : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.created_at == newItem.created_at

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem

    }


}
