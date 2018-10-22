package com.example.smarter_speaker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }
        setContentView(R.layout.info)

        val heykcText = findViewById<TextView>(R.id.textview_hey_cassie)
        heykcText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val heykc = findViewById<TextView>(R.id.textview_hey_cassie)
        val fontStyle = ResourcesCompat.getFont(this, R.font.kalam)
        heykc.typeface = fontStyle
    }
}