package com.example.myapplication.Misc

import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.R
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CriticalItemAdapter(private var items: List<String>) :
    RecyclerView.Adapter<CriticalItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.ingredient_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_critical_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

    override fun getItemCount(): Int = items.size

    // Add this method to update items and refresh the list
    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}
