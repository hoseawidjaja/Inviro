package com.example.myapplication.Misc

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.R
import com.example.myapplication.ViewModels.MenuItemModel
import com.example.myapplication.ViewModels.SalesModel
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.databinding.ModelStockBinding

class SalesAdapter(private val salesList: List<SalesModel>) : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    inner class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.salesDate)
        val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.menuItemsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sales_menu_page, parent, false)
        return SalesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val currentSales = salesList[position]
        holder.dateTextView.text = currentSales.date

        // Prepare the menu items for this date and set up the inner RecyclerView
        val menuItemList = currentSales.menu.map { MenuItemModel(it.key, it.value) }
        holder.innerRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.innerRecyclerView.adapter = MenuItemAdapter(menuItemList)
    }

    override fun getItemCount() = salesList.size
}
