package com.example.myapplication.Misc

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ItemsActivity.ProductActivity
import com.example.myapplication.ItemsActivity.StockActivity
import com.example.myapplication.ItemsActivity.StockModelActivity
import com.example.myapplication.ViewModels.ProductModel
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.databinding.ModelProductBinding
import com.example.myapplication.databinding.ModelStockBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(
    private var items: MutableList<ProductModel>,
    private val onItemClick: (ProductModel) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var context: Context

    class ProductViewHolder(val binding: ModelProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        context = parent.context
        firebaseAuth = FirebaseAuth.getInstance()
        val binding = ModelProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = items[position]

        // Set title and ID
        holder.binding.productTitle.text = item.title
        holder.binding.productId.text = item.id

        // Load image if available
        Glide.with(context)
            .load(item.image ?: "")
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.binding.productImage)

        // On click - open StockActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("product_id", item.id)
                putExtra("product_title", item.title)
                putExtra("product_image", item.image ?: "")
            }
            context.startActivity(intent)
        }

        // Delete button click opens StockModelActivity (for deletion)
        holder.binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    val uid = firebaseAuth.currentUser?.uid ?: return@setPositiveButton
                    val ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("menu")
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
                .setNegativeButton("Cancel", null) // do nothing if user cancels
                .show()
        }

    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: MutableList<ProductModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }


}
