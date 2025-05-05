package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Activity.ReportActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.navigs)
        bottomNav.setBackgroundColor(Color.TRANSPARENT)
        bottomNav.elevation = 0f

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_report -> {
                    Toast.makeText(this, "Report Clicked", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stock -> {
                    Toast.makeText(this, "Stock Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_menu -> {
                    Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

    }
}