package com.example.myapplication.ViewModels

data class UserProfileModel(
    val username: String = "",
    val email: String = "",
    val address: String = "",
    val dob: String = "",
    val phone: String = "",
    val profileImage: String = ""
) {
    // Helper function to check if profile is complete
    fun isComplete(): Boolean {
        return username.isNotEmpty() && email.isNotEmpty()
    }

    // Helper function to get display name
    fun getDisplayName(): String {
        return if (username.isNotEmpty()) username else "User"
    }
}