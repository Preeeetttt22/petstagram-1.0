package com.example.petstagram_1.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemPetListBinding
import com.example.petstagram_1.models.Pet

class PetAdapter(private val petList: List<Pet>) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(val binding: ItemPetListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petList[position]
        holder.binding.apply {
            petName.text = pet.name
            petBreed.text = pet.breed

            // Use Glide to load the pet's profile image
            if (pet.profileImageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(pet.profileImageUrl)
                    .circleCrop()
                    .into(petImage)
            } else {
                // Set a default placeholder if no image is available
                petImage.setImageResource(R.drawable.ic_pets) // Using the pets icon as a placeholder
            }
        }
    }
}
