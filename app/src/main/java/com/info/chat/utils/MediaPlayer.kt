package com.info.chat.utils

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.ProgressBar
import com.info.chat.R
import com.info.chat.data.message.RecordMessage
import java.io.IOException

object MediaPlayer {

   private var player = MediaPlayer()
   private lateinit var countDownTimer: CountDownTimer


    fun startPlaying(
        audioUri: String,
        adapterPosition: Int,
        recordMessage: RecordMessage,
        playPauseImage: ImageView,
        progressbar: ProgressBar
    ) {

        //update last clicked item to be reset
        positionDelegate = adapterPosition

        //show temporary loading while audio is downloaded
        playPauseImage.setImageResource(R.drawable.loading_animation)

        if (recordMessage.isPlaying == null || recordMessage.isPlaying == false) {

            stopPlaying()
            recordMessage.isPlaying = false

            player.apply {
                try {
                    setDataSource(audioUri)
                    prepareAsync()
                } catch (e: IOException) {
                    println("ChatFragment.startPlaying:prepare failed")
                }

                setOnPreparedListener {
                    //media downloaded and will play

                    recordMessage.isPlaying = true
                    //play the record
                    start()

                    //change image to stop and show progress of record
                    progressbar.max = player.duration
                    playPauseImage.setImageResource(R.drawable.ic_stop_black_24dp)

                    //count down timer to show record progess but on when record is playing
                    countDownTimer = object : CountDownTimer(player.duration.toLong(), 50) {
                        override fun onFinish() {

                            progressbar.progress = (player.duration)
                            playPauseImage.setImageResource(R.drawable.ic_play_arrow_black_24dp)

                        }

                        override fun onTick(millisUntilFinished: Long) {

                            progressbar.progress = (player.duration.minus(millisUntilFinished)).toInt()
                        }

                    }.start()
                }
            }

        } else {
            //stop the record
            playPauseImage.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            stopPlaying()
            recordMessage.isPlaying = false
            progressbar.progress = 0

        }


    }

    private fun stopPlaying() {
        if (::countDownTimer.isInitialized)
            countDownTimer.cancel()
        player.reset()
    }

}