package com.example.myapplication.MainActivity

import android.os.Bundle
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R

class ProfileActivity : NavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)  // Create this layout XML file
    }
}
