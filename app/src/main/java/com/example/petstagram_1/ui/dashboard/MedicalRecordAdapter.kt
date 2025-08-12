package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.databinding.ItemMedicalRecordBinding
import com.example.petstagram_1.models.MedicalRecord
import java.text.SimpleDateFormat
import java.util.*

class MedicalRecordAdapter(private val recordList: List<MedicalRecord>) :
    RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(val binding: ItemMedicalRecordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding =
            ItemMedicalRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.binding.apply {
            recordTitle.text = record.title
            // Format the timestamp into a readable date string
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            recordDate.text = sdf.format(record.date.toDate())
        }
    }
}
