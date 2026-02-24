package com.example.smartremainder

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartremainder.ui.ChatScreen
import com.example.smartremainder.ui.theme.HumbleBotTheme

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HumbleBotTheme {
                ChatScreen()
            }
        }
    }
}
