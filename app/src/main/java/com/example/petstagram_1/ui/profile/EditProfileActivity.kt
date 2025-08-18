package com.example.petstagram_1.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ActivityEditProfileBinding
import com.example.petstagram_1.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    // Activity Result Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            // Load the selected image into the ImageView
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(binding.editProfileImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        loadCurrentUserData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish() // Go back when the arrow is clicked
        }
    }

    private fun loadCurrentUserData() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    binding.editUsername.setText(it.username)
                    if (!it.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(it.profileImageUrl)
                            .circleCrop()
                            .into(binding.editProfileImage)
                    }
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            openGallery()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.passwordLayout.setEndIconOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun saveProfileChanges() {
        val newUsername = binding.editUsername.text.toString().trim()
        val uid = firebaseAuth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newUsername.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
            return
        } else {
            binding.usernameLayout.error = null
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false

        if (selectedImageUri != null) {
            // This now calls the correct Cloudinary upload function
            uploadImageToCloudinaryAndUpdateProfile(uid, newUsername)
        } else {
            // This updates only the username if no new photo was chosen
            updateProfile(uid, newUsername, null)
        }
    }

    // --- THIS IS THE CORRECTED UPLOAD LOGIC USING CLOUDINARY ---
    private fun uploadImageToCloudinaryAndUpdateProfile(uid: String, newUsername: String) {
        MediaManager.get().upload(selectedImageUri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                // Upload has started
            }
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // You can show progress here if you want
            }
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                // Image uploaded successfully, get the URL
                val imageUrl = resultData["secure_url"].toString()
                // Now, update the user's profile in Firestore with the new URL
                updateProfile(uid, newUsername, imageUrl)
            }
            override fun onError(requestId: String, error: ErrorInfo) {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(baseContext, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
            }
            override fun onReschedule(requestId: String, error: ErrorInfo) {
                // Handle reschedule
            }
        }).dispatch()
    }

    private fun updateProfile(uid: String, newUsername: String, imageUrl: String?) {
        val userUpdates = mutableMapOf<String, Any>()
        userUpdates["username"] = newUsername
        if (imageUrl != null) {
            userUpdates["profileImageUrl"] = imageUrl
        }

        firestore.collection("users").document(uid).update(userUpdates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // Go back to the profile screen
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(dialogView)

        val currentPasswordEt = dialogView.findViewById<EditText>(R.id.current_password)
        val newPasswordEt = dialogView.findViewById<EditText>(R.id.new_password)
        val confirmPasswordEt = dialogView.findViewById<EditText>(R.id.confirm_password)

        builder.setTitle("Change Password")
        builder.setPositiveButton("Change") { dialog, _ ->
            val currentPassword = currentPasswordEt.text.toString()
            val newPassword = newPasswordEt.text.toString()
            val confirmPassword = confirmPasswordEt.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            reauthenticateAndChangePassword(currentPassword, newPassword)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    private fun reauthenticateAndChangePassword(currentPassword: String, newPassword: String) {
        val user = firebaseAuth.currentUser
        val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
