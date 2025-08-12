package com.example.petstagram_1.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.databinding.ItemPostBinding
import com.example.petstagram_1.models.Post
import com.example.petstagram_1.models.User // We'll need this later to get author info

// The adapter takes a list of Post objects
class PostAdapter(private val postList: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // The ViewHolder holds the views for a single post item
    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    // This is called when the RecyclerView needs a new ViewHolder (a new item row)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    // This returns the total number of posts in our list
    override fun getItemCount(): Int {
        return postList.size
    }

    // This is where we bind the actual data from a Post object to the views in a ViewHolder
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.binding.apply {
            // Set the post text
            postText.text = post.text

            // --- Data we will fetch later ---
            // For now, we'll use placeholder text. Later, we'll fetch the real author's name.
            authorName.text = "Fetching user..."
            postTimestamp.text = "Just now" // We'll format the timestamp later

            // --- Handle the optional image ---
            if (post.imageUrl != null) {
                postImage.visibility = View.VISIBLE
                // Here we would use a library like Glide or Coil to load post.imageUrl into the postImage ImageView
            } else {
                postImage.visibility = View.GONE
            }
        }
    }
}
