package com.example.smartremainder

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smartremainder.databinding.ActivityProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var currentUser: UserProfile? = null
    private var userEmail: String? = null
    private var userDocId: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    uploadProfileImage(imageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        getUserInfoFromPrefs()
        setupToolbar()
        loadUserProfile()
        setupButtons()

        binding.ivEditProfileImage.setOnClickListener {
            openGallery()
        }
    }

    private fun getUserInfoFromPrefs() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userEmail = prefs.getString("email", null)
        userDocId = prefs.getString("user_doc_id", null)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        if (userEmail == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading profile image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // If we don’t already know the docId, fetch it once
        if (userDocId == null) {
            firestore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "User record not found!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    userDocId = docs.documents[0].id

                    // Save for later reuse
                    getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        .edit().putString("user_doc_id", userDocId).apply()

                    uploadImageToFirebase(imageUri, progressDialog)
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to fetch user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            uploadImageToFirebase(imageUri, progressDialog)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, progressDialog: ProgressDialog) {
        val docId = userDocId ?: return
        val storageRef = storage.reference.child("profile_images/$docId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        firestore.collection("users").document(docId)
                            .update("profileImage", uri.toString())
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                loadUserProfile()
                                Toast.makeText(this, "Profile image updated ✅", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this, "Failed to update Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to get image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserProfile() {
        if (userEmail == null) {
            Toast.makeText(this, "No user found. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    userDocId = doc.id
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("user_doc_id", userDocId).apply()

                    val name = doc.getString("name") ?: ""
                    val username = doc.getString("username") ?: name.replace(" ", "_")
                        .lowercase(Locale.getDefault())
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val profileImage = doc.getString("profileImage")

                    currentUser = UserProfile(0, name, username, email, phone, profileImage)
                    displayUserProfile(currentUser!!)
                } else {
                    Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserProfile(user: UserProfile) {
        with(binding) {
            tvUserName.text = user.name
            tvUsername.text = "@${user.username}"
            tvEmail.text = user.email
            tvPhone.text = "Phone: ${user.phone}"

            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(this@ProfileActivity)
                    .load(user.profileImage)
                    .circleCrop()
                    .into(ivProfilePicture)
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_user)
            }
        }
    }

    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener { openEditProfileDialog() }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun openEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        currentUser?.let { user ->
            etName.setText(user.name)
            etUsername.setText(user.username)
            etEmail.setText(user.email)
            etPhone.setText(user.phone)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                updateProfile(etName.text.toString(), etUsername.text.toString(), etEmail.text.toString(), etPhone.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(name: String, username: String, email: String, phone: String) {
        if (userDocId == null) return

        val updatedMap = mapOf(
            "name" to name,
            "username" to username,
            "email" to email,
            "phone" to phone
        )

        firestore.collection("users").document(userDocId!!)
            .update(updatedMap)
            .addOnSuccessListener {
                val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("email", email).apply()
                userEmail = email
                loadUserProfile()
                Toast.makeText(this, "Profile updated ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, Login::class.java))
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
