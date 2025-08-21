package com.example.petstagram_1.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemAppointmentBinding
import com.example.petstagram_1.models.Appointment
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private val appointments: MutableList<Appointment>,
    private val petMap: Map<String, String>, // Map of PetID to PetName
    private val onDeleteClicked: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position], petMap)
    }

    override fun getItemCount() = appointments.size

    class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding,
        private val onDeleteClicked: (Appointment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment, petMap: Map<String, String>) {
            binding.vetNameTextView.text = appointment.clinicName
            binding.reasonTextView.text = "Reason: ${appointment.reason}"
            val petName = petMap[appointment.petId] ?: "Unknown Pet"
            binding.petNameTextView.text = "Pet: $petName"

            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            binding.appointmentDateTextView.text = sdf.format(appointment.appointmentDate.toDate())

            // Set Status
            binding.statusTextView.text = appointment.status
            val statusColor = when (appointment.status) {
                "Scheduled" -> ContextCompat.getColor(binding.root.context, R.color.status_scheduled)
                "Completed" -> ContextCompat.getColor(binding.root.context, R.color.status_completed)
                "Cancelled" -> ContextCompat.getColor(binding.root.context, R.color.status_cancelled)
                else -> Color.GRAY
            }
            binding.statusTextView.setBackgroundColor(statusColor)


            binding.deleteAppointmentButton.setOnClickListener {
                onDeleteClicked(appointment)
            }
        }
    }
}
