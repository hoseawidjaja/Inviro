package com.example.myapplication.Misc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.HomeActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.UserProfileModel
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class SignupTabFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_tab, container, false)

        val emailEt = view.findViewById<EditText>(R.id.etEmailSignup)
        val passEt = view.findViewById<EditText>(R.id.etPasswordSignup)
        val confirmPassEt = view.findViewById<EditText>(R.id.signup_confirm)
        val signupBtn = view.findViewById<Button>(R.id.btnSignup)

        firebaseAuth = FirebaseAuth.getInstance()

        signupBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()
            val confirmPassword = confirmPassEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.currentUser?.let { saveUserProfileToFirestore(it) }

                        startHomeActivity()
                    } else {
                        val errorMessage = when (val exception = task.exception) {
                            is FirebaseAuthWeakPasswordException -> "Weak password: ${exception.reason}"
                            is FirebaseAuthUserCollisionException -> "This email is already registered"
                            else -> exception?.message ?: "Signup failed"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        return view
    }

    private fun saveUserProfileToFirestore(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userProfile = UserProfileModel(
            username = "",
            email = user.email ?: "",
            address = "",
            dob = "",
            phone = ""
        )

        db.collection("users").document(user.uid).set(userProfile)
    }



    private fun startHomeActivity() {
        Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
