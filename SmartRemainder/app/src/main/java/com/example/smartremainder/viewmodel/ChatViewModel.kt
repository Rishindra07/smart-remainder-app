package com.example.smartremainder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartremainder.model.ChatMessage
import com.example.smartremainder.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val userMessage = ChatMessage(text, true)
            _messages.value = _messages.value + userMessage

            val result = chatRepository.sendMessage(text)
            result.fold(
                onSuccess = { reply ->
                    val botMessage = ChatMessage(reply, false)
                    _messages.value = _messages.value + botMessage
                },
                onFailure = {
                    val errorMessage = ChatMessage("Error: ${it.message}", false)
                    _messages.value = _messages.value + errorMessage
                }
            )
            _isLoading.value = false
        }
    }
}