package com.example.myapplication.Misc

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ViewModels.MenuModel
import com.example.myapplication.databinding.ModelProductBinding

class ProductAdapter(private val items: MutableList<MenuModel>) : RecyclerView.Adapter<ProductAdapter.MenuViewHolder>() {
    lateinit var context: Context
    class MenuViewHolder(val binding: ModelProductBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        context = parent.context
        val binding = ModelProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.binding.textView.text = items[position].id.toString()
        holder.binding.textView2.text = items[position].title
    }

    override fun getItemCount(): Int = items.size


}
