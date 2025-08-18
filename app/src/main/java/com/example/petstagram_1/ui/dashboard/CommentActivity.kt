package com.example.petstagram_1.ui.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.ActivityCommentBinding
import com.example.petstagram_1.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        postId = intent.getStringExtra("POST_ID")

        setupToolbar()
        setupRecyclerView()
        setupCommentInput()

        // --- FIX: Check if postId is null OR EMPTY before loading ---
        if (!postId.isNullOrEmpty()) {
            loadComments()
        } else {
            Toast.makeText(this, "Error: Post ID not found.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(commentList, firestore)
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            adapter = commentAdapter
        }
    }

    private fun setupCommentInput() {
        binding.commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.postCommentButton.isEnabled = s.toString().trim().isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.postCommentButton.setOnClickListener {
            postComment()
        }
    }

    private fun loadComments() {
        // The check in onCreate already ensures postId is not null or empty here
        firestore.collection("posts").document(postId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading comments: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                commentList.clear()
                snapshots?.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

    private fun postComment() {
        val commentText = binding.commentEditText.text.toString().trim()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // --- FIX: Add another safety check here ---
        if (commentText.isEmpty() || currentUserId == null || postId.isNullOrEmpty()) {
            return
        }

        val commentId = UUID.randomUUID().toString()
        val newComment = Comment(
            commentId = commentId,
            postId = postId!!,
            authorId = currentUserId,
            text = commentText
        )

        firestore.collection("posts").document(postId!!)
            .collection("comments").document(commentId)
            .set(newComment)
            .addOnSuccessListener {
                binding.commentEditText.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
