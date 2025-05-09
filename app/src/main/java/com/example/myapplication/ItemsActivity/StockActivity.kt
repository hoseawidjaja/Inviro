package com.example.myapplication.ItemsActivity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StockActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private var stockId: Int = -1
    private var stockTitle: String = ""
    private var stockQuantity: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        titleEditText = findViewById(R.id.titleEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Get data passed from previous activity
        stockId = intent.getIntExtra("stock_id", -1)
        stockTitle = intent.getStringExtra("stock_title") ?: ""
        stockQuantity = intent.getIntExtra("stock_quantity", 0)

        // Pre-fill fields if it's an edit or view
        titleEditText.setText(stockTitle)
        quantityEditText.setText(stockQuantity.toString())

        // Save button logic
        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            val ref = FirebaseDatabase.getInstance().getReference("Stock_List")

            if (stockId == -1) {
                // If it's a new stock item, create a new ID (incremental)
                ref.orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newId = (snapshot.children.firstOrNull()?.child("id")?.getValue(Int::class.java) ?: 0) + 1
                        val newStock = StockModel(newId, newTitle, newQuantity)
                        ref.child(newId.toString()).setValue(newStock)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            } else {
                // If editing an existing stock item
                val updates = mapOf(
                    "title" to newTitle,
                    "quantity" to newQuantity
                )
                ref.child(stockId.toString()).updateChildren(updates)
            }

            finish() // Go back after saving
        }

        // Back button logic
        backButton.setOnClickListener {
            finish()
        }
    }
}
