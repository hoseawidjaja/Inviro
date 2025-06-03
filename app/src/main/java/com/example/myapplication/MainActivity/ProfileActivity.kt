package com.example.myapplication.MainActivity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.UserProfileModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.bumptech.glide.Glide

class ProfileActivity : NavActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient


    // Display elements
    private lateinit var profileImage: ImageView
    private lateinit var usernameDisplay: TextView
    private lateinit var emailDisplay: TextView
    private lateinit var phoneDisplay: TextView
    private lateinit var addressDisplay: TextView
    private lateinit var dobDisplay: TextView

    // Edit form elements
    private lateinit var usernameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var addressEt: EditText
    private lateinit var dobEt: EditText

    // Buttons
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var logoutButton: Button

    private lateinit var originalData: UserProfileModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        initializeViews()
        setupGoogleSignIn()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initializeViews() {
        // Display elements
        profileImage = findViewById(R.id.profile_image)
        usernameDisplay = findViewById(R.id.username_display)
        emailDisplay = findViewById(R.id.email_display)
        phoneDisplay = findViewById(R.id.phone)
        addressDisplay = findViewById(R.id.address)
        dobDisplay = findViewById(R.id.dob)

        // Edit form elements
        usernameEt = findViewById(R.id.username_edit_text)
        emailEt = findViewById(R.id.email_edit_text)
        phoneEt = findViewById(R.id.phone_edit_text)
        addressEt = findViewById(R.id.address_edit_text)
        dobEt = findViewById(R.id.dob_edit_text)

        // Buttons
        editButton = findViewById(R.id.edit_profile_button)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        logoutButton = findViewById(R.id.logout_button)

        // Initial state - editing disabled
        toggleEditing(false)
        emailEt.isEnabled = false // Email is always read-only
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        editButton.setOnClickListener {
            toggleEditing(true)
        }

        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            // Restore original data and exit edit mode
            setFormData(originalData)
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

        Log.d("ProfileActivity", "Loading profile for UID: $uid")

        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profile = snapshot.getValue(UserProfileModel::class.java)
                    profile?.let {
                        originalData = it
                        updateUI(it)
                        Log.d("ProfileActivity", "Loaded profile: $it")
                    } ?: run {
                        Log.e("ProfileActivity", "Failed to parse profile data")
                        createDefaultProfile(uid)
                    }
                } else {
                    Log.d("ProfileActivity", "No profile found, creating default profile")
                    createDefaultProfile(uid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Failed to load profile", error.toException())
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load profile: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                // Create default profile even if loading fails
                createDefaultProfile(uid)
            }
        })
    }

    private fun createDefaultProfile(uid: String) {
        val currentUser = firebaseAuth.currentUser
        val defaultProfile = UserProfileModel(
            username = currentUser?.displayName ?: "User",
            email = currentUser?.email ?: "",
            address = "",
            dob = "",
            phone = "",
            profileImage = currentUser?.photoUrl?.toString() ?: ""
        )

        originalData = defaultProfile
        updateUI(defaultProfile)

        // Save default profile to Realtime Database
        usersRef.child(uid).setValue(defaultProfile)
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Default profile saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Failed to save default profile", exception)
            }
    }

    private fun updateUI(profile: UserProfileModel) {
        // Update display elements
        usernameDisplay.text = if (profile.username.isNotEmpty()) profile.username else "No username"
        emailDisplay.text = if (profile.email.isNotEmpty()) profile.email else "No email"
        phoneDisplay.text = if (profile.phone.isNotEmpty()) profile.phone else "No phone"
        addressDisplay.text = if (profile.address.isNotEmpty()) profile.address else "No address"
        dobDisplay.text = if (profile.dob.isNotEmpty()) profile.dob else "No date of birth"

        // Update form elements
        setFormData(profile)

        // Load profile image if available
        if (profile.profileImage.isNotBlank()) {
            Glide.with(this@ProfileActivity)
                .load(profile.profileImage)
                .override(512, 512)
                .placeholder(R.drawable.ic_image_ingredient_placeholder)
                .error(R.drawable.ic_image_ingredient_placeholder)
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.ic_image_ingredient_placeholder)
        }
    }

    private fun setFormData(profile: UserProfileModel) {
        usernameEt.setText(profile.username)
        emailEt.setText(profile.email)
        phoneEt.setText(profile.phone)
        addressEt.setText(profile.address)
        dobEt.setText(profile.dob)
    }

    private fun saveProfile() {
        val username = usernameEt.text.toString().trim()
        val phone = phoneEt.text.toString().trim()
        val address = addressEt.text.toString().trim()
        val dob = dobEt.text.toString().trim()

        if (username.isEmpty()) {
            usernameEt.error = "Username cannot be empty"
            usernameEt.requestFocus()
            return
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Only update specific fields
        val updates = mapOf<String, Any?>(
            "username" to username,
            "phone" to phone,
            "address" to address,
            "dob" to dob
        )

        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        usersRef.child(uid).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                originalData = originalData.copy(
                    username = username,
                    phone = phone,
                    address = address,
                    dob = dob
                )
                updateUI(originalData)
                toggleEditing(false)

                saveButton.isEnabled = true
                saveButton.text = "Save"
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_LONG).show()

                saveButton.isEnabled = true
                saveButton.text = "Save"
            }
    }


    private fun toggleEditing(enabled: Boolean) {
        // Enable/disable form fields (except email which is always disabled)
        usernameEt.isEnabled = enabled
        phoneEt.isEnabled = enabled
        addressEt.isEnabled = enabled
        dobEt.isEnabled = enabled
        emailEt.isEnabled = false // Always disabled

        // Toggle button visibility
        editButton.visibility = if (enabled) View.GONE else View.VISIBLE
        saveButton.visibility = if (enabled) View.VISIBLE else View.GONE
        cancelButton.visibility = if (enabled) View.VISIBLE else View.GONE

        // If entering edit mode, populate form with current data
        if (enabled && ::originalData.isInitialized) {
            setFormData(originalData)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}