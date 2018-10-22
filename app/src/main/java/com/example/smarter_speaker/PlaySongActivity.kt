package com.example.smarter_speaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import java.net.URL


class PlaySongActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("SetJavaScriptEnabled", "NewApi")
    private lateinit var webView1: WebView

    companion object {
        fun openPlaySongActivity(context: Context, idList: ArrayList<String>) {
            val intent = Intent(context, PlaySongActivity::class.java)
            val bundle = Bundle()
            bundle.putStringArrayList("list", idList)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlaySongView().setContentView(this)
        webView1 = findViewById(R.id.webview)

        val idList = intent?.extras?.getStringArrayList("list")
        openKKBOX(ArrayList(idList))
    }


    private class PlaySongView : AnkoComponent<PlaySongActivity> {
        override fun createView(ui: AnkoContext<PlaySongActivity>) = with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                webView {
                    id = R.id.webview
                }.lparams(width = matchParent, height = matchParent)
            }
        }
    }


    private fun openKKBOX(idList: ArrayList<String>) {
        val link = "https://event.kkbox.com/content/song/${idList[0]}"
        doAsync {
            val url = URL(link)
            val document = Jsoup.parse(url, 3000)
            val webviewUrl = document.getElementsByClass("wpb-open-app").attr("href")
            uiThread {
                val intent = Intent.parseUri(webviewUrl, Intent.URI_INTENT_SCHEME)
                startActivity(intent)
                finish()
            }
        }
    }

}