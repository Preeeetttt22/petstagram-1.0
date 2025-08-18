package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemVetListBinding
import com.example.petstagram_1.models.User

// --- UPDATED CONSTRUCTOR ---
// It now accepts a function that will be called when the "Book" button is clicked.
class VetAdapter(
    private val vetList: List<User>,
    private val onBookClicked: (User) -> Unit
) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

    inner class VetViewHolder(val binding: ItemVetListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        val binding = ItemVetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VetViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return vetList.size
    }

    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        val vet = vetList[position]
        holder.binding.apply {
            vetName.text = vet.username
            vetClinicName.text = "Sunshine Pet Clinic" // Placeholder

            if (vet.profileImageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(vet.profileImageUrl)
                    .circleCrop()
                    .into(vetImage)
            } else {
                vetImage.setImageResource(R.drawable.ic_profile)
            }

            // --- UPDATED CLICK LISTENER ---
            btnBookAppointment.setOnClickListener {
                onBookClicked(vet) // Pass the selected vet back to the fragment
            }
        }
    }
}
