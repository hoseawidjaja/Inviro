package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.*

class StockManagementActivity : NavActivity() {

    private lateinit var adapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_page)

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter with lambda click listener
        adapter = StockAdapter(stockList) { selectedItem ->
            val intent = Intent(this, StockActivity::class.java).apply {
                putExtra("menu_id", selectedItem.id)
                putExtra("menu_title", selectedItem.title)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("Stock_List")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(StockModel::class.java)
                    item?.let { stockList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error or show a toast
            }
        })
    }
}
