package com.example.myapplication.Misc

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.databinding.ModelStockBinding

class StockAdapter(
    private var items: MutableList<StockModel>,
    private val onItemClick: (StockModel) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    lateinit var context: Context

    class StockViewHolder(val binding: ModelStockBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        context = parent.context
        val binding = ModelStockBinding.inflate(LayoutInflater.from(context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]

        // Bind the stock data to the UI elements
        holder.binding.textView.text = item.id  // Displaying the ID (which is the title)
        holder.binding.textView2.text = item.title  // Displaying the title

        // Set an onClickListener to open the StockActivity when an item is clicked
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StockActivity::class.java)
            intent.putExtra("stock_id", item.id)
            intent.putExtra("stock_title", item.title)
            intent.putExtra("stock_quantity", item.stock)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    // Method to update the list and notify adapter about the change
    fun setItems(newItems: MutableList<StockModel>) {
        this.items = newItems
        notifyDataSetChanged()  // Refresh the RecyclerView
    }
}
