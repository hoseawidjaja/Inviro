package com.example.myapplication.MainActivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Misc.ItemAdapter
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.Misc.ReportViewModel
import com.example.myapplication.R
import com.example.myapplication.ViewModels.SalesModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ReportActivity : NavActivity() {

    private lateinit var chart: LineChart
    private lateinit var viewModel: ReportViewModel
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_page)

        chart = findViewById(R.id.lineChart)
        barChart = findViewById(R.id.barChart)
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        val menuInput = findViewById<AutoCompleteTextView>(R.id.dropdown_report)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("ReportActivity", "No user logged in.")
            return
        }
        val userId = currentUser.uid
        loadWeeklyMenuSales(userId) { menuSales ->
            updateMenuBarChart(menuSales)
        }

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(6)

        val dateFromText: TextView = findViewById(R.id.date_from_text)
        val dateToText: TextView = findViewById(R.id.date_to_text)

        dateFromText.text = formatter.format(oneWeekAgo)
        dateToText.text = formatter.format(today)

        loadSalesFromRealtimeDatabase(userId) { salesData ->
            val topItems = getTop5ItemsFromLast7Days(salesData)

            val maxQtyPastWeek = getMaxQuantityPast7Days(salesData)
            // Setup recycler
            val recycler = findViewById<RecyclerView>(R.id.ingredientMenuRecycler)
            recycler.layoutManager = LinearLayoutManager(this)
            val itemAdapter = ItemAdapter(topItems) { item ->
                viewModel.loadSalesForItem(item, salesData)
            }
            recycler.adapter = itemAdapter

            if (topItems.isNotEmpty()) {
                itemAdapter.selectedItem = topItems[0]
                viewModel.loadSalesForItem(topItems[0], salesData)
            }

            viewModel.selectedItemSales.observe(this) { salesList ->
                val selectedItem = itemAdapter.selectedItem ?: return@observe
                updateChart(salesList, selectedItem, maxQtyPastWeek)
            }

            // Load menu items into dropdown
            loadMenuItems(userId) { menuList ->
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, menuList)
                menuInput.setAdapter(adapter)

                menuInput.setOnItemClickListener { _, _, position, _ ->
                    val selectedItem = adapter.getItem(position) ?: return@setOnItemClickListener
                    itemAdapter.selectedItem = selectedItem
                    viewModel.loadSalesForItem(selectedItem, salesData)
                }

                // Also handle text typed in manually (after dropdown closes)
                menuInput.setOnDismissListener {
                    val enteredText = menuInput.text.toString().trim()
                    if (enteredText in menuList) {
                        itemAdapter.selectedItem = enteredText
                        viewModel.loadSalesForItem(enteredText, salesData)
                    } else {
                        Log.d("ReportActivity", "Typed menu item '$enteredText' not found in menu.")
                    }
                }
            }
        }
    }

    private fun updateChart(salesList: List<SalesModel>, item: String, maxQtyPastWeek: Int) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val displayFormatter = DateTimeFormatter.ofPattern("MM-dd")
        val today = LocalDate.now()

        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }

        val quantities = last7Days.map { date ->
            salesList.find { it.date == date.format(formatter) }?.menu?.get(item) ?: 0
        }

        val lineEntries = quantities.mapIndexed { index, qty ->
            Entry(index.toFloat(), qty.toFloat())
        }

        val dataSet = LineDataSet(lineEntries, "Sales for $item").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
        }

        chart.data = LineData(dataSet)

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(last7Days.map { it.format(displayFormatter) })
            granularity = 1f
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }

        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = if (maxQtyPastWeek <= 5) 5f else (maxQtyPastWeek + 2).toFloat()
        }

        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    private fun loadSalesFromRealtimeDatabase(userId: String, callback: (Map<String, SalesModel>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId/sales")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val salesMap = mutableMapOf<String, SalesModel>()
                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue
                    val productsMap = dateSnapshot.getValue(object : GenericTypeIndicator<Map<String, Int>>() {}) ?: continue
                    salesMap[date] = SalesModel(date, productsMap)
                }
                callback(salesMap)
                updateSalesMetrics(salesMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Error loading sales: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    private fun getTop5ItemsFromLast7Days(salesData: Map<String, SalesModel>): List<String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(6)

        return salesData
            .filterKeys {
                try {
                    val date = LocalDate.parse(it, formatter)
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
    }

    private fun loadMenuItems(userId: String, callback: (List<String>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId/menu")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { items.add(it) }
                }
                callback(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Error loading menu: ${error.message}")
                callback(emptyList())
            }
        })
    }

    private fun getMaxQuantityPast7Days(salesData: Map<String, SalesModel>): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(7)

        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()).format(formatter) }

        var maxQty = 0
        for (date in last7Days) {
            val salesForDate = salesData[date]?.menu ?: continue
            val maxForDate = salesForDate.values.maxOrNull() ?: 0
            if (maxForDate > maxQty) maxQty = maxForDate
        }

        return maxQty
    }

    private fun updateSalesMetrics(salesMap: Map<String, SalesModel>) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val last7Days = (0..6).map { today.minusDays(it.toLong()).format(formatter) }

        var totalItemsSold = 0
        val menuSet = mutableSetOf<String>()

        for (date in last7Days) {
            val sales = salesMap[date]?.menu ?: continue
            totalItemsSold += sales.values.sum()
            menuSet.addAll(sales.keys)
        }

        findViewById<TextView>(R.id.items_sold_count).text = "$totalItemsSold items"
        findViewById<TextView>(R.id.revenue_amount).text = "${menuSet.size} menus"
    }

    private fun loadWeeklyMenuSales(userId: String, callback: (Map<String, Int>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId/sales")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(6)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuSales = mutableMapOf<String, Int>()

                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue
                    try {
                        val localDate = LocalDate.parse(date, formatter)
                        if (localDate.isBefore(oneWeekAgo) || localDate.isAfter(today)) continue
                    } catch (e: Exception) {
                        continue
                    }

                    val dailySales = dateSnapshot.getValue(object : GenericTypeIndicator<Map<String, Int>>() {}) ?: continue
                    for ((menuItem, qty) in dailySales) {
                        menuSales[menuItem] = menuSales.getOrDefault(menuItem, 0) + qty
                    }
                }

                callback(menuSales)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Error loading weekly menu sales: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    private fun updateMenuBarChart(menuSales: Map<String, Int>) {
        if (menuSales.isEmpty()) return

        val entries = menuSales.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val labels = menuSales.keys.toList()

        val dataSet = BarDataSet(entries, "Weekly Menu Sales").apply {
            color = Color.rgb(104, 241, 175)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.35f
        barChart.data = barData

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelCount = labels.size
            setDrawLabels(true)
            labelRotationAngle = 0f
            textSize = 9f
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            setAvoidFirstLastClipping(true)
            spaceMin = 0.5f
            spaceMax = 0.5f
            axisMinimum = -0.5f
            axisMaximum = labels.size - 0.5f
        }

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.invalidate()
    }

}
