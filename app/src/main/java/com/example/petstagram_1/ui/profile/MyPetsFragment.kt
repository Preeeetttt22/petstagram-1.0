package com.example.petstagram_1.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentMyPetsBinding
import com.example.petstagram_1.models.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPetsFragment : Fragment() {

    private var _binding: FragmentMyPetsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var petAdapter: PetAdapter
    private val petList = mutableListOf<Pet>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadUserPets()

        binding.fabAddPet.setOnClickListener {
            findNavController().navigate(R.id.action_myPetsFragment_to_addPetFragment)
        }
    }

    private fun setupRecyclerView() {
        petAdapter = PetAdapter(petList)
        binding.petsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = petAdapter
        }
    }

    private fun loadUserPets() {
        val uid = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("pets").whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                petList.clear()
                if (snapshots != null) {
                    for (document in snapshots) {
                        val pet = document.toObject(Pet::class.java)
                        petList.add(pet)
                    }
                }
                petAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
