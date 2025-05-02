package com.example.myapplication.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    private val splashTime: Long=2000 //2 detik splash screen delay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val btnGoToMain: Button = findViewById(R.id.button)

        // Set onClickListener to the button
//        btnGoToMain.setOnClickListener {
//            // Create an intent to go to MainActivity
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish() // Finish SplashActivity so user can't return to it
//        }
//        binding.startButton.setOnClickListener{
//
//        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.startButton)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashTime)

    }
}