package com.example.smartremainder

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
