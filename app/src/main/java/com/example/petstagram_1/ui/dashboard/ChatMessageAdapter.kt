package com.example.petstagram_1.ui.dashboard

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout // Import LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.petstagram_1.databinding.ItemChatMessageBinding
import com.example.petstagram_1.models.ChatMessage

class ChatMessageAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding =
            ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.binding.messageText.text = message.message

        val params = holder.binding.messageText.layoutParams as FrameLayout.LayoutParams
        params.gravity = if (message.isUser) Gravity.END else Gravity.START
        holder.binding.messageText.layoutParams = params
    }
}
