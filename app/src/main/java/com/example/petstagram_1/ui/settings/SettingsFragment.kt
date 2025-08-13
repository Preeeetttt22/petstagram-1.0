package com.example.petstagram_1.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petstagram_1.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for the settings options
        binding.btnTheme.setOnClickListener {
            // TODO: Implement theme selection dialog
            Toast.makeText(context, "Theme selection coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnFeedback.setOnClickListener {
            // TODO: Implement feedback email intent
            Toast.makeText(context, "Feedback clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAbout.setOnClickListener {
            // TODO: Navigate to an 'About' screen or show a dialog
            Toast.makeText(context, "About App clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
