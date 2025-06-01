package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StockFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()
    private lateinit var addButton: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.stock_page, container, false)

        recyclerView = view.findViewById(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

        adapter = StockAdapter(stockList) { selectedStock ->
            val intent = Intent(requireContext(), StockActivity::class.java)
            intent.putExtra("stock_id", selectedStock.id)
            intent.putExtra("stock_title", selectedStock.title)
            intent.putExtra("stock_quantity", selectedStock.stock)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        addButton = view.findViewById(R.id.addStockButton)
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), StockActivity::class.java)
            intent.putExtra("stock_id", "")
            intent.putExtra("stock_title", "")
            intent.putExtra("stock_quantity", 0)
            startActivity(intent)
        }

        val uid = firebaseAuth.currentUser?.uid ?: return null
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("ingredients")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(StockModel::class.java)
                    item?.let {
                        it.title = itemSnapshot.key ?: ""
                        stockList.add(it)
                    }
                }
                adapter.setItems(stockList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle error
            }
        })

        return view
    }
}
