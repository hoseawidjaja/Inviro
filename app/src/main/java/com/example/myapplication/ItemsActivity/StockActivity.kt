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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myapplication.Misc.SupabaseClient
import com.example.myapplication.R
import com.example.myapplication.ViewModels.StockModel
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


class StockActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var unitSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var deleteButton: Button

    private lateinit var imagePreview: ImageView
    private var imageUri: Uri? = null

    private var stockId: String = ""  // This is the current Firebase key (title)
    private var stockQuantity: Int = 0
    private var stockImage: String = ""
    private var stockTitle: String = ""  // This is the title entered by the user
    private var stockUnit: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("StockActivity", "Picked image URI: $uri")
            imageUri = uri
            Glide.with(this).load(uri).into(imagePreview)
            uploadImageToSupabaseAndSaveUrl(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        titleEditText = findViewById(R.id.titleEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        unitSpinner = findViewById(R.id.unitSpinner)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        imagePreview = findViewById(R.id.imagePreview)

        // Set up the unit spinner with the array from resources
        ArrayAdapter.createFromResource(
            this,
            R.array.stock_units_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = adapter
        }

        // Get data passed from previous activity
        stockId = intent.getStringExtra("stock_id") ?: ""
        stockTitle = intent.getStringExtra("stock_title") ?: ""  // Save the title initially
        stockQuantity = intent.getIntExtra("stock_quantity", 0)
        stockImage = intent.getStringExtra("stock_image") ?: ""
        stockUnit = intent.getStringExtra("stock_unit") ?: ""

        // Pre-fill fields if editing
        titleEditText.setText(stockTitle)
        quantityEditText.setText(stockQuantity.toString())

        // Set the spinner to the saved unit if it exists
        if (stockUnit.isNotEmpty()) {
            val unitsArray = resources.getStringArray(R.array.stock_units_array)
            val position = unitsArray.indexOf(stockUnit)
            if (position >= 0) {
                unitSpinner.setSelection(position)
            }
        }

        // Load existing image if available
        Glide.with(this@StockActivity)
            .load(stockImage)
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
            val newTitle = titleEditText.text.toString().trim()  // Get the new title entered by the user
            val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
            val newUnit = unitSpinner.selectedItem.toString()

            if (newTitle.isEmpty()) {
                // Prevent saving empty titles
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = FirebaseDatabase.getInstance().getReference("ingredients")

            if (stockId.isEmpty()) {
                // Create a new ingredient with the title as the key
                val newId = generateRandomId()  // Generate 8-character alphanumeric ID
                val newStock = StockModel(
                    id = newId,  // Use the generated ID as the stock ID
                    stock = newQuantity,
                    image = stockImage,
                    title = newTitle,  // Set title here
                    unit = newUnit  // Add unit here
                )
                ref.child(newTitle).setValue(newStock)  // Save the new stock with title as the key
            } else {
                if (newTitle != stockTitle) {
                    // Title has changed, so we need to update the key in Firebase
                    // Keep the old ID, and just update the title in the key
                    val updatedStock = StockModel(
                        id = stockId,  // Keep the old ID (do not generate new ID)
                        stock = newQuantity,
                        image = stockImage,
                        title = newTitle,  // Update the title
                        unit = newUnit  // Add unit here
                    )
                    ref.child(stockTitle).removeValue().addOnSuccessListener {
                        // After deleting the old entry, add the new entry with updated title
                        ref.child(newTitle).setValue(updatedStock)
                        Toast.makeText(this, "Title updated successfully", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to update title", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If title hasn't changed, just update the stock data
                    val updates = mapOf(
                        "stock" to newQuantity,
                        "image" to stockImage,
                        "unit" to newUnit  // Add unit here
                    )
                    ref.child(stockTitle).updateChildren(updates)  // Use the title as the key
                    Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show()
                }
            }

            finish()
        }

        backButton.setOnClickListener {
            finish()
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
                val supabaseUrl = SupabaseClient.uploadImage(this@StockActivity, file, uploadPath)

                withContext(Dispatchers.Main) {
                    // Update your local image URL and UI
                    stockImage = supabaseUrl
                    Glide.with(this@StockActivity)
                        .load(supabaseUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imagePreview)
                    Toast.makeText(this@StockActivity, "Image uploaded to Supabase! ${stockImage}", Toast.LENGTH_SHORT).show()
                    Log.d("StockActivity", "Loaded image URL: $stockImage")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StockActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    // Function to generate an 8-character alphanumeric ID
    private fun generateRandomId(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { charset[Random.nextInt(charset.length)] }
            .joinToString("")
    }

}

