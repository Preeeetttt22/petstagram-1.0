package com.example.petstagram_1.ui.dashboard

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
import androidx.navigation.fragment.navArgs
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petstagram_1.databinding.FragmentAddMedicalRecordBinding
import com.example.petstagram_1.models.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddMedicalRecordFragment : Fragment() {

    private var _binding: FragmentAddMedicalRecordBinding? = null
    private val binding get() = _binding!!

    // Safely get the petId passed from the previous screen
    private val args: AddMedicalRecordFragmentArgs by navArgs()
    private var selectedFileUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore

    // Launcher to handle the result from the file picker
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedFileUri = result.data?.data
            binding.imagePreview.setImageURI(selectedFileUri)
            binding.imagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddMedicalRecordBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectDocument.setOnClickListener {
            openFilePicker()
        }

        binding.btnSaveRecord.setOnClickListener {
            saveRecord()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Allows picking any image file. Change to "*/*" for any file type.
        pickFileLauncher.launch(intent)
    }

    private fun saveRecord() {
        val title = binding.editRecordTitle.text.toString().trim()
        val petId = args.petId

        if (title.isEmpty()) {
            binding.titleLayout.error = "Title cannot be empty"
            return
        }
        if (selectedFileUri == null) {
            Toast.makeText(context, "Please select a document or photo", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveRecord.isEnabled = false

        uploadFileToCloudinary(title, petId)
    }

    private fun uploadFileToCloudinary(title: String, petId: String) {
        MediaManager.get().upload(selectedFileUri).callback(object : UploadCallback {
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val documentUrl = resultData["secure_url"].toString()
                saveRecordToFirestore(title, petId, documentUrl)
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveRecord.isEnabled = true
                Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
            }

            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun saveRecordToFirestore(title: String, petId: String, documentUrl: String) {
        val recordId = UUID.randomUUID().toString()
        val newRecord = MedicalRecord(
            recordId = recordId,
            petId = petId,
            title = title,
            documentUrl = documentUrl,
            date = com.google.firebase.Timestamp.now()
        )

        firestore.collection("medical_records").document(recordId)
            .set(newRecord)
            .addOnSuccessListener {
                Toast.makeText(context, "Record saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Go back to the records list
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSaveRecord.isEnabled = true
                Toast.makeText(context, "Failed to save record: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
