package com.example.smarter_speaker

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.speech.RecognizerIntent
import android.support.annotation.RequiresApi
import android.widget.Toast
import edu.cmu.pocketsphinx.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class RecognitionListenerImpl(val activity: Activity) : RecognitionListener {
    companion object {
        val REQ_CODE_SPEECH_INPUT = 100
    }

    private lateinit var recognizer: SpeechRecognizer
    private val KWS_SEARCH = "wakeup"

    private val KEYPHRASE = "hey cassie"

    private var olami: Olami? = null

    private val tts = TTS()

    private var player: Player? = null

    init {
        player = Player(activity)
        olami = Olami(activity)
    }

    interface RecognitionCallback {
        fun onSuccess()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, recognitionCallback: RecognitionCallback?) {

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val userCommand = result[0]
                    Toast.makeText(activity, userCommand, Toast.LENGTH_SHORT).show()
                    val url = olami?.getUrl(userCommand)
                    val idList = olami?.getTrackIds(url)

                    recognitionCallback?.onSuccess()
                    playMusic(idList)
                }

                Handler().postDelayed({
                    recognizer.startListening(KWS_SEARCH)
                }, 5000)
            }
        }
    }

    // 如果只有一首歌就只講歌名
    // 如果是清單要講第一首歌的歌名
    private fun generateMessage(idList: List<String>): String {
        return if (idList.size > 1) {
            val nameofFirstSong = idList[0]
            val texttospeech = "為您播放$nameofFirstSong"
            texttospeech
        } else {
            val nameofSong = idList.toString()
            val texttospeech = "為您播放$nameofSong"
            texttospeech
        }
    }

    private fun playMusic(idList: List<String>?) {

        val newIdList = idList?.drop(1) //把歌名弄掉
        if (newIdList != null && newIdList.isNotEmpty()) {
            val textOfSpeech = generateMessage(idList)
            tts.textToSpeechMp3(textOfSpeech)

            PlaySongActivity.openPlaySongActivity(activity, ArrayList(newIdList))
        }

    }

    fun onDestroy() {
        recognizer.cancel()
        recognizer.shutdown()
    }

    fun setup() {
        SetupTask().execute()
    }

    fun stop() {
        recognizer.stop()
    }

    inner class SetupTask : AsyncTask<Void, Void, Exception>() {

        override fun doInBackground(vararg params: Void): Exception? {
            try {
                val assets = Assets(activity)
                val assetDir = assets.syncAssets()
                setupRecognizer(assetDir)
            } catch (e: IOException) {
                return e
            }

            return null
        }

        /** 開始listening */
        override fun onPostExecute(result: Exception?) {
            if (result == null) {
                recognizer.startListening(KWS_SEARCH)
            }
        }
    }

    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(assetsDir, "en-us-ptm"))
                .setDictionary(File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold(1e-20f)
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .recognizer
        recognizer.addListener(this)

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE)
    }

    override fun onResult(hypothesis: Hypothesis?) {
        /*
        if (hypothesis == null)
            return

        val text = hypothesis.hypstr
        toast(text)
        */
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null)
            return

        recognizer.stop()
        val text = hypothesis.hypstr

        if (text == KEYPHRASE) {
            this.player?.playKCAudio()
        }
    }

    override fun onTimeout() {}

    override fun onBeginningOfSpeech() {}

    override fun onEndOfSpeech() {}

    override fun onError(p0: Exception?) {}
}