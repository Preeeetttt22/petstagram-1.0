package com.example.petstagram_1.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            // --- NEW: Launch email intent for feedback ---
            sendFeedbackEmail()
        }

        binding.btnAbout.setOnClickListener {
            // --- NEW: Show a dialog with app information ---
            showAboutDialog()
        }
    }

    /**
     * Creates and launches an email intent for sending feedback.
     */
    private fun sendFeedbackEmail() {
        val recipientEmail = "feedback@petstagram.com" // Your feedback email address
        val subject = "Feedback for Petstagram App"
        val body = """
            
            --------------------
            Device Information:
            Model: ${Build.MODEL}
            OS Version: ${Build.VERSION.RELEASE}
            App Version: ${getAppVersionName()}
            --------------------
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        // Check if there's an email app to handle the intent
        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays a simple AlertDialog with information about the app.
     */
    private fun showAboutDialog() {
        val appVersion = getAppVersionName()
        AlertDialog.Builder(requireContext())
            .setTitle("About Petstagram")
            .setMessage("Version: $appVersion\n\nPetstagram is your one-stop app for all your pet's needs. Connect with a community of pet lovers, manage your pet's health, and book appointments with trusted veterinarians.")
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Helper function to get the current version name of the app.
     * @return The app's version name as a String.
     */
    private fun getAppVersionName(): String {
        return try {
            val packageInfo = context?.packageManager?.getPackageInfo(context!!.packageName, 0)
            packageInfo?.versionName ?: "N/A"
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
