package com.example.myapplication.Misc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ViewModels.MenuItemModel
import com.example.myapplication.ViewModels.SalesModel

class SalesAdapter(private val salesList: List<SalesModel>) : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {
    private val expandedPositions = mutableSetOf<Int>()

    inner class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.salesDate)
        val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.salesRecyclerView)
        val toggleIcon: ImageView = itemView.findViewById(R.id.toggleIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sales_menu_page, parent, false)
        return SalesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val currentSales = salesList[position]
        holder.dateTextView.text = currentSales.date

        // Setup inner RecyclerView
        val menuItemList = currentSales.menu.map { MenuItemModel(it.key, it.value) }
        holder.innerRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.innerRecyclerView.adapter = MenuItemAdapter(menuItemList)

        // Check if expanded
        val isExpanded = expandedPositions.contains(position)
        holder.innerRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.toggleIcon.setImageResource(
            if (isExpanded) R.drawable.ic_expand_more else R.drawable.ic_expand_less
        )

        // Toggle on click
        holder.itemView.setOnClickListener {
            toggleExpanded(holder, position)
        }

        holder.toggleIcon.setOnClickListener {
            toggleExpanded(holder, position)
        }
    }

    private fun toggleExpanded(holder: SalesViewHolder, position: Int) {
        val isExpanded = expandedPositions.contains(position)
        if (isExpanded) {
            expandedPositions.remove(position)
            holder.innerRecyclerView.visibility = View.GONE
            holder.toggleIcon.setImageResource(R.drawable.ic_expand_less)
        } else {
            expandedPositions.add(position)
            holder.innerRecyclerView.visibility = View.VISIBLE
            holder.toggleIcon.setImageResource(R.drawable.ic_expand_more)
        }
    }


    override fun getItemCount() = salesList.size
}
