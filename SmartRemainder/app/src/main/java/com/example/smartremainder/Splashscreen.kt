package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splashscreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        // Delay for 2.5 seconds, then open Login screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, WelComePage::class.java)
            startActivity(intent)
            finish()
        }, 2500) // 2500 ms = 2.5 seconds
    }
}
