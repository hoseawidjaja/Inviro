package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.SalesAdapter
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.SalesModel
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.*

class SalesActivity : NavActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SalesAdapter
    private val salesList = mutableListOf<SalesModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sales_date_page)

        // RecyclerView setup
        recyclerView = findViewById(R.id.salesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter
        adapter = SalesAdapter(salesList)
        recyclerView.adapter = adapter

        // Fetch sales data from Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("sales")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                salesList.clear()
                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: ""
                    val menu = mutableMapOf<String, Int>()
                    for (menuSnapshot in dateSnapshot.children) {
                        val menuName = menuSnapshot.key ?: ""
                        val count = menuSnapshot.getValue(Int::class.java) ?: 0
                        menu[menuName] = count
                    }
                    val salesModel = SalesModel(date, menu)
                    salesList.add(salesModel)
                }
                adapter.notifyDataSetChanged()  // Update RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
