package com.example.petstagram_1.ui.vet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petstagram_1.databinding.FragmentEditVetProfileBinding
import com.example.petstagram_1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class EditVetProfileFragment : Fragment() {

    // --- IMPORTANT: REPLACE WITH YOUR ACTUAL CLOUDINARY CREDENTIALS ---
    private val CLOUD_NAME = "ddbzqh2vw"
    private val API_KEY = "799637424972498" // This is the key you provided
    private val API_SECRET = "lavGiN-fNbR1XZ9qx4dDejCurLs"
    // --------------------------------------------------------------------

    private var _binding: FragmentEditVetProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.editVetProfileImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditVetProfileBinding.inflate(inflater, container, false)
        // Initialize Cloudinary directly here
        initCloudinary()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = firebaseAuth.currentUser?.uid!!

        loadCurrentData()

        binding.editVetProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile(selectedImageUri!!)
            } else {
                saveProfileChanges(null)
            }
        }
    }

    /**
     * Initializes the Cloudinary MediaManager with your credentials.
     */
    private fun initCloudinary() {
        if (MediaManager.get() == null) {
            val config = mapOf(
                "cloud_name" to CLOUD_NAME,
                "api_key" to API_KEY,
                "api_secret" to API_SECRET
            )
            MediaManager.init(requireContext(), config)
        }
    }

    private fun loadCurrentData() {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val user = document.toObject<User>()
                    user?.let {
                        binding.editVetName.setText(it.username)
                        binding.editVetSpecialization.setText(it.specialization)
                        binding.editVetAbout.setText(it.about)
                        binding.editClinicName.setText(it.clinicName)
                        binding.editClinicAddress.setText(it.clinicAddress)
                        binding.editVetPhone.setText(it.phone)

                        if (!it.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(it.profileImageUrl).into(binding.editVetProfileImage)
                        }
                    }
                }
            }
            .addOnFailureListener {
                if(isAdded) {
                    Toast.makeText(context, "Failed to load profile data.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Handles the direct upload of the selected image to Cloudinary.
     */
    private fun uploadImageAndSaveProfile(imageUri: Uri) {
        Toast.makeText(context, "Uploading image...", Toast.LENGTH_LONG).show()
        binding.btnSaveProfile.isEnabled = false // Disable button during upload

        MediaManager.get().upload(imageUri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                // Upload started
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // You can show progress here if needed
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val imageUrl = resultData["secure_url"] as? String
                if (imageUrl != null) {
                    // Once the upload is successful, save the new URL to Firestore
                    saveProfileChanges(imageUrl)
                } else {
                    Toast.makeText(context, "Failed to get image URL.", Toast.LENGTH_SHORT).show()
                    binding.btnSaveProfile.isEnabled = true
                }
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                binding.btnSaveProfile.isEnabled = true
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                // Rescheduled
            }
        }).dispatch()
    }

    /**
     * Saves the text fields and optionally a new image URL to Firestore.
     */
    private fun saveProfileChanges(imageUrl: String?) {
        val userUpdates = mutableMapOf<String, Any?>(
            "username" to binding.editVetName.text.toString().trim(),
            "specialization" to binding.editVetSpecialization.text.toString().trim(),
            "about" to binding.editVetAbout.text.toString().trim(),
            "clinicName" to binding.editClinicName.text.toString().trim(),
            "clinicAddress" to binding.editClinicAddress.text.toString().trim(),
            "phone" to binding.editVetPhone.text.toString().trim()
        )

        if (imageUrl != null) {
            userUpdates["profileImageUrl"] = imageUrl
        }

        firestore.collection("users").document(currentUserId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                // Re-enable the button once the Firestore update is complete (success or failure)
                if(isAdded) {
                    binding.btnSaveProfile.isEnabled = true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
