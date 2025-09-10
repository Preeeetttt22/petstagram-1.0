package com.example.petstagram_1.ui.vet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentMyScheduleBinding
import com.example.petstagram_1.models.Appointment
import com.example.petstagram_1.models.ScheduleItem // Import the new data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class MyScheduleFragment : Fragment() {

    private var _binding: FragmentMyScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var scheduleAdapter: ScheduleAdapter // Use the imported adapter

    private val scheduleItemList = mutableListOf<ScheduleItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadAppointments()
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(scheduleItemList)
        binding.recyclerViewSchedule.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
        }
    }

    private fun loadAppointments() {
        binding.progressBar.visibility = View.VISIBLE
        val currentVetId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("appointments")
            .whereEqualTo("vetId", currentVetId)
            .orderBy("appointmentDate", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded) return@addOnSuccessListener

                binding.progressBar.visibility = View.GONE
                if (documents.isEmpty) {
                    binding.textNoAppointments.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val tempScheduleItems = mutableListOf<ScheduleItem>()
                val totalAppointments = documents.size()
                var processedCount = 0

                for (document in documents) {
                    val appointment = document.toObject(Appointment::class.java).copy(appointmentId = document.id)

                    fetchPetAndOwnerDetails(appointment) { petName, ownerName ->
                        tempScheduleItems.add(ScheduleItem(appointment, petName, ownerName))
                        processedCount++

                        if (processedCount == totalAppointments) {
                            scheduleItemList.clear()
                            scheduleItemList.addAll(tempScheduleItems.sortedBy { it.appointment.appointmentDate })
                            scheduleAdapter.notifyDataSetChanged()
                            binding.textNoAppointments.visibility = View.GONE
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    binding.textNoAppointments.text = "Failed to load appointments."
                    binding.textNoAppointments.visibility = View.VISIBLE
                    Log.e("MyScheduleFragment", "Error loading appointments", exception)
                }
            }
    }

    private fun fetchPetAndOwnerDetails(appointment: Appointment, onComplete: (petName: String, ownerName: String) -> Unit) {
        var petName = "Unknown Pet"
        var ownerName = "Unknown Owner"

        firestore.collection("pets").document(appointment.petId).get()
            .addOnSuccessListener { petDoc ->
                if (petDoc != null && petDoc.exists()) {
                    petName = petDoc.getString("name") ?: "Unknown Pet"
                }

                firestore.collection("users").document(appointment.userId).get()
                    .addOnSuccessListener { userDoc ->
                        if (userDoc != null && userDoc.exists()) {
                            ownerName = userDoc.getString("username") ?: "Unknown Owner"
                        }
                        onComplete(petName, ownerName)
                    }
                    .addOnFailureListener { onComplete(petName, ownerName) }
            }
            .addOnFailureListener { onComplete(petName, ownerName) }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
