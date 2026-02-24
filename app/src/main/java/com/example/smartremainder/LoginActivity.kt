
package com.example.smartremainder

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etPassword = findViewById(R.id.etPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (etPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        } else {
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        }
        etPassword.setSelection(etPassword.text.length)
    }
}
