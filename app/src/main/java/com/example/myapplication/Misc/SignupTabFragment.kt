package com.example.myapplication.Misc

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.HomeActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignupTabFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

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
                        Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
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
}
