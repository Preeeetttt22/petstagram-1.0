package com.example.petstagram_1.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.petstagram_1.databinding.FragmentMyAppointmentsBinding
import com.example.petstagram_1.models.Appointment
import com.example.petstagram_1.models.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyAppointmentsFragment : Fragment() {

    private var _binding: FragmentMyAppointmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var appointmentAdapter: AppointmentAdapter
    private val appointmentList = mutableListOf<Appointment>()
    private val petMap = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadPetsAndThenAppointments()
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(appointmentList, petMap) { appointment ->
            showDeleteConfirmationDialog(appointment)
        }
        binding.appointmentsRecyclerView.adapter = appointmentAdapter
    }

    private fun loadPetsAndThenAppointments() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("pets").whereEqualTo("ownerId", userId).get()
            .addOnSuccessListener { petDocuments ->
                petDocuments.forEach { doc ->
                    val pet = doc.toObject(Pet::class.java)
                    petMap[pet.petId] = pet.name
                }
                loadAppointments(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Could not load pet data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAppointments(userId: String) {
        Log.d("MyAppointments", "Querying appointments for userId: $userId")

        firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .orderBy("appointmentDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                appointmentList.clear()
                for (document in documents) {
                    val appointment = document.toObject(Appointment::class.java)
                    appointmentList.add(appointment)
                }

                if (appointmentList.isEmpty()) {
                    binding.noAppointmentsTextView.visibility = View.VISIBLE
                    binding.appointmentsRecyclerView.visibility = View.GONE
                } else {
                    binding.noAppointmentsTextView.visibility = View.GONE
                    binding.appointmentsRecyclerView.visibility = View.VISIBLE
                }

                appointmentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading appointments: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MyAppointments", "Error loading appointments", e)
            }
    }

    private fun showDeleteConfirmationDialog(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                deleteAppointment(appointment)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAppointment(appointment: Appointment) {
        firestore.collection("appointments").document(appointment.appointmentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                val position = appointmentList.indexOf(appointment)
                if (position != -1) {
                    appointmentList.removeAt(position)
                    appointmentAdapter.notifyItemRemoved(position)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to cancel appointment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
