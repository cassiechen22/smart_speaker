package com.example.smarter_speaker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*


@Suppress("DEPRECATION")

open class MainActivity : AppCompatActivity() {

    /* Used to handle permission request */
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1

    private var olami: Olami? = null

    var token: String
    val tts = TTS()
    var player: Player? = null
    var loadingView: ProgressBar? = null

    init {
        token = tts.getTTStoken().execute().get()
    }

    private var recognitionListenerImpl: RecognitionListenerImpl? = null


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting of UI
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }
        setContentView(R.layout.homepage)

        loadingView = findViewById(R.id.loading_view)
        val heykc = findViewById<TextView>(R.id.textview_hey_cassie)
        val fontStyle = ResourcesCompat.getFont(this, R.font.kalam)
        heykc.typeface = fontStyle

        // Permission of Record
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
        }

        // Check internet
        val internet = isNetworkConnected()
        if (!internet) {
            Toast.makeText(this, "Please open Cellular or Wi-fi to use the app", Toast.LENGTH_SHORT).show()
        } else {
            recognitionListenerImpl = RecognitionListenerImpl(this)
            olami = Olami(this)

            player = Player(this)
            val mic = findViewById<ImageView>(R.id.mic)
            mic.setOnClickListener {
                recognitionListenerImpl?.stop()
                player?.playKCAudio()
            }

            recognitionListenerImpl?.setup()

        }
    }

    override fun onResume() {
        super.onResume()
        loadingView?.visibility = View.GONE
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recognitionListenerImpl?.setup()
            } else {
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loadingView?.visibility = View.VISIBLE
        recognitionListenerImpl?.onActivityResult(requestCode, resultCode, data, object : RecognitionListenerImpl.RecognitionCallback {
            override fun onSuccess() {
                loadingView?.visibility = View.GONE
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        recognitionListenerImpl?.onDestroy()
    }

}

