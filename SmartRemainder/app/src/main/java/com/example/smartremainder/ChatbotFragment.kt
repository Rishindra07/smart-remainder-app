package com.example.smartremainder

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.smartremainder.R

class ChatbotFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val chatLayout = view.findViewById<LinearLayout>(R.id.chatLayout)

        // Initial bot greeting
        val initBotMsg = TextView(requireContext())
        initBotMsg.text = "Hi 👋 How can I assist you today?"
        initBotMsg.setBackgroundResource(R.drawable.bot_bubble)
        initBotMsg.setPadding(20, 10, 20, 10)
        val initParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        initParams.setMargins(8, 8, 8, 8)
        initParams.gravity = Gravity.START
        initBotMsg.layoutParams = initParams
        chatLayout.addView(initBotMsg)

        btnSend.setOnClickListener {
            val userMsg = etMessage.text.toString().trim()
            if (userMsg.isNotEmpty()) {
                val userTextView = TextView(requireContext())
                userTextView.text = userMsg
                userTextView.setBackgroundResource(R.drawable.user_bubble)
                userTextView.setPadding(20, 10, 20, 10)
                val userParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                userParams.setMargins(8, 8, 8, 8)
                userParams.gravity = Gravity.END
                userTextView.layoutParams = userParams
                chatLayout.addView(userTextView)
                etMessage.setText("")

                // Gemini API call
                GeminiService.sendMessage(userMsg) { reply ->
                    activity?.runOnUiThread {
                        val botTextView = TextView(requireContext())
                        botTextView.text = reply
                        botTextView.setBackgroundResource(R.drawable.bot_bubble)
                        botTextView.setPadding(20, 10, 20, 10)
                        val botParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        botParams.setMargins(8, 8, 8, 8)
                        botParams.gravity = Gravity.START
                        botTextView.layoutParams = botParams
                        chatLayout.addView(botTextView)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
