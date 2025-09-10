package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petstagram_1.databinding.FragmentAiAssistantBinding
import com.example.petstagram_1.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ServerException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.petstagram_1.BuildConfig

class AiAssistantFragment : Fragment() {

    private var _binding: FragmentAiAssistantBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messageList = mutableListOf<ChatMessage>()

    // Make it nullable to avoid crash if not initialized
    private var generativeModel: GenerativeModel? = null

    // Constants for the retry logic
    private val MAX_RETRIES = 3
    private val INITIAL_DELAY = 2000L // 2 seconds

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

        val apiKey: String = BuildConfig.GEMINI_API_KEY

        // ✅ Initialize model safely
        if (apiKey.isNotEmpty()) {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = apiKey
            )
        } else {
            messageList.add(ChatMessage("Error: API key is missing!", isUser = false))
            updateChat()
        }

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
            val userMessage = ChatMessage(messageText, isUser = true)
            messageList.add(userMessage)
            updateChat()
            binding.etMessage.text?.clear()

            // Show a loading indicator
            val loadingMessage = ChatMessage("Thinking...", isUser = false)
            messageList.add(loadingMessage)
            updateChat()

            // --- Call the Gemini AI API with Retry Logic ---
            lifecycleScope.launch {
                val model = generativeModel
                if (model == null) {
                    messageList.removeLast()
                    messageList.add(ChatMessage("Error: AI model not initialized", isUser = false))
                    updateChat()
                    return@launch
                }

                var response: GenerateContentResponse? = null
                var finalError: Exception? = null

                for (attempt in 1..MAX_RETRIES) {
                    try {
                        response = model.generateContent(messageText)
                        // If successful, break the loop
                        break
                    } catch (e: ServerException) {
                        finalError = e
                        messageList[messageList.size - 1] =
                            ChatMessage("Server busy, retrying ($attempt/$MAX_RETRIES)...", false)
                        updateChat()
                        delay(INITIAL_DELAY * attempt)
                    } catch (e: Exception) {
                        finalError = e
                        break
                    }
                }

                // --- Process the final result ---
                messageList.removeLast()
                if (response != null) {
                    response.text?.let {
                        val aiMessage = ChatMessage(it, isUser = false)
                        messageList.add(aiMessage)
                    }
                } else {
                    val errorMessage =
                        ChatMessage("Error: ${finalError?.message}", isUser = false)
                    messageList.add(errorMessage)
                }
                updateChat()
            }
        }
    }

    private fun updateChat() {
        activity?.runOnUiThread {
            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
