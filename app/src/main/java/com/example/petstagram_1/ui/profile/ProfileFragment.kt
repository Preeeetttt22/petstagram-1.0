package com.example.petstagram_1.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentProfileBinding
import com.example.petstagram_1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            // Handle user not being logged in if needed
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                // --- FIX: Check if the fragment is still attached to an activity ---
                // This prevents a crash if the user navigates away while data is loading.
                if (isAdded && document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.profileUsername.text = it.username
                        binding.profileEmail.text = it.email

                        if (!it.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(it.profileImageUrl)
                                .circleCrop()
                                .into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(context, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyPets.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_myPetsFragment)
        }

        // --- NEW CLICK LISTENER ---
        binding.btnMyAppointments.setOnClickListener {
            // We will create this action in the navigation graph next
            findNavController().navigate(R.id.action_nav_profile_to_myAppointmentsFragment)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
