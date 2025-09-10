package com.example.petstagram_1.ui.vet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.petstagram_1.R // Make sure to import R
import com.example.petstagram_1.databinding.FragmentVetProfileBinding
import com.example.petstagram_1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class VetProfileFragment : Fragment() {

    private var _binding: FragmentVetProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVetProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadVetProfile()

        // Set a click listener on the floating edit button.
        binding.fabEditProfile.setOnClickListener {
            // Use the NavController to navigate using the action we defined in nav_graph.xml.
            findNavController().navigate(R.id.action_vetProfileFragment_to_editVetProfileFragment)
        }
    }

    private fun loadVetProfile() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Error: User not found.", Toast.LENGTH_LONG).show()
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val user = document.toObject<User>()
                    user?.let { populateUi(it) }
                }
            }
            .addOnFailureListener {
                if(isAdded) {
                    Toast.makeText(context, "Error fetching profile", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun populateUi(user: User) {
        binding.vetName.text = user.username ?: "Vet Name"
        binding.vetSpecialization.text = user.specialization ?: "Not specified"
        binding.vetAboutMe.text = user.about ?: "No bio available."
        binding.vetClinicName.text = user.clinicName ?: "Not specified"
        binding.vetClinicAddress.text = user.clinicAddress ?: "Not specified"
        binding.vetClinicPhone.text = user.phone ?: "Not specified"

        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(user.profileImageUrl).into(binding.vetProfileImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
