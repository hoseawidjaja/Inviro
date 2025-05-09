package com.example.myapplication.MainActivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Misc.MenuAdapter
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.MenuModel
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StockManagementActivity : NavActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        recyclerView = findViewById(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StockAdapter(stockList)
        recyclerView.adapter = adapter

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
                // Handle error
            }
        })
    }
}