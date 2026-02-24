package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class WelComePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wel_come_page)

        // Fetching button IDs from XML
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Navigate to SignUpActivity
        btnCreateAccount.setOnClickListener {
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
