package com.nic.hostelapppanna.models

data class Complaint(
    val complaintId: Int,
    val complaintText: String,
    val userId: Int,
    val createdAt: String,
    val status: String
)