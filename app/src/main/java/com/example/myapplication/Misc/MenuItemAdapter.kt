package com.example.myapplication.Misc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ViewModels.MenuItemModel

class MenuItemAdapter(private val menuItemList: List<MenuItemModel>) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.menuItemTitle)
        val countTextView: TextView = itemView.findViewById(R.id.menuItemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.model_product_item, parent, false)
        return MenuItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val currentItem = menuItemList[position]
        holder.titleTextView.text = currentItem.title
        holder.countTextView.text = currentItem.count.toString()
    }

    override fun getItemCount() = menuItemList.size
}
