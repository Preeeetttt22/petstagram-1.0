package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentMedicalRecordsBinding
import com.example.petstagram_1.models.MedicalRecord
import com.example.petstagram_1.models.Pet
import com.example.petstagram_1.ui.medical.MedicalRecordAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MedicalRecordFragment : Fragment() {

    private var _binding: FragmentMedicalRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recordAdapter: MedicalRecordAdapter

    // Use navArgs to safely get arguments passed from the navigation graph
    private val args: MedicalRecordFragmentArgs by navArgs()

    private val petList = mutableListOf<Pet>()
    private val recordList = mutableListOf<MedicalRecord>()
    private var selectedPet: Pet? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMedicalRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Set the initial title. If a petName was passed from another screen, use it.
        if (args.petName != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "${args.petName}'s Records"
        }

        setupRecyclerView()
        setupSpinner()
        loadUserPets()

        binding.fabAddRecord.setOnClickListener {
            selectedPet?.let { pet ->
                // Navigate to the "Add Record" screen, passing the selected pet's ID
                val action = MedicalRecordFragmentDirections.actionMedicalRecordFragmentToAddMedicalRecordFragment(pet.petId)
                findNavController().navigate(action)
            } ?: run {
                // Show a message if no pet is selected from the dropdown
                Toast.makeText(context, "Please select a pet first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        recordAdapter = MedicalRecordAdapter(recordList)
        binding.recordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordAdapter
        }
    }

    private fun setupSpinner() {
        binding.spinnerSelectPet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < petList.size) {
                    selectedPet = petList[position]
                    // When a pet is selected, update the toolbar title and load its records.
                    (activity as? AppCompatActivity)?.supportActionBar?.title = "${selectedPet?.name}'s Records"
                    selectedPet?.let { loadMedicalRecords(it.petId) }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                (activity as? AppCompatActivity)?.supportActionBar?.title = "Medical Records"
                recordList.clear()
                recordAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadUserPets() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("pets").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener { documents ->
                petList.clear()
                documents.forEach { doc ->
                    petList.add(doc.toObject(Pet::class.java))
                }
                val petNames = petList.map { it.name }
                val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, petNames)
                binding.spinnerSelectPet.adapter = spinnerAdapter

                // If a petId was passed from another screen, find and pre-select it in the spinner.
                if (args.petId != null) {
                    val petIndex = petList.indexOfFirst { it.petId == args.petId }
                    if (petIndex != -1) {
                        binding.spinnerSelectPet.setSelection(petIndex)
                    }
                }
            }
    }

    private fun loadMedicalRecords(petId: String) {
        if (petId.isEmpty()) return

        firestore.collection("medical_records").whereEqualTo("petId", petId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Error loading records: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                recordList.clear()
                snapshots?.forEach { doc ->
                    recordList.add(doc.toObject(MedicalRecord::class.java))
                }
                recordAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
