package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ForgetPassword : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var btnVerify: Button
    private lateinit var passwordSection: LinearLayout
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnToggleNewPassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    private lateinit var firestore: FirebaseFirestore
    private var verifiedDocId: String? = null
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        firestore = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUsername)
        btnVerify = findViewById(R.id.btnVerify)
        passwordSection = findViewById(R.id.passwordSection)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)

        // Step 1: Verify user by email and username
        btnVerify.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()

            if (email.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please enter both email and username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyUser(email, username)
        }

        // Toggle password visibility
        btnToggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(etNewPassword, btnToggleNewPassword, isNewPasswordVisible)
        }

        btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible)
        }

        // Step 2: Change Password
        btnChangePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter and confirm your new password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePassword(newPassword)
        }
    }

    // ✅ Verify user by email and username in Firestore
    private fun verifyUser(email: String, username: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    verifiedDocId = doc.id

                    Toast.makeText(this, "User verified! Enter new password.", Toast.LENGTH_SHORT).show()
                    passwordSection.visibility = View.VISIBLE
                    btnVerify.visibility = View.GONE
                    etEmail.isEnabled = false
                    etUsername.isEnabled = false
                } else {
                    Toast.makeText(this, "No user found with provided details", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ Update password in Firestore
    private fun updatePassword(newPassword: String) {
        val docId = verifiedDocId ?: run {
            Toast.makeText(this, "User not verified", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(docId)
            .update("password", newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update password: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Toggle password visibility
    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageButton, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye_off)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye)
        }
        editText.setSelection(editText.text.length)
    }

    // Redirect to login
    private fun redirectToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}
