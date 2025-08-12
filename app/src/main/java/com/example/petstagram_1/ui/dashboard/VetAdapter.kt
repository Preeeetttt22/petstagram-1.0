package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.databinding.ItemVetListBinding
import com.example.petstagram_1.models.User

// This adapter takes a list of Users (who are vets)
class VetAdapter(private val vetList: List<User>) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

    // This class holds the views for a single item in the list
    inner class VetViewHolder(val binding: ItemVetListBinding) : RecyclerView.ViewHolder(binding.root)

    // This function is called when a new view holder is needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        val binding = ItemVetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VetViewHolder(binding)
    }

    // This function returns the total number of items in the list
    override fun getItemCount(): Int {
        return vetList.size
    }

    // This function is called to display the data at a specific position
    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        val vet = vetList[position]
        holder.binding.apply {
            // Here we bind the data from the 'vet' object to the views
            vetName.text = vet.username
            // In a real app, you would have a clinic name field in your User model
            vetClinicName.text = "Sample Pet Clinic"
            // We would also load the vet's image here using a library like Glide or Coil
        }
    }
}
