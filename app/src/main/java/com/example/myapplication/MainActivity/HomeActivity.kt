package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R

class HomeActivity : NavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)
        Log.d("HomeActivity", "HomeActivity has been launched")
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        gotoNotifs()
    }

    fun gotoNotifs(){
        val notifs = findViewById<ImageView>(R.id.notification_bell)
        notifs.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }
    }
}