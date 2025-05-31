package com.example.myapplication.Misc

import android.content.Intent
import android.os.Bundle
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

class LoginTabFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_tab, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        emailEt = view.findViewById(R.id.etEmail)
        passEt = view.findViewById(R.id.etPassword)
        loginButton = view.findViewById(R.id.btnLogin)

        loginButton.setOnClickListener {
            val email = emailEt.text.toString()
            val pass = passEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
