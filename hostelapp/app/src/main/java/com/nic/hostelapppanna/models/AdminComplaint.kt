package com.nic.hostelapppanna.models

data class AdminComplaint(
    val complaintId: Int,
    val complaintText: String,
    val fullName: String,
    val phoneNumber: String,
    val hostelName: String,
    val hostelLocation: String,
    val status: String,
    val createdAt: String
)