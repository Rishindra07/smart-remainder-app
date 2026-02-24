package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firestore = FirebaseFirestore.getInstance()
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        prefs.edit().putString("email", email).apply()

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, Signukt::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPassword::class.java))
        }
    }

    private fun togglePasswordVisibility() {
        if (etPassword.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        }
        etPassword.setSelection(etPassword.text.length)
    }
}
