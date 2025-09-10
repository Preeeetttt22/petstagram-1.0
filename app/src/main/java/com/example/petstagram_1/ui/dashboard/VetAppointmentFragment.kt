package com.example.petstagram_1.ui.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.DialogBookAppointmentBinding
import com.example.petstagram_1.databinding.FragmentVetAppointmentBinding
import com.example.petstagram_1.models.Appointment
import com.example.petstagram_1.models.Pet
import com.example.petstagram_1.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class VetAppointmentFragment : Fragment() {

    private var _binding: FragmentVetAppointmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var vetAdapter: VetAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private val vetList = mutableListOf<User>()
    private val userPetList = mutableListOf<Pet>()
    private val selectedDateTime = Calendar.getInstance()

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
        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadVetsFromFirestore()
        loadUserPets() // Pre-load the user's pets for the spinner
    }

    private fun setupRecyclerView() {
        vetAdapter = VetAdapter(vetList) { vet ->
            // This is the lambda function that gets called when a "Book" button is clicked
            showBookingDialog(vet)
        }
        binding.vetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vetAdapter
        }
    }

    private fun loadVetsFromFirestore() {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("users")
            .whereEqualTo("role", "Veterinarian")
            .get()
            .addOnSuccessListener { documents ->
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    if (documents != null && !documents.isEmpty) {
                        vetList.clear()
                        for (document in documents) {
                            val vet = document.toObject(User::class.java)
                            vetList.add(vet)
                        }
                        vetAdapter.notifyDataSetChanged()
                        binding.textNoVets.visibility = View.GONE
                    } else {
                        binding.textNoVets.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    binding.textNoVets.text = "Failed to load vets."
                    binding.textNoVets.visibility = View.VISIBLE
                    Log.w("VetAppointmentFragment", "Error getting documents: ", exception)
                }
            }
    }

    private fun loadUserPets() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("pets").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener { documents ->
                if (isAdded) {
                    userPetList.clear()
                    documents.forEach { doc ->
                        userPetList.add(doc.toObject(Pet::class.java))
                    }
                }
            }
    }

    private fun showBookingDialog(vet: User) {
        if (userPetList.isEmpty()) {
            Toast.makeText(context, "You must add a pet to your profile before booking.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogBookAppointmentBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        // Setup Spinner with pet names
        val petNames = userPetList.map { it.name }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, petNames)
        dialogBinding.spinnerSelectPet.adapter = spinnerAdapter

        // Setup Date Picker Button
        dialogBinding.btnSelectDate.setOnClickListener {
            showDatePicker(dialogBinding)
        }

        // Setup Time Picker Button
        dialogBinding.btnSelectTime.setOnClickListener {
            showTimePicker(dialogBinding)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Confirm Booking") { _, _ ->
                val selectedPetPosition = dialogBinding.spinnerSelectPet.selectedItemPosition
                val selectedPet = userPetList[selectedPetPosition]
                val reason = dialogBinding.editReason.text.toString().trim()

                if (reason.isEmpty()) {
                    Toast.makeText(context, "Please provide a reason for the visit.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                saveAppointment(vet, selectedPet, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(dialogBinding: DialogBookAppointmentBinding) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDateTime.set(Calendar.YEAR, year)
            selectedDateTime.set(Calendar.MONTH, month)
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            dialogBinding.btnSelectDate.text = sdf.format(selectedDateTime.time)
        }
        DatePickerDialog(requireContext(), dateSetListener,
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(dialogBinding: DialogBookAppointmentBinding) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedDateTime.set(Calendar.MINUTE, minute)
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            dialogBinding.btnSelectTime.text = sdf.format(selectedDateTime.time)
        }
        TimePickerDialog(requireContext(), timeSetListener,
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE), false).show()
    }

    private fun saveAppointment(vet: User, pet: Pet, reason: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "You must be logged in to book.", Toast.LENGTH_SHORT).show()
            return
        }

        val appointmentId = firestore.collection("appointments").document().id
        val newAppointment = Appointment(
            appointmentId = appointmentId,
            userId = currentUserId,
            vetId = vet.uid,
            petId = pet.petId,
            clinicName = vet.clinicName ?: "Clinic Not Specified",
            appointmentDate = Timestamp(selectedDateTime.time),
            reason = reason,
            status = "Scheduled"
        )

        firestore.collection("appointments").document(appointmentId)
            .set(newAppointment)
            .addOnSuccessListener {
                Toast.makeText(context, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to book appointment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
