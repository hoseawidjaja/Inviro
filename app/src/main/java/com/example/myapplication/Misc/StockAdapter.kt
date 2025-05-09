package com.example.myapplication.Misc

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ViewModels.MenuModel
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.databinding.ModelMenuBinding
import com.example.myapplication.databinding.ModelStockBinding

class StockAdapter(private val items: MutableList<StockModel>) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {
    lateinit var context: Context
    class StockViewHolder(val binding: ModelStockBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        context = parent.context
        val binding = ModelStockBinding.inflate(LayoutInflater.from(context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.binding.textView.text = items[position].id.toString()
        holder.binding.textView2.text = items[position].title
    }

    override fun getItemCount(): Int = items.size
}
