package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.R // Make sure this import is present
import com.example.petstagram_1.databinding.FragmentCommunityForumBinding
import com.example.petstagram_1.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommunityForumFragment : Fragment() {

    private var _binding: FragmentCommunityForumBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter
    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()

        // UPDATED THIS CLICK LISTENER
        binding.fabAddPost.setOnClickListener {
            // This line will navigate to the create post screen
            findNavController().navigate(R.id.action_nav_community_forum_to_createPostFragment)
        }

        loadPostsFromFirestore()
    }

    private fun setupRecyclerView() {
        // Pass the firestore instance to the adapter
        postAdapter = PostAdapter(postList, firestore)
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    /**
     * Fetches all posts from the 'posts' collection, ordered by timestamp.
     */
    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest posts first
            .addSnapshotListener { snapshots, e -> // Changed to addSnapshotListener for real-time updates
                if (e != null) {
                    Log.w("CommunityForumFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots) {
                        val post = document.toObject(Post::class.java)
                        postList.add(post)
                    }
                    postAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
