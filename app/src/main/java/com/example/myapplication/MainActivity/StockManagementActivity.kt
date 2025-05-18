package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.*

class StockManagementActivity : NavActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()
    private lateinit var addButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_page)

        // RecyclerView setup
        recyclerView = findViewById(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with click listener
        adapter = StockAdapter(stockList) { selectedStock ->
            val intent = Intent(this, StockActivity::class.java)
            intent.putExtra("stock_id", selectedStock.id)
            intent.putExtra("stock_title", selectedStock.title)
            intent.putExtra("stock_quantity", selectedStock.stock)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Setup the (+) button for adding a new stock item
        addButton = findViewById(R.id.addStockButton)
        addButton.setOnClickListener {
            val intent = Intent(this, StockActivity::class.java)
            intent.putExtra("stock_id", "")  // Indicate it's a new item (no ID assigned yet)
            intent.putExtra("stock_title", "") // Empty title for new item
            intent.putExtra("stock_quantity", 0) // Set quantity to 0 for new item
            startActivity(intent)
        }

        // Fetch data from Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("ingredients")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(StockModel::class.java)
                    item?.let {
                        it.title = itemSnapshot.key ?: ""  // Set the title to the Firebase key
                        stockList.add(it)
                    }
                }
                adapter.setItems(stockList)  // Update RecyclerView with the new data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
