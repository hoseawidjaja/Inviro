package com.example.myapplication.ItemsActivity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.myapplication.Misc.StockAdapter
import com.example.myapplication.Misc.StockUsageAdapter
import com.example.myapplication.Misc.SupabaseClient
import com.example.myapplication.R
import com.example.myapplication.ViewModels.ProductModel
import com.example.myapplication.ViewModels.StockModel
import com.example.myapplication.ViewModels.StockUsageModel
import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.log
import com.bumptech.glide.request.target.Target
import com.example.myapplication.Misc.StockAutoCompleteAdapter


class ProductActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var deleteButton: Button
    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockUsageAdapter
    private val stockList = mutableListOf<StockUsageModel>()
    private lateinit var addIngredientButton: Button


    private lateinit var imagePreview: ImageView
    private var imageUri: Uri? = null

    private var productId: String = ""  // This is the current Firebase key (title)
    private var productImage: String = ""
    private var productTitle: String = ""  // This is the title entered by the user

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("ProductActivity", "Picked image URI: $uri")
            imageUri = uri
            Glide.with(this).load(uri).into(imagePreview)
            uploadImageToSupabaseAndSaveUrl(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        titleEditText = findViewById(R.id.titleEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        imagePreview = findViewById(R.id.imagePreview)
        stockRecyclerView = findViewById(R.id.ingredientsRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)
        stockAdapter = StockUsageAdapter(stockList, object : StockUsageAdapter.OnIngredientActionListener {
            override fun onDelete(position: Int) {
                val ingredient = stockList[position]
                android.app.AlertDialog.Builder(this@ProductActivity)
                    .setTitle("Delete Ingredient")
                    .setMessage("Are you sure you want to remove \"${ingredient.title}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        stockList.removeAt(position)
                        stockAdapter.notifyItemRemoved(position)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onEditAmount(position: Int) {
                val ingredient = stockList[position]
                val input = EditText(this@ProductActivity)
                input.hint = "Enter new amount"
                input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

                android.app.AlertDialog.Builder(this@ProductActivity)
                    .setTitle("Edit Amount for ${ingredient.title}")
                    .setView(input)
                    .setPositiveButton("Update") { _, _ ->
                        val newAmount = input.text.toString().toIntOrNull()
                        if (newAmount != null) {
                            ingredient.amountNeeded = newAmount
                            stockAdapter.notifyItemChanged(position)
                        } else {
                            Toast.makeText(this@ProductActivity, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        })

        stockRecyclerView.adapter = stockAdapter


        // Set up the unit spinner with the array from resources
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.stock_units_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            unitSpinner.adapter = adapter
//        }

        // Get data passed from previous activity
        productId = intent.getStringExtra("product_id") ?: ""
        productTitle = intent.getStringExtra("product_title") ?: ""  // Save the title initially
        productImage = intent.getStringExtra("product_image") ?: ""

        // Pre-fill fields if editing
        titleEditText.setText(productTitle)

        // Load existing image if available
        if (productImage.isNotBlank()) {
            Glide.with(this@ProductActivity)
                .load(productImage)
                .override(512, 512) // downsample large images
                .placeholder(R.drawable.ic_image_ingredient_placeholder)
                .error(R.drawable.ic_image_ingredient_placeholder)
                .into(imagePreview)

        } else {
            imagePreview.setImageResource(R.drawable.ic_image_ingredient_placeholder)
        }


        // Set up image upload button
        uploadImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = FirebaseDatabase.getInstance().getReference("menu")

            if (productId.isEmpty()) {
                // Asynchronously generate unique ID
                generateUniqueId { newId ->

                    val ingredientsMap = stockList.associate { it.title to it.amountNeeded }

                    val newProduct = ProductModel(
                        id = newId,
                        image = productImage,
                        title = newTitle
                    )

                    ref.child(newTitle).setValue(newProduct)
                        .addOnSuccessListener {
                            ref.child(newTitle).child("ingredients").setValue(ingredientsMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Product and ingredients saved", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                        }

                }
            } else {
                // Existing product update logic stays the same...
                if (newTitle != productTitle) {
                    val updatedProduct = ProductModel(
                        id = productId,
                        image = productImage,
                        title = newTitle
                    )
                    ref.child(productTitle).removeValue().addOnSuccessListener {
                        ref.child(newTitle).setValue(updatedProduct)
                        Toast.makeText(this, "Title updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to update title", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val ingredientsMap = stockList.associate { it.title to it.amountNeeded }

                    val updates = mapOf(
                        "image" to productImage,
                        "ingredients" to ingredientsMap
                    )

                    ref.child(productTitle).updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show()
                        }
                }

            }
        }

        backButton.setOnClickListener {
            finish()
        }

        addIngredientButton = findViewById(R.id.addIngredientButton)

        addIngredientButton.setOnClickListener {
            showAddIngredientDialog()
        }


        if (productTitle.isNotEmpty()) {
            loadStockUsedInProduct(productTitle)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
        try {
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun uploadImageToSupabaseAndSaveUrl(uri: Uri) {
        val file = uriToFile(uri)
        if (file == null) {
            Toast.makeText(this, "Failed to prepare image file", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate upload path inside bucket
        val uploadPath = "images/IMG_${System.currentTimeMillis()}.jpg"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call your Java method
                val supabaseUrl = SupabaseClient.uploadImage(this@ProductActivity, file, uploadPath)

                withContext(Dispatchers.Main) {
                    // Update your local image URL and UI
                    productImage = supabaseUrl
                    Glide.with(this@ProductActivity)
                        .load(supabaseUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imagePreview)
                    Toast.makeText(this@ProductActivity, "Image uploaded to Supabase! ${productImage}", Toast.LENGTH_SHORT).show()
                    Log.d("ProductActivity", "Loaded image URL: $productImage")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    // Function to generate an 8-character alphanumeric ID
    private fun generateUniqueId(callback: (String) -> Unit) {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val ref = FirebaseDatabase.getInstance().getReference("menu") // or "menu", depending on your use case

        fun tryGenerate() {
            val newId = (1..8)
                .map { charset.random() }
                .joinToString("")

            ref.orderByChild("id").equalTo(newId).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        // ID is unique
                        callback(newId)
                    } else {
                        // ID already exists, try again
                        tryGenerate()
                    }
                }
                .addOnFailureListener { error ->
                    // Handle error gracefully (fallback or show message)
                    Log.e("ID_GEN", "Error checking ID: ${error.message}")
                }
        }

        tryGenerate()
    }

    private fun loadStockUsedInProduct(productTitle: String) {
        val menuRef = FirebaseDatabase.getInstance().getReference("menu").child(productTitle)
        val stockRef = FirebaseDatabase.getInstance().getReference("ingredients")

        menuRef.child("ingredients").get()
            .addOnSuccessListener { ingredientsSnapshot ->
                val ingredientMap = mutableMapOf<String, Int>()

                for (child in ingredientsSnapshot.children) {
                    val stockTitle = child.key ?: continue
                    val amountNeeded = child.getValue(Int::class.java) ?: continue
                    ingredientMap[stockTitle] = amountNeeded
                }

                stockRef.get().addOnSuccessListener { stockSnapshot ->
                    stockList.clear()

                    for ((stockTitle, amountNeeded) in ingredientMap) {
                        val stockData = stockSnapshot.child(stockTitle)
                        val stockModel = stockData.getValue(StockModel::class.java)

                        if (stockModel != null) {
                            val title = stockModel.title?.takeIf { it.isNotBlank() } ?: stockTitle

                            stockList.add(
                                StockUsageModel(
                                    title = title,
                                    image = stockModel.image,
                                    unit = stockModel.unit,
                                    amountNeeded = amountNeeded
                                )
                            )
                        }
                    }

                    stockAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showAddIngredientDialog() {
        val context = this
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Add Ingredient")

        val inputLayout = layoutInflater.inflate(R.layout.dialog_add_ingredient, null)
        val ingredientInput = inputLayout.findViewById<AutoCompleteTextView>(R.id.ingredientNameEditText)
        val amountInput = inputLayout.findViewById<EditText>(R.id.amountEditText)

        val ingredientsRef = FirebaseDatabase.getInstance().getReference("ingredients")

        ingredientsRef.get().addOnSuccessListener { snapshot ->
            val stockList = snapshot.children.mapNotNull { child ->
                val fallbackKey = child.key ?: return@mapNotNull null
                val stock = child.getValue(StockModel::class.java) ?: return@mapNotNull null

                if (stock.title.isBlank()) stock.title = fallbackKey
                stock
            }

            val adapter = StockAutoCompleteAdapter(context, stockList)
            ingredientInput.setAdapter(adapter)
            ingredientInput.threshold = 1

            ingredientInput.post {
                ingredientInput.dropDownWidth = ingredientInput.width
                ingredientInput.setDropDownAnchor(ingredientInput.id)
            }

            var itemSelected = false

            ingredientInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!itemSelected && s?.length ?: 0 >= ingredientInput.threshold) {
                        ingredientInput.showDropDown()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            ingredientInput.setOnItemClickListener { _, _, position, _ ->
                val selected = adapter.getItem(position)
                ingredientInput.setText(selected?.title ?: "")
                itemSelected = true
            }
        }

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { dialog, _ ->

            val ingredientName = ingredientInput.text.toString().trim()
            val amountStr = amountInput.text.toString().trim()
            val amount = amountStr.toIntOrNull()

            if (ingredientName.isEmpty() || amount == null || amount <= 0) {
                Toast.makeText(context, "Please enter valid ingredient name and amount", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val adapter = ingredientInput.adapter as? StockAutoCompleteAdapter
            val matchedStock = adapter?.items?.find { it.title.equals(ingredientName, ignoreCase = true) }

            if (matchedStock == null) {
                // Ingredient not found — confirm create new
                android.app.AlertDialog.Builder(context)
                    .setTitle("Create New Ingredient")
                    .setMessage("Ingredient \"$ingredientName\" does not exist. Do you want to create it?")
                    .setPositiveButton("Yes") { confirmDialog, _ ->
                        // Create ingredient with default values
                        val newStock = StockModel(
                            id = ingredientName,
                            title = ingredientName,
                            stock = 0,
                            unit = "pcs",
                            image = ""
                        )

                        ingredientsRef.child(ingredientName).setValue(newStock).addOnSuccessListener {
                            // Add to stockList and notify adapter
                            stockList.add(
                                StockUsageModel(
                                    title = ingredientName,
                                    image = "",
                                    unit = "pcs",
                                    amountNeeded = amount
                                )
                            )
                            stockAdapter.notifyItemInserted(stockList.size - 1)
                            Toast.makeText(context, "Ingredient \"$ingredientName\" created and added.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to create ingredient", Toast.LENGTH_SHORT).show()
                        }

                        confirmDialog.dismiss()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { confirmDialog, _ ->
                        confirmDialog.dismiss()
                    }
                    .show()
            } else {
                // Ingredient exists - check duplicate in stockList
                if (stockList.any { it.title.equals(matchedStock.title, ignoreCase = true) }) {
                    Toast.makeText(context, "${matchedStock.title} is already added", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Add existing ingredient
                stockList.add(
                    StockUsageModel(
                        title = matchedStock.title,
                        image = matchedStock.image,
                        unit = matchedStock.unit,
                        amountNeeded = amount
                    )
                )
                stockAdapter.notifyItemInserted(stockList.size - 1)

                // Clear inputs
                ingredientInput.setText("")
                amountInput.setText("")

                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()
        dialog.show()
    }



}

