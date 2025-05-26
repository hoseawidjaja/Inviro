package com.example.myapplication.Misc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockUsageModel

class StockUsageAdapter(
    private val items: MutableList<StockUsageModel>,
    private val listener: OnIngredientActionListener
) : RecyclerView.Adapter<StockUsageAdapter.StockViewHolder>() {

    interface OnIngredientActionListener {
        fun onDelete(position: Int)
        fun onEditAmount(position: Int)
    }

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.titleTextView)
        val amountView: TextView = view.findViewById(R.id.amountTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_used, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = items[position]
        holder.titleView.text = stock.title
        holder.amountView.text = "${stock.amountNeeded} ${stock.unit}"
        Glide.with(holder.imageView.context)
            .load(stock.image)
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            listener.onDelete(position)
        }

        holder.amountView.setOnClickListener {
            listener.onEditAmount(position)
        }
    }

    override fun getItemCount() = items.size
}
