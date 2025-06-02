package com.example.myapplication.Misc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ItemAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    var selectedItem: String? = null

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.itemName)

        init {
            view.setOnClickListener {
                val title = name.text.toString()
                selectedItem = title
                onItemClick(title)
                notifyDataSetChanged() // to refresh any UI changes for selection (optional)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.name.text = items[position]

        // Optional: highlight selected item visually
        if (items[position] == selectedItem) {
//            holder.name.setBackgroundColor(0xFF8692F7.toInt()) // lavender
            holder.name.setTextColor(0xFF1171FF.toInt())
        } else {
//            holder.name.setBackgroundColor(0x00000000) // transparent
            holder.name.setTextColor(0xFF000000.toInt())

        }
    }

    override fun getItemCount(): Int = items.size
}
