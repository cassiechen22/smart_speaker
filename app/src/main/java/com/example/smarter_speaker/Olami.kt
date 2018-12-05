package com.example.smarter_speaker

import android.app.Activity
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import java.math.BigInteger
import java.security.MessageDigest

class Olami(activity: Activity) {

    private var token: String
    private var tts: TTS
    private var player: Player? = null

    init {
        player = Player(activity)
        tts = TTS(activity)
        token = tts.getTTStoken().execute().get()
    }

    // Get request URL
    fun getUrl(userCommand: String): String {
        val getUrl = "https://tw.olami.ai/cloudservice/api?"
        val api = "nli"
        val timestamp = (System.currentTimeMillis()).toString()
        val sign = "${APIKey.olamiSecret}api=${api}apikey=${APIKey.olamiKey}timestamp=$timestamp${APIKey.olamiSecret}".toByteArray()
        // 加密
        val methodOfMd5 = MessageDigest.getInstance("MD5")
        methodOfMd5.update(sign, 0, sign.size)
        val signOfMd5 = BigInteger(1, methodOfMd5.digest()).toString(16)
        val rq = "'data':{'input_type':0,'text':'$userCommand'},'data_type':'stt'"
        val url = getUrl + "apikey=${APIKey.olamiKey}&api=$api&timestamp=$timestamp&sign=$signOfMd5&rq={$rq}"
        return url
    }

    // API request
    inner class GetOlamiData : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg urls: String): String {
            val (request, response, result) = Fuel.get(urls[0]).responseJson()
            println(response.data)
            return String(response.data)
        }
    }

    // process for Result and get ID List
    fun getTrackIds(url: String?): List<String>? {
        var idList: List<String>? = null
        for (i in 0 until 5) {
            idList = getOlamiResult(url)
            if (idList.isNotEmpty()) {
                break
            }
        }
        if (idList?.isEmpty() == true) {
            noResult()
        }
        return idList
    }

    private fun getOlamiResult(url: String?): List<String> {
        try {
            val resultJson = GetOlamiData()?.execute(url)?.get()

            val dataJson = Json(resultJson!!)
            val jsonobj = dataJson.obj()
            val objData = jsonobj.getJSONObject("data")
            val objData_nli = objData.getJSONArray("nli")
            val objData_nliArray = objData_nli.getJSONObject(0)

            val result = objData_nliArray.getJSONObject("desc_obj").getString("result")
            if (result.contains("馬上為你播放")) {
                Log.e("result", result)
                val dataObj = objData_nliArray.getJSONArray("data_obj")
                // 如果是清單 第一個就先放歌名
                val idsList = ArrayList<String>()
                val title = dataObj.getJSONObject(0).getString("title")
                val artist = dataObj.getJSONObject(0).getString("artist")
                val firstSongName = artist + title
                idsList.add(firstSongName)

                // 其他存id
                for (i in 0 until dataObj.length()) {
                    val id = dataObj.getJSONObject(i).getString("id")
                    idsList.add(id)
                }

                return idsList

            } else {
                return emptyList()
            }

        } catch (e: Exception) {
            return emptyList()
        }
    }


    private fun noResult() {
        Log.e("token", token)
        Log.e("Olami", "No result.")
        val none = "你說的我還不懂"
        tts.textToSpeechMp3(none)

        val handler = Handler()
        handler.postDelayed({
            player?.playKCAudio()
        }, 4500)
    }


}