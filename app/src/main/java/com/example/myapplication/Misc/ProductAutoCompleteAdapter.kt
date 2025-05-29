import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.myapplication.R
import com.example.myapplication.ViewModels.ProductModel
import com.squareup.picasso.Picasso

class ProductAutoCompleteAdapter(
    context: Context,
    private val products: List<ProductModel>
) : ArrayAdapter<ProductModel>(context, 0, products), Filterable {

    private var filteredItems: List<ProductModel> = products

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): ProductModel? = filteredItems[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.menu_dropdown_item, parent, false)

        val product = getItem(position)
        val textView = view.findViewById<TextView>(R.id.menu_name)
        val imageView = view.findViewById<ImageView>(R.id.menu_image)

        textView.text = product?.title ?: ""

        if (!product?.image.isNullOrEmpty()) {
            Picasso.get()
                .load(product?.image)
                .placeholder(R.drawable.circle_red)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.circle_red)
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filtered = if (constraint.isNullOrEmpty()) {
                    products
                } else {
                    val query = constraint.toString().lowercase()
                    products.filter { it.title.lowercase().contains(query) }
                }

                results.values = filtered
                results.count = filtered.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = if (results?.values is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    results.values as List<ProductModel>
                } else {
                    products
                }
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? ProductModel)?.title ?: ""
            }
        }
    }
}
