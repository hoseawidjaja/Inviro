package com.example.myapplication.MainActivity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
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
import java.util.Calendar

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

        val fab: View = findViewById(R.id.fab_add_sale)
        fab.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.sales_input_popup, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            val dateInput = dialogView.findViewById<EditText>(R.id.input_date)
            val menuInput = dialogView.findViewById<EditText>(R.id.input_menu_name)
            val quantityInput = dialogView.findViewById<EditText>(R.id.input_quantity)
            val submitBtn = dialogView.findViewById<Button>(R.id.submit_sale_btn)

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
                val menuItem = menuInput.text.toString().trim()
                val quantity = quantityInput.text.toString().trim().toIntOrNull() ?: 0

                if (date.isNotEmpty() && menuItem.isNotEmpty()) {
                    val salesRef = FirebaseDatabase.getInstance().getReference("sales")
                    salesRef.child(date).child(menuItem).setValue(quantity)
                        .addOnSuccessListener {
                            alertDialog.dismiss()
                        }
                }
            }
        }

    }
}
