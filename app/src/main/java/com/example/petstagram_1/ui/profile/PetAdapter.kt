package com.example.petstagram_1.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemPetListBinding
import com.example.petstagram_1.models.Pet

// --- UPDATED CONSTRUCTOR ---
// It now accepts a function to handle clicks.
class PetAdapter(
    private val petList: List<Pet>,
    private val onPetClicked: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

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

            if (pet.profileImageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(pet.profileImageUrl)
                    .circleCrop()
                    .into(petImage)
            } else {
                petImage.setImageResource(R.drawable.ic_pets)
            }

            // --- UPDATED CLICK LISTENER ---
            // It now calls the function that was passed into the constructor.
            holder.itemView.setOnClickListener {
                onPetClicked(pet)
            }
        }
    }
}
