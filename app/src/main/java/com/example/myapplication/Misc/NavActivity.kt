package com.example.myapplication.Misc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity.HomeActivity
import com.example.myapplication.MainActivity.NotificationActivity
import com.example.myapplication.MainActivity.ReportActivity
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

open class NavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)  // This must match your layout file

        // Print or log when the navigation is created
        println(">> NavActivity created")
        Log.d("NavActivity", "Navigation is now active")
    }

    override fun setContentView(layoutResID: Int) {
        // Inflate the base layout that contains the bottom navigation and content frame
        val baseLayout = layoutInflater.inflate(R.layout.activity_nav, null)
        val contentFrame = baseLayout.findViewById<FrameLayout>(R.id.contentFrame)

        // Inflate the specific layout for the activity into the content frame
        layoutInflater.inflate(layoutResID, contentFrame, true)

        super.setContentView(baseLayout)

        val nav = baseLayout.findViewById<BottomNavigationView>(R.id.navigs)
        setupNavigation(nav)
    }

    private fun setupNavigation(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_report -> {
                    if (this !is ReportActivity) {
                        startActivity(Intent(this, ReportActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

        val appTitle = findViewById<TextView>(R.id.app_title)
        appTitle.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val profile = findViewById<ImageView>(R.id.profile_button)
        //TODO: ini dijadiin profile pas menunya udah ada
        profile.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
            finish()
        }


        // Set the selected item based on the current activity
        val currentItem = when (this) {
            is HomeActivity -> R.id.nav_home
            is ReportActivity -> R.id.nav_report
            else -> 0
        }

        if (currentItem != 0) bottomNav.selectedItemId = currentItem
    }
}
