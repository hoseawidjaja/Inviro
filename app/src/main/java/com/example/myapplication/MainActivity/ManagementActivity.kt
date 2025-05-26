package com.example.myapplication.MainActivity

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R

class ManagementActivity : NavActivity() {

    private lateinit var btnProduct: ImageButton
    private lateinit var btnStock: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_management)

        btnStock = findViewById(R.id.btnStock)
        btnProduct = findViewById(R.id.btnProduct)

        // Load StockFragment by default
        if (savedInstanceState == null) {
            setFragment(StockFragment())
            highlightButton(btnStock)
        }

        btnProduct.setOnClickListener {
            setFragment(ProductFragment())
            highlightButton(btnProduct)
        }

        btnStock.setOnClickListener {
            setFragment(StockFragment())
            highlightButton(btnStock)
        }
    }

    private fun setFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun highlightButton(selected: ImageButton) {
        // Reset both first
        btnProduct.setColorFilter(Color.WHITE)
        btnStock.setColorFilter(Color.WHITE)

        // Highlight selected
        selected.setColorFilter(resources.getColor(R.color.teal_200, theme)) // or any highlight color
    }
}
