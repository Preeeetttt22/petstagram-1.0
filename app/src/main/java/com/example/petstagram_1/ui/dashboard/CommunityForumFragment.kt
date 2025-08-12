package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.FragmentCommunityForumBinding
import com.example.petstagram_1.models.Post

class CommunityForumFragment : Fragment() {

    private var _binding: FragmentCommunityForumBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Set a click listener for the Floating Action Button
        binding.fabAddPost.setOnClickListener {
            // For now, we'll just show a simple message.
            // Later, this will open a new screen to create a post.
            Toast.makeText(context, "Add new post clicked!", Toast.LENGTH_SHORT).show()
        }

        // In the future, we will add a function here to load posts from Firestore
        // loadPostsFromFirestore()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list for now
        postAdapter = PostAdapter(emptyList())
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
