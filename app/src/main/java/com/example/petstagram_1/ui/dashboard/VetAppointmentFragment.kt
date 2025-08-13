package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.FragmentVetAppointmentBinding
import com.example.petstagram_1.models.User
import com.google.firebase.firestore.FirebaseFirestore

class VetAppointmentFragment : Fragment() {

    private var _binding: FragmentVetAppointmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var vetAdapter: VetAdapter
    private lateinit var firestore: FirebaseFirestore
    private val vetList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVetAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()
        loadVetsFromFirestore()
    }

    private fun setupRecyclerView() {
        vetAdapter = VetAdapter(vetList)
        binding.vetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vetAdapter
        }
    }

    /**
     * Fetches users with the role "Veterinarian" from the Firestore database.
     */
    private fun loadVetsFromFirestore() {
        firestore.collection("users")
            .whereEqualTo("role", "Veterinarian") // Query to get only vets
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    vetList.clear() // Clear the list before adding new data
                    for (document in documents) {
                        val vet = document.toObject(User::class.java)
                        vetList.add(vet)
                    }
                    vetAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed
                }
            }
            .addOnFailureListener { exception ->
                Log.w("VetAppointmentFragment", "Error getting documents: ", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
