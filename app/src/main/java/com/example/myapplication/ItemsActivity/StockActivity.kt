package com.example.myapplication.ItemsActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityStockBinding

class StockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up back button click
        binding.backButton.setOnClickListener {
            finish() // Go back to previous Activity
        }

        // Get the data from intent
        val id = intent.getIntExtra("menu_id", -1)
        val title = intent.getStringExtra("menu_title")

        // Display the data
        binding.titleTextViewX.text = "ID: $id\nTitle: $title"
    }
}
