package com.example.myapplication.Misc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ViewModels.SalesModel
import java.time.LocalDate

class ReportViewModel : ViewModel() {

    private val _selectedItemSales = MutableLiveData<List<SalesModel>>()
    val selectedItemSales: LiveData<List<SalesModel>> get() = _selectedItemSales

    // Map<String, Int> -> e.g., "Banana Berries" -> [("2025-05-29", 1), ("2025-05-30", 5)]
    fun loadSalesForItem(title: String, allSales: Map<String, SalesModel>) {
        val today = LocalDate.now()
        val lastWeek = today.minusDays(6)

        val filteredSales = mutableListOf<SalesModel>()

        for ((dateStr, salesModel) in allSales) {
            val date = LocalDate.parse(dateStr)
            if (date.isAfter(lastWeek.minusDays(1)) && date.isBefore(today.plusDays(1))) {
                val quantity = salesModel.menu[title] ?: 0
                if (quantity > 0) {
                    // Create a new SalesModel with just the filtered menu for the title
                    val filteredMenu = mapOf(title to quantity)
                    filteredSales.add(SalesModel(date = dateStr, menu = filteredMenu))
                }
            }
        }

        // Sort by date
        _selectedItemSales.value = filteredSales.sortedBy { LocalDate.parse(it.date) }
    }

}
