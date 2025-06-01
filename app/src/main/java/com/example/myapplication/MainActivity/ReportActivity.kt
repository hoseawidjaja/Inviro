package com.example.myapplication.MainActivity

import android.graphics.Color
import android.os.Bundle
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
import java.time.LocalDate

class ReportActivity : NavActivity() {

    private lateinit var chart: LineChart
    private lateinit var viewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_page)

        chart = findViewById(R.id.lineChart)
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        val salesData = loadSalesFromJson()

        val recycler = findViewById<RecyclerView>(R.id.ingredientMenuRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val items = extractMenuTitles(salesData)
        val adapter = ItemAdapter(items) { title ->
            viewModel.loadSalesForItem(title, salesData)
        }
        recycler.adapter = adapter

        // Select first item by default
        if (items.isNotEmpty()) {
            adapter.selectedItem = items[0]
            viewModel.loadSalesForItem(items[0], salesData)
        }

        viewModel.selectedItemSales.observe(this) { salesList ->
            val selectedItem = adapter.selectedItem ?: ""
            updateChart(salesList, selectedItem)
        }
    }

    private fun updateChart(salesList: List<SalesModel>, items: String) {
        val lineEntries = salesList.mapIndexed { index, sale ->
            val quantity = sale.menu[items] ?: 0
            Entry(index.toFloat(), quantity.toFloat())
        }

        val dataSet = LineDataSet(lineEntries, "Sales for $items")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun loadSalesFromJson(): Map<String, SalesModel> {
        return mapOf(
            "2025-05-29" to SalesModel("2025-05-29", mapOf("Banana Berries" to 1)),
            "2025-05-30" to SalesModel("2025-05-30", mapOf("Banana Choco" to 5)),
            "2025-05-26" to SalesModel("2025-05-26", mapOf("Jus Jeruk" to 4, "Banana Berry" to 2))
        )
    }

    private fun extractMenuTitles(salesData: Map<String, SalesModel>): List<String> {
        return salesData.values.flatMap { it.menu.keys }.distinct()
    }
}
