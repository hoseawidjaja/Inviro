package com.example.myapplication.ItemsActivity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.FirebaseDatabase

class StockActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private lateinit var stockModel: StockModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        titleEditText = findViewById(R.id.titleEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Get full StockModel object
        stockModel = intent.getSerializableExtra("stock_data") as? StockModel ?: StockModel()

        // Pre-fill fields
        titleEditText.setText(stockModel.title)
        quantityEditText.setText(stockModel.quantity.toString())

        // Save button logic
        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            val ref = FirebaseDatabase.getInstance().getReference("Stock_List")
                .child(stockModel.id.toString())

            val updates = mapOf(
                "title" to newTitle,
                "quantity" to newQuantity
            )

            ref.updateChildren(updates)
                .addOnSuccessListener {
                    finish()
                }
        }

        // Back button logic
        backButton.setOnClickListener {
            finish()
        }
    }
}
