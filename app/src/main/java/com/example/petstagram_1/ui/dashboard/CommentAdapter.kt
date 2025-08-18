package com.example.petstagram_1.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemCommentBinding
import com.example.petstagram_1.models.Comment
import com.example.petstagram_1.models.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val commentList: List<Comment>,
    private val firestore: FirebaseFirestore
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.binding.apply {
            commentText.text = comment.text

            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            commentTimestamp.text = sdf.format(comment.timestamp.toDate())

            fetchAuthorDetails(comment.authorId, holder)
        }
    }

    private fun fetchAuthorDetails(authorId: String, holder: CommentViewHolder) {
        firestore.collection("users").document(authorId).get()
            .addOnSuccessListener { document ->
                // Convert the document to a User object. This can be null.
                val author = document.toObject(User::class.java)

                // Use a null-safe 'let' block. The code inside only runs if 'author' is not null.
                author?.let { user ->
                    holder.binding.commentAuthorName.text = user.username

                    // Load profile image or set default
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(holder.itemView.context)
                            .load(user.profileImageUrl)
                            .circleCrop()
                            .into(holder.binding.commentAuthorImage)
                    } else {
                        holder.binding.commentAuthorImage.setImageResource(R.drawable.ic_profile)
                    }
                } ?: run {
                    // This block runs if author is null (e.g., user deleted their account)
                    holder.binding.commentAuthorName.text = "Unknown User"
                    holder.binding.commentAuthorImage.setImageResource(R.drawable.ic_profile)
                }
            }
            .addOnFailureListener {
                // Handle cases where fetching the user fails
                holder.binding.commentAuthorName.text = "Unknown User"
                holder.binding.commentAuthorImage.setImageResource(R.drawable.ic_profile)
            }
    }
}
