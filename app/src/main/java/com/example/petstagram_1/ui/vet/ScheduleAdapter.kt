package com.example.petstagram_1.ui.vet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemVetAppointmentBinding
import com.example.petstagram_1.models.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * This adapter can be reused by any fragment that needs to display a list of ScheduleItems.
 */
class ScheduleAdapter(private val scheduleItems: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemVetAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleItems[position])
    }

    override fun getItemCount(): Int = scheduleItems.size

    class ScheduleViewHolder(private val binding: ItemVetAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scheduleItem: ScheduleItem) {
            val appointment = scheduleItem.appointment
            binding.petName.text = scheduleItem.petName
            binding.ownerName.text = "Owner: ${scheduleItem.ownerName}"
            binding.appointmentReason.text = "Reason: ${appointment.reason}"

            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            binding.appointmentTime.text = appointment.appointmentDate.toDate().let { sdf.format(it) }

            binding.appointmentStatus.text = appointment.status
            setStatusBackground(binding.appointmentStatus, appointment.status)
        }

        private fun setStatusBackground(textView: TextView, status: String) {
            val context = textView.context
            val backgroundResId = when (status.lowercase(Locale.ROOT)) {
                "confirmed" -> R.drawable.status_background_confirmed
                "completed" -> R.drawable.status_background_completed
                else -> R.drawable.status_background_pending
            }
            textView.background = ContextCompat.getDrawable(context, backgroundResId)
        }
    }
}
