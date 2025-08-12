package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // --- THE FIX IS HERE: Uncommented the navigation lines ---

        binding.btnFindVet.setOnClickListener {
            findNavController().navigate(R.id.action_nav_dashboard_to_nav_vet_appointment)
        }

        binding.btnPost.setOnClickListener {
            findNavController().navigate(R.id.action_nav_dashboard_to_nav_community_forum)
        }

        binding.btnAiAssistant.setOnClickListener {
            findNavController().navigate(R.id.action_nav_dashboard_to_nav_ai_assistant)
        }

        binding.btnMedicalRecord.setOnClickListener {
            findNavController().navigate(R.id.action_nav_dashboard_to_nav_medical_record)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
