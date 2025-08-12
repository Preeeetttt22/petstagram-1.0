package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.FragmentVetAppointmentBinding
import com.example.petstagram_1.models.User // Assuming Vets are a type of User

class VetAppointmentFragment : Fragment() {

    private var _binding: FragmentVetAppointmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var vetAdapter: VetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVetAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        // In the future, we will add a function here to load vets from Firestore
        // loadVetsFromFirestore()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list for now
        vetAdapter = VetAdapter(emptyList())
        binding.vetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vetAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
