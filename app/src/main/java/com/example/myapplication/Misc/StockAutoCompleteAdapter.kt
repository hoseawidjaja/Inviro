package com.example.myapplication.Misc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
import java.util.Locale

class StockAutoCompleteAdapter(
    context: Context,
    val items: List<StockModel>
) : ArrayAdapter<StockModel>(context, android.R.layout.simple_dropdown_item_1line, items),
    Filterable {

    private var filteredItems: List<StockModel> = items

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): StockModel? = filteredItems.getOrNull(position)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filterPattern = constraint?.toString()?.lowercase(Locale.ROOT)?.trim().orEmpty()

                results.values = if (filterPattern.isEmpty()) {
                    items
                } else {
                    items.filter {
                        it.title.lowercase(Locale.ROOT).contains(filterPattern)
                    }
                }
                results.count = (results.values as List<*>).size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as? List<StockModel> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.stock_dropdown_item, parent, false)

        val item = getItem(position)
        val titleTextView = view.findViewById<TextView>(R.id.stockTitle)
        val imageView = view.findViewById<ImageView>(R.id.stockImage)

        titleTextView.text = item?.title ?: ""
        Glide.with(context)
            .load(item?.image)
            .placeholder(R.drawable.ic_image_ingredient_placeholder)
            .into(imageView)

        return view
    }

}
