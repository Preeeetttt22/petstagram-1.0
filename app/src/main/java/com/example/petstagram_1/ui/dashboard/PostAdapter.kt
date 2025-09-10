package com.example.petstagram_1.ui.dashboard

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ItemPostBinding
import com.example.petstagram_1.models.Post
import com.example.petstagram_1.models.User
import com.example.petstagram_1.ui.comment.CommentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val postList: List<Post>,
    private val firestore: FirebaseFirestore
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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

            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            postTimestamp.text = sdf.format(post.timestamp.toDate())

            fetchAuthorDetails(post.authorId, holder)

            if (!post.imageUrl.isNullOrEmpty()) {
                postImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context).load(post.imageUrl).into(postImage)
            } else {
                postImage.visibility = View.GONE
            }

            // --- LIKE LOGIC ---
            updateLikeButton(holder, post)
            btnLike.setOnClickListener {
                handleLikeButtonClick(post, holder.itemView.context)
            }

            // --- COMMENT LOGIC ---
            btnComment.setOnClickListener {
                val context = holder.itemView.context
                // --- FIX: Check if postId is valid before starting the activity ---
                if (post.postId.isNotEmpty()) {
                    val intent = Intent(context, CommentActivity::class.java)
                    intent.putExtra("POST_ID", post.postId)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Cannot open comments, post ID is missing.", Toast.LENGTH_SHORT).show()
                }
            }

            // --- EDIT/DELETE LOGIC ---
            if (post.authorId == currentUserId) {
                postOptionsMenu.visibility = View.VISIBLE
                postOptionsMenu.setOnClickListener { view ->
                    showPopupMenu(view, post)
                }
            } else {
                postOptionsMenu.visibility = View.GONE
            }
        }
    }

    private fun fetchAuthorDetails(authorId: String, holder: PostViewHolder) {
        firestore.collection("users").document(authorId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val author = document.toObject(User::class.java)
                    holder.binding.authorName.text = author?.username
                    if (!author?.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(holder.itemView.context).load(author?.profileImageUrl).circleCrop().into(holder.binding.authorImage)
                    } else {
                        holder.binding.authorImage.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
    }

    private fun updateLikeButton(holder: PostViewHolder, post: Post) {
        val likesCount = post.likes.size
        holder.binding.likeCountText.text = likesCount.toString()

        if (currentUserId != null && post.likes.contains(currentUserId)) {
            holder.binding.btnLike.setIconResource(R.drawable.ic_like_filled)
            holder.binding.btnLike.iconTint = ContextCompat.getColorStateList(holder.itemView.context, R.color.red)
        } else {
            holder.binding.btnLike.setIconResource(R.drawable.ic_like)
            holder.binding.btnLike.iconTint = ContextCompat.getColorStateList(holder.itemView.context, R.color.grey)
        }
    }

    private fun handleLikeButtonClick(post: Post, context: Context) {
        if (currentUserId == null) {
            Toast.makeText(context, "You must be logged in to like posts", Toast.LENGTH_SHORT).show()
            return
        }

        if (post.postId.isEmpty()) {
            Toast.makeText(context, "Error: Cannot perform action. Post ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val postRef = firestore.collection("posts").document(post.postId)

        if (post.likes.contains(currentUserId)) {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId))
        } else {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId))
        }
    }

    private fun showPopupMenu(view: View, post: Post) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.post_options_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit_post -> {
                    Toast.makeText(view.context, "Edit coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_delete_post -> {
                    showDeleteConfirmationDialog(view.context, post)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmationDialog(context: Context, post: Post) {
        AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(post, context)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Post, context: Context) {
        if (post.postId.isEmpty()) {
            Toast.makeText(context, "Error: Cannot delete post. Post ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("posts").document(post.postId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
