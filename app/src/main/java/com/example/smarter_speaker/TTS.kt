package com.example.smarter_speaker

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.AsyncTask
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import java.nio.charset.Charset

class TTS {
    private var token: String

    init {
        token = getTTStoken().execute().get()
    }

    @SuppressLint("StaticFieldLeak")
    inner class getTTStoken : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg text: String): String {
            val urlOfToken = "https://westus.api.cognitive.microsoft.com/sts/v1.0/issueToken"

            // token
            val (request, response, result) = Fuel.post(urlOfToken).header(mapOf("Content-Length" to "0", "Ocp-Apim-Subscription-Key" to APIKey.TTSKey)).responseJson()
            return Json(String(response.data)).content
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetTTSAudio : AsyncTask<String, String, ByteArray>() {
        override fun doInBackground(vararg text: String): ByteArray {
            val speak = text[0]
            val api_url = "https://westus.tts.speech.microsoft.com/cognitiveservices/v1"
            val host = "westus.tts.speech.microsoft.com"
            val ContentType = "application/ssml+xml"
            val Connection = "Keep-Alive"
            val XMicrosoftOutputFormat = "raw-16khz-16bit-mono-pcm"
            val takemp3Header = mapOf("Authorization" to "Bearer $token", "Host" to host, "Content-Type" to ContentType, "Connection" to Connection, "X-Microsoft-OutputFormat" to XMicrosoftOutputFormat)
            println(takemp3Header)
            val body = "<speak version='1.0' xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang='en-US'>" +
                    "<voice name='Microsoft Server Speech Text to Speech Voice (zh-TW, Yating, Apollo)'>" +
                    "$speak</voice></speak>"

            val (request, response, result) = Fuel.post(api_url).header(takemp3Header).body(body, Charset.forName("UTF-8")).responseJson()
            return response.data
        }
    }

    fun textToSpeechMp3(text: String) {
        val audiobyte = GetTTSAudio().execute(text).get()

        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audiobyte.size,
                AudioTrack.MODE_STREAM
        )

        audioTrack.write(audiobyte, 0, audiobyte.size)
        audioTrack.play()

//        if (audioTrack == null) {
//            Log.d(TAG, "Stopping")
//            audioTrack.stop()
//            Log.d(TAG, "Releasing")
//            audioTrack.release()
//            Log.d(TAG, "Nulling")
//        }
    }
}