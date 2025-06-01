package com.example.myapplication.MainActivity

import ProductAutoCompleteAdapter
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.SalesAdapter
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModels.ProductModel
import com.example.myapplication.ViewModels.SalesModel
import com.example.myapplication.ViewModels.StockModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class SalesActivity : NavActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SalesAdapter
    private val salesList = mutableListOf<SalesModel>()
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sales_date_page)
        firebaseAuth = FirebaseAuth.getInstance()

        val salesDateTextView = findViewById<TextView>(R.id.salesDate)
        val currentDate = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.getDefault())
        salesDateTextView.text = currentDate.format(java.util.Date())

        // RecyclerView setup
        recyclerView = findViewById(R.id.salesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter
        adapter = SalesAdapter(salesList)
        recyclerView.adapter = adapter

        // Fetch sales data from Firebase

        val uid = firebaseAuth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("sales")
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
                // 🔽 SORT BY DATE DESCENDING (newest first)
                salesList.sortByDescending { it.date }

                adapter.notifyDataSetChanged()  // Update RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        val fab: View = findViewById(R.id.fab_add_sale)
        fab.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.sales_input_popup, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            val dateInput = dialogView.findViewById<EditText>(R.id.input_date)
            // Set today's date as default
            val today = Calendar.getInstance()
            val year = today.get(Calendar.YEAR)
            val month = today.get(Calendar.MONTH)
            val day = today.get(Calendar.DAY_OF_MONTH)

            val formattedToday = String.format("%04d-%02d-%02d", year, month + 1, day)
            dateInput.setText(formattedToday)

            val quantityInput = dialogView.findViewById<EditText>(R.id.input_quantity)
            val submitBtn = dialogView.findViewById<Button>(R.id.submit_sale_btn)

            val menuInput = dialogView.findViewById<AutoCompleteTextView>(R.id.input_menu_name)

// Load product list from Firebase or hardcode from your JSON
            val productList = mutableListOf<ProductModel>()
            val uid = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("ingredients")
            val productsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("menu")

            productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()
                    for (productSnap in snapshot.children) {
                        val product = productSnap.getValue(ProductModel::class.java)
                        if (product != null) {
                            // Default to key as title if title is empty
                            if (product.title.isEmpty()) {
                                product.title = productSnap.key ?: ""
                            }
                            productList.add(product)
                        }
                    }

                    val adapter = ProductAutoCompleteAdapter(this@SalesActivity, productList)
                    menuInput.setAdapter(adapter)
                }

                override fun onCancelled(error: DatabaseError) {
                    // handle error
                }
            })


            // Set up date picker
            dateInput.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePicker = DatePickerDialog(this, { _, y, m, d ->
                    val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    dateInput.setText(formattedDate)
                }, year, month, day)

                datePicker.show()
            }

            submitBtn.setOnClickListener {
                val date = dateInput.text.toString().trim()
                val menuItemName = menuInput.text.toString().trim()
                val quantity = quantityInput.text.toString().trim().toIntOrNull() ?: 0

                if (date.isNotEmpty() && menuItemName.isNotEmpty() && quantity > 0) {
                    val database = FirebaseDatabase.getInstance()
                    val uid = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
                    val ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("ingredients")
                    val salesRef = database.getReference("users").child(uid).child("sales")
                    val menuRef = database.getReference("users").child(uid).child("menu")
                    val ingredientsRef = database.getReference("users").child(uid).child("ingredients")

                    // Step 1: Record the sale
                    salesRef.child(date).child(menuItemName)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val existingQuantity = snapshot.getValue(Int::class.java) ?: 0
                                val newTotal = existingQuantity + quantity

                                salesRef.child(date).child(menuItemName).setValue(newTotal)
                                    .addOnSuccessListener {
                                        alertDialog.dismiss()

                                        // Now handle ingredient deduction as before
                                        menuRef.child(menuItemName)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val product = snapshot.getValue(ProductModel::class.java)
                                                    if (product != null && product.ingredients.isNotEmpty()) {
                                                        for ((ingredientName, amountPerItem) in product.ingredients) {
                                                            val totalUsed = amountPerItem * quantity

                                                            ingredientsRef.child(ingredientName).child("stock")
                                                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                                                    override fun onDataChange(stockSnap: DataSnapshot) {
                                                                        val currentStock = stockSnap.getValue(Int::class.java) ?: 0
                                                                        val newStock = (currentStock - totalUsed).coerceAtLeast(0)

                                                                        ingredientsRef.child(ingredientName).child("stock")
                                                                            .setValue(newStock)
                                                                    }

                                                                    override fun onCancelled(error: DatabaseError) {
                                                                        // Handle error
                                                                    }
                                                                })
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    // Handle error
                                                }
                                            })
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                }
            }

        }

    }
}
