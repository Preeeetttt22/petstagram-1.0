package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
// REMOVED the incorrect BuildConfig import
import com.example.petstagram_1.databinding.FragmentAiAssistantBinding
import com.example.petstagram_1.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class AiAssistantFragment : Fragment() {

    private var _binding: FragmentAiAssistantBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var generativeModel: GenerativeModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // --- Initialize the Gemini Model ---
        // IMPORTANT: Replace "YOUR_API_KEY" with the key you generated
        val apiKey = "AIzaSyARvTeF1IHS-hZuLrYSm3SzG9axVbI09qw"
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = apiKey
        )

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter(messageList)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Add user message to the list
            val userMessage = ChatMessage(messageText, isUser = true)
            messageList.add(userMessage)
            updateChat()
            binding.etMessage.text?.clear()

            // Show a loading indicator (optional, but good UX)
            val loadingMessage = ChatMessage("Thinking...", isUser = false)
            messageList.add(loadingMessage)
            updateChat()

            // --- Call the Gemini AI API ---
            lifecycleScope.launch {
                try {
                    val response = generativeModel.generateContent(messageText)
                    // Remove the "Thinking..." message
                    messageList.removeLast()
                    // Add the real AI response
                    response.text?.let {
                        val aiMessage = ChatMessage(it, isUser = false)
                        messageList.add(aiMessage)
                        updateChat()
                    }
                } catch (e: Exception) {
                    // Handle errors
                    messageList.removeLast() // Remove "Thinking..."
                    val errorMessage = ChatMessage("Error: ${e.message}", isUser = false)
                    messageList.add(errorMessage)
                    updateChat()
                }
            }
        }
    }

    // Helper function to update the RecyclerView
    private fun updateChat() {
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
