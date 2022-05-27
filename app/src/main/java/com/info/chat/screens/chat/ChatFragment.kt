
package com.info.chat.screens.chat

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.info.chat.R
import com.info.chat.data.MyImage
import com.info.chat.data.User
import com.info.chat.data.message.*
import com.info.chat.databinding.ChatFragmentBinding
import com.info.chat.utils.*
import com.info.chat.utils.eventbus_events.PermissionEvent
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.stfalcon.imageviewer.StfalconImageViewer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class ChatFragment : Fragment() {


    companion object {
        const val TAG = "ChatFragment"
        const val SELECT_CHAT_IMAGE_REQUEST = 3
        const val CHOOSE_FILE_REQUEST = 4
    }


    private var recordStart = 0L
    private var recordDuration = 0L

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private var recorder: MediaRecorder? = null
    var isRecording = false //whether is recoding now or not
    var isRecord = true //whether it is text message or record
    private lateinit var loggedUser: User
    private lateinit var clickedUser: User

    private var messageList = mutableListOf<Message>()
    private lateinit var binding: ChatFragmentBinding

    private val chatAdapter by lazy { ChatAdapter(requireContext()) }


    private lateinit var viewModel: ChatViewModel
    private lateinit var viewModeldFactory: ChatViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment, container, false)

        //setup bottomsheet
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        //set record view
        handleRecord()


        //get logged user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(LOGGED_USER, null)
        loggedUser = gson.fromJson(json, User::class.java)

        //get receiver data from contacts fragment(NOTE:IF NAVIGATING FROM FCM-NOTIFICATION USER ONLY HAS id,username)
        clickedUser = gson.fromJson(arguments?.getString(CLICKED_USER), User::class.java)


        activity?.title = "Chatting with ${clickedUser.username}"

        //user viewmodel factory to pass ids on creation of view model
        if (clickedUser.uid != null) {
            viewModeldFactory = ChatViewModelFactory(loggedUser.uid, clickedUser.uid.toString())
            viewModel = ViewModelProviders.of(this, viewModeldFactory).get(ChatViewModel::class.java)
        }


        //Move layouts up when soft keyboard is shown
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        setObserves()
        setViews()


        return binding.root
    }

    private fun setAdapter(): ChatAdapter {

        /** onMessageClickListener **/
        chatAdapter.clickListener = object: ChatAdapter.MessageClickListener {
            override fun onMessageClick(position: Int, message: Message) {
                onMessageClicked(message, position)
            }
        }

        return chatAdapter
    }

    private fun onMessageClicked(message: Message, position: Int){
        when (message.type) {
            1.0 -> {  //if clicked item is image open in full screen with pinch to zoom

                binding.fullSizeImageView.visibility = View.VISIBLE

                StfalconImageViewer.Builder(
                    activity!!,
                    listOf(MyImage((message as ImageMessage).uri!!))){ imageView, myImage ->
                    Glide.with(activity!!)
                        .load(myImage.url)
                        .apply(RequestOptions().error(R.drawable.ic_broken_image_black_24dp))
                        .into(imageView)
                }
                    .withDismissListener { binding.fullSizeImageView.visibility = View.GONE }
                    .show()


            }
            //show dialog confirming user want to download file then proceed to download or cancel
            2.0 -> {
                //file message we should download
                val dialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }
                dialogBuilder?.setMessage("Do you want to download clicked file?")
                    ?.setPositiveButton(
                        "yes"
                    ) { _, _ ->
                        viewModel.downloadFile(requireActivity(),message)
                    }?.setNegativeButton("cancel", null)?.show()

            }
            3.0 -> {
                chatAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setViews() {
        binding.apply {
            recycler.adapter = setAdapter()

        }

    }

    private fun setObserves() {

        /** messages **/
        viewModel.messages.observe(viewLifecycleOwner) { mMessagesList ->
            if (!mMessagesList.isNullOrEmpty()){
                messageList = mMessagesList as MutableList<Message>
                ChatAdapter.messageList = mMessagesList
                Log.d(TAG,"messages: ${mMessagesList.size}")
                chatAdapter.submitList(mMessagesList)

                binding.recycler.scrollToPosition(mMessagesList.size - 1 )

            }

        }



        /** file uri **/
        viewModel.fileUri.observe(viewLifecycleOwner) { chatFileMap ->
            viewModel.sendMessage(
                FileMessage(
                    "",
                    loggedUser.uid,
                    Timestamp(Date()),
                    2.0,
                    clickedUser.uid,
                    loggedUser.username,
                    chatFileMap["fileName"].toString(),
                    chatFileMap["downloadUri"].toString()
                )
            )

        }


       /** image uri **/
        viewModel.imageUri.observe(viewLifecycleOwner) { uploadedChatImageUri ->
            viewModel.sendMessage(
                ImageMessage(
                    "",
                    loggedUser.uid,
                    Timestamp(Date()),
                    1.0,
                    clickedUser.uid,
                    loggedUser.username,
                    uploadedChatImageUri.toString()
                )
            )
        }


        /** snack bar **/
        viewModel.isSnackBarVisible.observe(viewLifecycleOwner){ info ->
            if (!info.isNullOrEmpty()){
                Snackbar.make(
                    binding.coordinator, info, Snackbar.LENGTH_LONG).setAction("Grant") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity!!.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.show()
            }

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        //send message on keyboard done click
        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            sendMessage()
            true
        }





        //handle click of bottomsheet items
        binding.attachment.sendPictureButton.setOnClickListener {
            selectFromGallery()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.attachment.sendFileButton.setOnClickListener {
            openFileChooser()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.attachment.hide.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        //show bottom sheet
        binding.attachmentImageView.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


        //observe when new record is uploaded
        viewModel.recordUri.observe(viewLifecycleOwner) { recordUri ->
            viewModel.sendMessage(
                RecordMessage(
                    "",
                    AuthUtil.getAuthId(),
                    Timestamp(Date()),
                    3.0,
                    clickedUser.uid,
                    loggedUser.username,
                    recordDuration.toString(),
                    recordUri.toString(),
                    null,
                    null
                )
            )
        }
    }


    private fun handleRecord() {


        //change fab icon depending on is text message empty or not
        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isRecord = if (s.isNullOrEmpty()) {
                    //empty text message
                    binding.recordFab.setImageResource(R.drawable.ic_mic_white_24dp)
                    true
                } else {
                    binding.recordFab.setImageResource(R.drawable.ic_right_arrow)
                    false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        binding.recordFab.setOnClickListener {
            if (isRecord) {
                //record message
                if (isRecording) {
                    //chnage size and color or button so user know its finished recording
                    val regainer = AnimatorInflater.loadAnimator(context, R.animator.regain_size) as AnimatorSet
                    regainer.setTarget(binding.recordFab)
                    regainer.start()
                    binding.recordFab.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#b39ddb"))
                    //stop recording and upload record
                    stopRecording()
                    showPlaceholderRecord()
                    viewModel.uploadRecord("${activity!!.externalCacheDir?.absolutePath}/audiorecord.3gp")
                    Toast.makeText(context, "Finished recording", Toast.LENGTH_SHORT).show()
                    isRecording = !isRecording

                } else {

                    Dexter.withActivity(activity)
                        .withPermission(Manifest.permission.RECORD_AUDIO)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                                //chnage size and color or button so user know its recording
                                val increaser = AnimatorInflater.loadAnimator(
                                    context,
                                    R.animator.increase_size
                                ) as AnimatorSet
                                increaser.setTarget(binding.recordFab)
                                increaser.start()
                                binding.recordFab.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#EE4B4B"))
                                //start recording
                                startRecording()
                                Toast.makeText(context, "Recording", Toast.LENGTH_SHORT).show()
                                isRecording = !isRecording
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permission: com.karumi.dexter.listener.PermissionRequest?,
                                token: PermissionToken?
                            ) {
                                token?.continuePermissionRequest()
                                //notify parent activity that permission denied to show toast for manual permission giving
                                viewModel.showSnackBar("Permission is needed for this feature to work")
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                                //notify parent activity that permission denied to show toast for manual permission giving
                                viewModel.showSnackBar("Permission is needed for this feature to work")
                            }
                        }).check()

                }

            } else {
                //text message
                sendMessage()
            }
        }


    }


    private fun sendMessage() {
        if (binding.messageEditText.text.isEmpty()) {
            Toast.makeText(context, getString(R.string.empty_message), Toast.LENGTH_LONG).show()
            return
        }
        viewModel.sendMessage(
            TextMessage(
                getMessageId(),
                loggedUser.uid,
                Timestamp(Date()),
                0.0,
                clickedUser.uid,
                loggedUser.username,
                binding.messageEditText.text.toString()
            )
        )

        binding.messageEditText.setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //select file result
        if (requestCode == CHOOSE_FILE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {
            val file = data.data
            if (file != null) {
                viewModel.uploadFile(file)
            }
            showPlaceholderFile(file)
        }

        //select picture result
        if (requestCode == SELECT_CHAT_IMAGE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {
            //show fake item with image in recycler until image is uploaded
            showPlaceholderPhoto(data.data)
            val image = data.data
            if (image != null){
                viewModel.uploadImage(image)
            }
        }
    }



    private fun openFileChooser() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "*/*"
        try {
            startActivityForResult(i, CHOOSE_FILE_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No suitable file manager was found on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPlaceholderPhoto(data: Uri?) {
        messageList.add(
            ImageMessage(
                getMessageId(),
                AuthUtil.getAuthId(),
                null,
                1.0,
                clickedUser.uid,
                loggedUser.username,
                data.toString()
            )
        )
        chatAdapter.submitList(messageList)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recycler.scrollToPosition(messageList.size - 1)
    }


    private fun showPlaceholderRecord() {
        //show fake item with progress bar while record uploads
        messageList.add(
            RecordMessage(
                null,
                AuthUtil.getAuthId(),
                null,
                8.0,
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
        chatAdapter.submitList(messageList)
        chatAdapter.notifyItemInserted(messageList.size )
        binding.recycler.scrollToPosition(messageList.size )
    }


    private fun showPlaceholderFile(data: Uri?) {
        messageList.add(
            FileMessage(
                getMessageId(),
                AuthUtil.getAuthId(),
                null,
                2.0,
                clickedUser.uid,
                loggedUser.username,
                data.toString(),
                data?.path.toString()
            )
        )
        chatAdapter.submitList(messageList)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recycler.scrollToPosition(messageList.size - 1)
    }

    private fun selectFromGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            SELECT_CHAT_IMAGE_REQUEST
        )
    }

//    private fun showSnackBar() {
//        Snackbar.make(
//            binding.coordinator,
//            "Permission is needed for this feature to work",
//            Snackbar.LENGTH_LONG
//        ).setAction(
//            "Grant", View.OnClickListener {
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                val uri = Uri.fromParts("package", activity!!.packageName, null)
//                intent.data = uri
//                startActivity(intent)
//            }
//        ).show()
//
//    }


    private fun startRecording() {

        //name of the file where record will be stored
        val fileName = "${activity!!.externalCacheDir?.absolutePath}/audiorecord.3gp"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()

            } catch (e: IOException) {
                Log.d(TAG,"onStartRecording: Error -> ${e.message}")
            }

            start()
            recordStart = Date().time
        }
    }

    private fun stopRecording() {

        recorder?.apply {
            stop()
            release()
            recorder = null
        }

        recordDuration = Date().time - recordStart

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecycleItemEvent(event: UpdateRecycleItemEvent) {
        chatAdapter.notifyItemChanged(event.adapterPosition)
    }
}

