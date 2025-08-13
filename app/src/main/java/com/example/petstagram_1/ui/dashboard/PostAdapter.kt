package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemPostBinding
import com.example.petstagram_1.models.Post
import com.example.petstagram_1.models.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val postList: List<Post>,
    private val firestore: FirebaseFirestore // Pass Firestore instance
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.binding.apply {
            postText.text = post.text

            // Format and display the timestamp
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            postTimestamp.text = sdf.format(post.timestamp.toDate())

            // Fetch author details from the 'users' collection
            firestore.collection("users").document(post.authorId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val author = document.toObject(User::class.java)
                        authorName.text = author?.username

                        // Load author's profile image
                        if (!author?.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(author?.profileImageUrl)
                                .circleCrop()
                                .into(authorImage)
                        } else {
                            authorImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                }

            // Handle the optional post image
            if (!post.imageUrl.isNullOrEmpty()) {
                postImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(post.imageUrl)
                    .into(postImage)
            } else {
                postImage.visibility = View.GONE
            }
        }
    }
}
