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
import com.example.myapplication.ItemsActivity.ProductActivity
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.ProductAdapter
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.ProductModel
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<ProductModel>()
    private lateinit var addButton: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.product_page, container, false)

        recyclerView = view.findViewById(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

        adapter = ProductAdapter(productList) { selectedProduct ->
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("product_id", selectedProduct.id)
            intent.putExtra("product_title", selectedProduct.title)
            intent.putExtra("product_image", selectedProduct.image)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        addButton = view.findViewById(R.id.addStockButton)
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("product_id", "")
            intent.putExtra("product_title", "")
            intent.putExtra("product_image", "")
            startActivity(intent)
        }

        val uid = firebaseAuth.currentUser?.uid ?: return null
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("menu")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ProductModel::class.java)
                    item?.let {
                        it.title = itemSnapshot.key ?: ""
                        productList.add(it)
                    }
                }
                adapter.setItems(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle error
            }
        })

        return view
    }
}
