package com.example.myapplication.ItemsActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        stockAdapter = StockUsageAdapter(stockList)
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
        Glide.with(this@ProductActivity)
            .load(productImage)
//            .placeholder(R.drawable.ic_image_ingredient_placeholder)
//            .error(R.drawable.ic_image_ingredient_placeholder)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imagePreview)

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

                    val newProduct = ProductModel(
                        id = newId,
                        image = productImage,
                        title = newTitle
                    )

                    ref.child(newTitle).setValue(newProduct)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                            finish()  // Close activity after saving
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed to add product: ${error.message}", Toast.LENGTH_LONG).show()
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
                    val updates = mapOf(
                        "image" to productImage,
                    )
                    ref.child(productTitle).updateChildren(updates)
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
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
                            val title = if (stockModel.title.isNotBlank()) stockModel.title else stockTitle

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
        val ingredientInput = inputLayout.findViewById<EditText>(R.id.ingredientNameEditText)
        val amountInput = inputLayout.findViewById<EditText>(R.id.amountEditText)

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { dialog, _ ->
            val ingredientName = ingredientInput.text.toString().trim()
            val amount = amountInput.text.toString().toIntOrNull()

            if (ingredientName.isEmpty() || amount == null) {
                Toast.makeText(context, "Please enter valid name and amount", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val ingredientsRef = FirebaseDatabase.getInstance().getReference("ingredients")
            ingredientsRef.child(ingredientName).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Ingredient exists, add to RecyclerView
                    val stock = snapshot.getValue(StockModel::class.java)
                    if (stock != null) {
                        stockList.add(
                            StockUsageModel(
                                title = stock.title,
                                image = stock.image,
                                unit = stock.unit,
                                amountNeeded = amount
                            )
                        )
                        stockAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Ingredient doesn't exist
                    val confirmBuilder = android.app.AlertDialog.Builder(context)
                    confirmBuilder.setTitle("Create New Ingredient?")
                    confirmBuilder.setMessage("Ingredient \"$ingredientName\" does not exist. Create it?")

                    confirmBuilder.setPositiveButton("Yes") { _, _ ->
                        generateUniqueId { newId ->
                            val newStock = StockModel(
                                id = newId,
                                title = ingredientName,
                                stock = 0,
                                unit = "pcs",
                                image = ""
                            )

                            ingredientsRef.child(ingredientName).setValue(newStock).addOnSuccessListener {
                                stockList.add(
                                    StockUsageModel(
                                        title = newStock.title,
                                        image = newStock.image,
                                        unit = newStock.unit,
                                        amountNeeded = amount
                                    )
                                )
                                stockAdapter.notifyDataSetChanged()
                                Toast.makeText(context, "Ingredient created", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                    confirmBuilder.setNegativeButton("No", null)
                    confirmBuilder.show()
                }
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

}

