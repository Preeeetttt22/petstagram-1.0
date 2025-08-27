package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemVetListBinding
import com.example.petstagram_1.models.User

/**
 * This is the correct adapter for displaying the list of veterinarians.
 * It takes a list of User objects and a function to handle the "Book" button click.
 */
class VetAdapter(
    private val vets: List<User>,
    private val onBookClick: (User) -> Unit // Lambda to handle book button clicks
) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        // Inflates the corrected item_vet.xml layout
        val binding = ItemVetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VetViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        holder.bind(vets[position])
    }

    override fun getItemCount(): Int = vets.size

    class VetViewHolder(
        private val binding: ItemVetListBinding,
        private val onBookClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vet: User) {
            // Uses the correct view IDs from item_vet.xml
            binding.vetName.text = vet.username ?: "Dr. Unknown"
            binding.clinicName.text = vet.clinicName ?: "Clinic not specified"

            // Loads the profile image, with a placeholder if the URL is empty
            if (!vet.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(vet.profileImageUrl)
                    .circleCrop()
                    .into(binding.vetProfileImage)
            } else {
                binding.vetProfileImage.setImageResource(R.drawable.ic_profile)
            }

            // Sets the click listener for the book button
            binding.btnBook.setOnClickListener {
                onBookClick(vet)
            }
        }
    }
}
