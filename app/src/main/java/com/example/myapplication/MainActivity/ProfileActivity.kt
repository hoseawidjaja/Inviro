package com.example.myapplication.MainActivity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.Misc.LoginTabFragment
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.UserProfileModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : NavActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var usernameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var addressEt: EditText
    private lateinit var dobEt: EditText

    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var logoutButton: Button

    private lateinit var originalData: UserProfileModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up click listeners
        setupClickListeners()

        // Load user profile data
        loadUserProfile()
    }

    private fun initializeViews() {
        usernameEt = findViewById(R.id.username_edit_text)
        emailEt = findViewById(R.id.email_edit_text)
        phoneEt = findViewById(R.id.phone_edit_text)
        addressEt = findViewById(R.id.address_edit_text)
        dobEt = findViewById(R.id.dob_edit_text)

        editButton = findViewById(R.id.edit_profile_button)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        logoutButton = findViewById(R.id.logout_button)

        // Initially hide save and cancel buttons
        saveButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
    }

    private fun setupClickListeners() {
        editButton.setOnClickListener {
            toggleEditing(true)
        }

        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            // Restore original data and disable editing
            setProfileData(originalData)
            toggleEditing(false)
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfileModel::class.java)
                    profile?.let { user ->
                        originalData = user
                        setProfileData(user)
                    }
                } else {
                    // Create default profile if doesn't exist
                    val currentUser = firebaseAuth.currentUser
                    val defaultProfile = UserProfileModel(
                        username = currentUser?.displayName ?: "",
                        email = currentUser?.email ?: "",
                        address = "",
                        dob = "",
                        phone = ""
                    )
                    originalData = defaultProfile
                    setProfileData(defaultProfile)

                    // Save default profile to Firestore
                    db.collection("users").document(uid).set(defaultProfile)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setProfileData(profile: UserProfileModel) {
        usernameEt.setText(profile.username)
        emailEt.setText(profile.email)
        addressEt.setText(profile.address)
        dobEt.setText(profile.dob)
        phoneEt.setText(profile.phone)
    }

    private fun saveProfile() {
        val updatedProfile = UserProfileModel(
            username = usernameEt.text.toString().trim(),
            email = emailEt.text.toString().trim(),
            address = addressEt.text.toString().trim(),
            dob = dobEt.text.toString().trim(),
            phone = phoneEt.text.toString().trim()
        )

        // Basic validation
        if (updatedProfile.username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (updatedProfile.email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = firebaseAuth.currentUser?.uid ?: return

        db.collection("users").document(uid).set(updatedProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                originalData = updatedProfile
                toggleEditing(false)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleEditing(enabled: Boolean) {
        // Enable/disable EditTexts
        usernameEt.isEnabled = enabled
        emailEt.isEnabled = false // Keep email disabled as it's usually not editable
        phoneEt.isEnabled = enabled
        addressEt.isEnabled = enabled
        dobEt.isEnabled = enabled

        // Show/hide buttons
        editButton.visibility = if (enabled) View.GONE else View.VISIBLE
        saveButton.visibility = if (enabled) View.VISIBLE else View.GONE
        cancelButton.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        firebaseAuth.signOut()

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener {
            // Redirect to login
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

}