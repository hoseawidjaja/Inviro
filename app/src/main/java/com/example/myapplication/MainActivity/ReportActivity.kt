package com.example.myapplication.MainActivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Misc.ItemAdapter
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.ReportViewModel
import com.example.myapplication.R
import com.example.myapplication.ViewModels.SalesModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ReportActivity : NavActivity() {

    private lateinit var chart: LineChart
    private lateinit var viewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_page)

        chart = findViewById(R.id.lineChart)
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("ReportActivity", "No user logged in. Cannot load sales data.")
            // Optionally: show an error or redirect to login
            return
        }

        val userId = currentUser.uid

        // Load sales from current user's node in Realtime Database
        loadSalesFromRealtimeDatabase(userId) { salesData ->
            val topItems = getTop5ItemsFromLast7Days(salesData)

            val recycler = findViewById<RecyclerView>(R.id.ingredientMenuRecycler)
            recycler.layoutManager = LinearLayoutManager(this)

            val adapter = ItemAdapter(topItems) { title ->
                viewModel.loadSalesForItem(title, salesData)
            }
            recycler.adapter = adapter

            if (topItems.isNotEmpty()) {
                adapter.selectedItem = topItems[0]
                viewModel.loadSalesForItem(topItems[0], salesData)
            }

            viewModel.selectedItemSales.observe(this) { salesList ->
                val selectedItem = adapter.selectedItem ?: return@observe
                updateChart(salesList, selectedItem)
            }
        }
    }

    private fun updateChart(salesList: List<SalesModel>, item: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val displayFormatter = DateTimeFormatter.ofPattern("MM-dd")
        val today = LocalDate.now()

        // Prepare dates for the past 7 days (including today)
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }

        // Map dates to quantities, default 0 if missing
        val quantities = last7Days.map { date ->
            salesList.find { it.date == date.format(formatter) }?.menu?.get(item) ?: 0
        }

        // Create entries with X = index and Y = quantity
        val lineEntries = quantities.mapIndexed { index, qty ->
            Entry(index.toFloat(), qty.toFloat())
        }

        val dataSet = LineDataSet(lineEntries, "Sales for $item")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK

        val lineData = LineData(dataSet)
        chart.data = lineData

        // Set X axis labels to dates (MM-dd)
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(last7Days.map { it.format(displayFormatter) })
        xAxis.granularity = 1f
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

        // Configure Y axis min and max
        val maxQty = quantities.maxOrNull() ?: 0
        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = (maxQty + 2).toFloat() // add some padding

        // Optional: disable right Y axis
        chart.axisRight.isEnabled = false

        chart.invalidate()
    }

    // Modified to accept userId and read sales under /users/{userId}/sales
    private fun loadSalesFromRealtimeDatabase(userId: String, callback: (Map<String, SalesModel>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val salesRef = database.getReference("users").child(userId).child("sales")

        salesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val salesMap = mutableMapOf<String, SalesModel>()

                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue
                    val productsMap = dateSnapshot.getValue(object : GenericTypeIndicator<Map<String, Int>>() {}) ?: continue
                    salesMap[date] = SalesModel(date, productsMap)
                }

                callback(salesMap)  // Return data to caller
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Failed to fetch sales data: ${error.message}")
                callback(emptyMap()) // Return empty map on failure
            }
        })
    }

    private fun getTop5ItemsFromLast7Days(salesData: Map<String, SalesModel>): List<String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(7)

        val recentSales = salesData
            .filterKeys { dateStr ->
                try {
                    val date = LocalDate.parse(dateStr, formatter)
                    !date.isBefore(oneWeekAgo) && !date.isAfter(today)
                } catch (e: Exception) {
                    false
                }
            }
            .flatMap { it.value.menu.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.sum() }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        return recentSales
    }
}
