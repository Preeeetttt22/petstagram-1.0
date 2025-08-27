package com.example.petstagram_1.ui.vet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.FragmentVetDashboardBinding
import com.example.petstagram_1.models.Appointment
import com.example.petstagram_1.models.ScheduleItem // Import the new data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class VetDashboardFragment : Fragment() {

    private var _binding: FragmentVetDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var upcomingAdapter: ScheduleAdapter // Use the imported adapter
    private val upcomingAppointmentsList = mutableListOf<ScheduleItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVetDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()

        binding.btnManageAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_vetDashboardFragment_to_myScheduleFragment)
        }

        loadDashboardData()
    }

    private fun setupRecyclerView() {
        upcomingAdapter = ScheduleAdapter(upcomingAppointmentsList)
        binding.recyclerViewUpcoming.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = upcomingAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDashboardData() {
        val currentVetId = firebaseAuth.currentUser?.uid ?: return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        firestore.collection("appointments")
            .whereEqualTo("vetId", currentVetId)
            .whereGreaterThanOrEqualTo("appointmentDate", startOfDay)
            .whereLessThanOrEqualTo("appointmentDate", endOfDay)
            .orderBy("appointmentDate", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded) return@addOnSuccessListener

                val todaysAppointments = documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java).copy(appointmentId = doc.id)
                }

                val totalToday = todaysAppointments.size
                val completedCount = todaysAppointments.count { it.status.equals("Completed", ignoreCase = true) }
                val uniquePatients = todaysAppointments.distinctBy { it.petId }.size

                binding.statAppointmentsCount.text = totalToday.toString()
                binding.statCompletedCount.text = completedCount.toString()
                binding.statPatientsCount.text = uniquePatients.toString()

                val upcoming = todaysAppointments
                    .filterNot { it.status.equals("Completed", ignoreCase = true) }
                    .take(3)

                if (upcoming.isNotEmpty()) {
                    fetchDetailsForUpcoming(upcoming)
                }
            }
            .addOnFailureListener { exception ->
                if(isAdded) {
                    Log.e("VetDashboard", "Error loading dashboard data", exception)
                }
            }
    }

    private fun fetchDetailsForUpcoming(appointments: List<Appointment>) {
        val tempScheduleItems = mutableListOf<ScheduleItem>()
        var processedCount = 0

        for (appointment in appointments) {
            fetchPetAndOwnerDetails(appointment) { petName, ownerName ->
                tempScheduleItems.add(ScheduleItem(appointment, petName, ownerName))
                processedCount++

                if (processedCount == appointments.size) {
                    upcomingAppointmentsList.clear()
                    upcomingAppointmentsList.addAll(tempScheduleItems.sortedBy { it.appointment.appointmentDate })
                    upcomingAdapter.notifyDataSetChanged()
                }
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
