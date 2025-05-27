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
            .override(210, 210) // downsample large images
//            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imagePreview)

        // Set up image upload button
        uploadImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()
            val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
            val newUnit = unitSpinner.selectedItem.toString()

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = FirebaseDatabase.getInstance().getReference("ingredients")

            if (stockId.isEmpty()) {
                // Generate unique ID asynchronously
                generateUniqueId { newId ->

                    val newStock = StockModel(
                        id = newId,
                        stock = newQuantity,
                        image = stockImage,
                        title = newTitle,
                        unit = newUnit
                    )

                    ref.child(newTitle).setValue(newStock)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Stock added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed to add stock: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                }
            } else {
                if (newTitle != stockTitle) {
                    val updatedStock = StockModel(
                        id = stockId,
                        stock = newQuantity,
                        image = stockImage,
                        title = newTitle,
                        unit = newUnit
                    )
                    ref.child(stockTitle).removeValue().addOnSuccessListener {
                        ref.child(newTitle).setValue(updatedStock)
                        Toast.makeText(this, "Title updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to update title", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val updates = mapOf(
                        "stock" to newQuantity,
                        "image" to stockImage,
                        "unit" to newUnit
                    )
                    ref.child(stockTitle).updateChildren(updates)
                    Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
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
                        .override(210, 210) // downsample large images
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
    private fun generateUniqueId(callback: (String) -> Unit) {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val ref = FirebaseDatabase.getInstance().getReference("ingredients") // or "menu", depending on your use case

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

}

