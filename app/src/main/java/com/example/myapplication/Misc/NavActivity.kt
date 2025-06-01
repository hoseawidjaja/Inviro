package com.example.myapplication.Misc

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.MainActivity.HomeActivity
import com.example.myapplication.MainActivity.ReportActivity
import com.example.myapplication.MainActivity.SalesActivity
import com.example.myapplication.MainActivity.ManagementActivity
import com.example.myapplication.MainActivity.ProfileActivity
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

open class NavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)  // This must match your layout file

        window.statusBarColor = ContextCompat.getColor(this, R.color.main_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.main_background)

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
        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.indicator))
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
                R.id.nav_stock -> {
                    if (this !is ManagementActivity) {
                        startActivity(Intent(this, ManagementActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_menu -> {
                    if(this !is SalesActivity) {
                        startActivity(Intent(this, SalesActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        startActivity(Intent(this, ProfileActivity::class.java))
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
        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        // Load profile image from FirebaseAuth
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_image_ingredient_placeholder) // fallback image
                .error(R.drawable.ic_image_ingredient_placeholder) // error image
                .circleCrop() // make it round
                .into(profile)
        } else {
            profile.setImageResource(R.drawable.ic_image_ingredient_placeholder)
        }


        // Set the selected item based on the current activity
        val currentItem = when (this) {
            is HomeActivity -> R.id.nav_home
            is ReportActivity -> R.id.nav_report
            is ManagementActivity -> R.id.nav_stock
            is SalesActivity -> R.id.nav_menu
            is ProfileActivity -> R.id.nav_profile
            else -> 0
        }

        if (currentItem != 0) bottomNav.selectedItemId = currentItem
    }
}
