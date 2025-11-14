package com.nic.hostelapppanna.models

data class User(
    val userId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val hostelId: String,
    val hostelName: String,
    val hostelLocation: String
)