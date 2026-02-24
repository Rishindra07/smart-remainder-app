package com.example.smartremainder

data class UserProfile(
    val id: Int = 0,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String? = null
)
