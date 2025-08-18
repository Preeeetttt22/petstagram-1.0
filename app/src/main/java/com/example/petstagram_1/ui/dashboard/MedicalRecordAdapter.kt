package com.example.petstagram_1.ui.medical

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemMedicalRecordBinding
import com.example.petstagram_1.models.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MedicalRecordAdapter(
    private val recordList: List<MedicalRecord>
) : RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(val binding: ItemMedicalRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemMedicalRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.binding.apply {
            recordTitle.text = record.title

            // Format the date for display
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            recordDate.text = sdf.format(record.date.toDate())

            // Set the click listener for the new options button
            btnOptions.setOnClickListener {
                showPopupMenu(holder.itemView.context, it, record)
            }
        }
    }

    private fun showPopupMenu(context: Context, view: View, record: MedicalRecord) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.record_options_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_download -> {
                    downloadRecord(context, record)
                    true
                }
                R.id.menu_delete -> {
                    showDeleteConfirmationDialog(context, record)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmationDialog(context: Context, record: MedicalRecord) {
        AlertDialog.Builder(context)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete '${record.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRecord(context, record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRecord(context: Context, record: MedicalRecord) {
        if (record.recordId.isEmpty()) {
            Toast.makeText(context, "Error: Cannot delete record. ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance().collection("medical_records").document(record.recordId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                // The list will update automatically because of the snapshot listener in the fragment.
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting record: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadRecord(context: Context, record: MedicalRecord) {
        if (record.documentUrl.isEmpty()) {
            Toast.makeText(context, "No file available to download.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(record.documentUrl)
            val request = DownloadManager.Request(uri)

            // Create a unique filename, for example, using the record title and pet ID
            val fileName = "${record.title.replace(" ", "_")}_${record.petId}.jpg"

            request.setTitle(record.title)
            request.setDescription("Downloading medical record...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Save the file to the public "Downloads" directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
