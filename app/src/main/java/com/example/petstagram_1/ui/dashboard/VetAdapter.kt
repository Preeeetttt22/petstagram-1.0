package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemVetListBinding
import com.example.petstagram_1.models.User

class VetAdapter(private val vetList: List<User>) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

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
            // Assuming the clinic name is stored in the email field for now
            vetClinicName.text = "Sunshine Pet Clinic" // Placeholder

            // Use Glide to load the profile image
            if (vet.profileImageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(vet.profileImageUrl)
                    .circleCrop()
                    .into(vetImage)
            } else {
                // Set a default placeholder if no image is available
                vetImage.setImageResource(R.drawable.ic_profile)
            }
        }
    }
}
