package com.example.smarter_speaker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.speech.RecognizerIntent
import android.widget.Toast
import java.util.*

class Player(val activity: Activity) {

    fun playKCAudio() {
        val afd = activity.assets.openFd("text2audio.mp3")
        val player = MediaPlayer()
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        player.prepare()
        player.start()

        player.setOnCompletionListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請問有何吩咐？")

            try {
                activity.startActivityForResult(intent, RecognitionListenerImpl.REQ_CODE_SPEECH_INPUT)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(activity, "Sorry! Your device doesn't support speech input", Toast.LENGTH_SHORT).show()
            }
            player.release()
        }

    }

}