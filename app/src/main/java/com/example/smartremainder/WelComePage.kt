package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class WelComePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wel_come_page)

        // Fetching button IDs from XML
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnLogin = findViewById<TextView>(R.id.loginText)

        // Navigate to SignUpActivity
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, Signukt::class.java)
            startActivity(intent)
        }

        // Navigate to LoginActivity
        btnLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}
