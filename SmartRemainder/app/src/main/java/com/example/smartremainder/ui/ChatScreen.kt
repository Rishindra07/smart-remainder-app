package com.example.smartremainder.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartremainder.viewmodel.ChatViewModel

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    var message by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { chatMessage ->
                val author = if (chatMessage.isFromUser) "Me" else "Bot"
                Text(text = "$author: ${chatMessage.text}")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                label = { Text("Type a message") },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        chatViewModel.sendMessage(message)
                        message = ""
                    }
                },
                enabled = !isLoading
            ) {
                Text("Send")
            }
        }
    }
}
