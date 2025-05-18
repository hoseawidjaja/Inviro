package com.example.myapplication.ItemsActivity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class StockActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private var stockId: String = ""  // This is the current Firebase key (title)
    private var stockQuantity: Int = 0
    private var stockImage: String = ""
    private var stockTitle: String = ""  // This is the title entered by the user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        titleEditText = findViewById(R.id.titleEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Get data passed from previous activity
        stockId = intent.getStringExtra("stock_id") ?: ""
        stockTitle = intent.getStringExtra("stock_title") ?: ""  // Save the title initially
        stockQuantity = intent.getIntExtra("stock_quantity", 0)
        stockImage = intent.getStringExtra("stock_image") ?: ""

        // Pre-fill fields if editing
        titleEditText.setText(stockTitle)
        quantityEditText.setText(stockQuantity.toString())

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()  // Get the new title entered by the user
            val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            if (newTitle.isEmpty()) {
                // Prevent saving empty titles
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = FirebaseDatabase.getInstance().getReference("ingredients")

            if (stockId.isEmpty()) {
                // Create a new ingredient with the title as the key
                val newId = generateRandomId()  // Generate 8-character alphanumeric ID
                val newStock = StockModel(
                    id = newId,  // Use the generated ID as the stock ID
                    stock = newQuantity,
                    image = stockImage,
                    title = newTitle  // Set title here
                )
                ref.child(newTitle).setValue(newStock)  // Save the new stock with title as the key
            } else {
                if (newTitle != stockTitle) {
                    // Title has changed, so we need to update the key in Firebase
                    // Keep the old ID, and just update the title in the key
                    val updatedStock = StockModel(
                        id = stockId,  // Keep the old ID (do not generate new ID)
                        stock = newQuantity,
                        image = stockImage,
                        title = newTitle  // Update the title
                    )
                    ref.child(stockTitle).removeValue().addOnSuccessListener {
                        // After deleting the old entry, add the new entry with updated title
                        ref.child(newTitle).setValue(updatedStock)
                        Toast.makeText(this, "Title updated successfully", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to update title", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If title hasn't changed, just update the stock data
                    val updates = mapOf(
                        "stock" to newQuantity,
                        "image" to stockImage
                    )
                    ref.child(stockId).updateChildren(updates)  // Use stockId as the key (title)
                    Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show()
                }
            }

            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    // Function to generate an 8-character alphanumeric ID
    private fun generateRandomId(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { charset[Random.nextInt(charset.length)] }
            .joinToString("")
    }
}
