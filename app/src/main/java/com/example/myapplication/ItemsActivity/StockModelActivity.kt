package com.example.myapplication.ItemsActivity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.database.FirebaseDatabase

class StockModelActivity : AppCompatActivity() {
    private lateinit var deleteButton: ImageButton
    private var stockId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.model_stock)

        // Get stockId passed from previous activity
        stockId = intent.getStringExtra("stock_id") ?: ""

        deleteButton = findViewById(R.id.deleteButton)
        if (deleteButton == null) {
            Toast.makeText(this, "Delete button not found in layout!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        deleteButton.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("ingredients")

            val query = ref.orderByChild("id").equalTo(stockId)

            query.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                                finish() // close activity after delete
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(this, "Delete failed: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Item not found for deletion", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { error ->
                Toast.makeText(this, "Error during query: ${error.message}", Toast.LENGTH_LONG).show()
            }

        }
    }
}
