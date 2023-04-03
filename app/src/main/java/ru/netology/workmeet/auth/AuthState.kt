package ru.netology.workmeet.auth

data class AuthState (
    val id: Long = 0,
    val token: String? = null
)