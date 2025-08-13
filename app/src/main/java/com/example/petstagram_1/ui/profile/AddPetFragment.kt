package com.example.petstagram_1.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petstagram_1.databinding.FragmentAddPetBinding
import com.example.petstagram_1.models.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPetFragment : Fragment() {

    private var _binding: FragmentAddPetBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.petImagePreview.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.petImagePreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnSavePet.setOnClickListener {
            uploadImageToCloudinary()
        }
    }

    private fun uploadImageToCloudinary() {
        val petName = binding.etPetName.text.toString().trim()
        val petBreed = binding.etPetBreed.text.toString().trim()
        val petAge = binding.etPetAge.text.toString().trim()

        if (petName.isEmpty() || petBreed.isEmpty() || petAge.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(context, "Please select a profile picture for your pet", Toast.LENGTH_SHORT).show()
            return
        }

        MediaManager.get().upload(imageUri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show()
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val imageUrl = resultData["secure_url"].toString()
                savePetToFirestore(imageUrl, petName, petBreed, petAge)
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                Toast.makeText(context, "Image upload failed: ${error.description}", Toast.LENGTH_LONG).show()
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun savePetToFirestore(imageUrl: String, petName: String, petBreed: String, petAge: String) {
        val ownerId = firebaseAuth.currentUser?.uid ?: return

        val petId = firestore.collection("pets").document().id
        val newPet = Pet(
            petId = petId,
            ownerId = ownerId,
            name = petName,
            breed = petBreed,
            age = petAge.toInt(),
            profileImageUrl = imageUrl
        )

        firestore.collection("pets").document(petId).set(newPet)
            .addOnSuccessListener {
                Toast.makeText(context, "Pet added successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add pet: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
