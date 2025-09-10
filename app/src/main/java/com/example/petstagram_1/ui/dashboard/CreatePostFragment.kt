package com.example.petstagram_1.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentCreatePostBinding
import com.example.petstagram_1.models.Pet
import com.example.petstagram_1.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null
    private val userPets = mutableListOf<Pet>()
    private var selectedPet: Pet? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.postImagePreview.setImageURI(imageUri)
            binding.uploadPromptLayout.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserPets()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardImageUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnSharePost.setOnClickListener {
            uploadPostImage()
        }
    }

    private fun loadUserPets() {
        val uid = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("pets").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        val pet = document.toObject(Pet::class.java)
                        userPets.add(pet)
                    }
                    setupPetSpinner()
                } else {
                    Toast.makeText(context, "No pets found. Please add a pet first.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load pets.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupPetSpinner() {
        val petNames = userPets.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_custom, petNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.petSpinner.adapter = adapter

        binding.petSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPet = userPets[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedPet = null
            }
        }
    }

    private fun uploadPostImage() {
        val caption = binding.etCaption.text.toString().trim()

        if (imageUri == null || caption.isEmpty() || selectedPet == null) {
            Toast.makeText(context, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- START: Show Progress Bar ---
        binding.progressBarCreatePost.visibility = View.VISIBLE
        binding.btnSharePost.isEnabled = false // Disable button to prevent multiple clicks
        // --- END: Show Progress Bar ---

        MediaManager.get().upload(imageUri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                // Progress bar is already visible
            }
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val imageUrl = resultData["secure_url"].toString()
                savePostToFirestore(imageUrl, caption)
            }
            override fun onError(requestId: String, error: ErrorInfo) {
                Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                // --- START: Hide Progress Bar on Failure ---
                binding.progressBarCreatePost.visibility = View.GONE
                binding.btnSharePost.isEnabled = true // Re-enable button
                // --- END: Hide Progress Bar on Failure ---
            }
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun savePostToFirestore(imageUrl: String, caption: String) {
        val authorId = firebaseAuth.currentUser?.uid ?: return
        val petId = selectedPet?.petId ?: return

        val postId = firestore.collection("posts").document().id
        val newPost = Post(
            postId = postId,
            authorId = authorId,
            petId = petId,
            text = caption,
            imageUrl = imageUrl,
            timestamp = Timestamp.now()
        )

        firestore.collection("posts").document(postId).set(newPost)
            .addOnSuccessListener {
                Toast.makeText(context, "Post shared successfully!", Toast.LENGTH_SHORT).show()
                binding.progressBarCreatePost.visibility = View.GONE
                binding.btnSharePost.isEnabled = true
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to share post: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBarCreatePost.visibility = View.GONE
                binding.btnSharePost.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
