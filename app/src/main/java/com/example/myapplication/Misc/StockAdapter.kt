package com.example.myapplication.Misc

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.ItemsActivity.StockModelActivity
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.databinding.ModelStockBinding
import com.google.firebase.database.FirebaseDatabase

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

        // Set title and ID
        holder.binding.ingredientTitle.text = item.title
        holder.binding.ingredientId.text = item.id
        holder.binding.ingredientQuantity.text = item.stock.toString()

        // Set unit if available
        holder.binding.ingredientUnit.text = item.unit ?: "No unit"

        // Load image if available
        Glide.with(context)
            .load(item.image ?: "")
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.binding.ingredientImage)

        // On click - open StockActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StockActivity::class.java).apply {
                putExtra("stock_id", item.id)
                putExtra("stock_title", item.title)
                putExtra("stock_quantity", item.stock)
                putExtra("stock_image", item.image ?: "")
                putExtra("stock_unit", item.unit ?: "")
            }
            context.startActivity(intent)
        }

        // Delete button click opens StockModelActivity (for deletion)
        holder.binding.deleteButton.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("ingredients")
            val query = ref.orderByChild("id").equalTo(item.id)

            query.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(context, "Delete failed: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "Item not found for deletion", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { error ->
                Toast.makeText(context, "Error during query: ${error.message}", Toast.LENGTH_LONG).show()
            }

        }

    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: MutableList<StockModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }


}
