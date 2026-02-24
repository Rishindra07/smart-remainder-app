package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Signukt : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signukt)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbTerms = findViewById(R.id.cbTerms)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, btnTogglePassword)
        }

        btnToggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword)
        }

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "password" to password
                        )

                        firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, Login::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton) {
        if (editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye)
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(editText.text.length)
    }
}
