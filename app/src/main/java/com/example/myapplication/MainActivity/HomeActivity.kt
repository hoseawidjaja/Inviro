package com.example.myapplication.MainActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : NavActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        fetchDataAndCalculateStatus()
        gotoNotifs()
    }

    private fun fetchDataAndCalculateStatus() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HomeActivity", "No logged-in user found.")
            return
        }

        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val ingredients = snapshot.child("ingredients").children.associate {
                val key = it.key ?: ""
                val stockValue = it.child("stock").getValue(Long::class.java)
                val stock = stockValue?.toInt() ?: 0

                key to stock
            }

            val menus = snapshot.child("menu").children.associate { menuSnapshot ->
                val ingredientsMap = menuSnapshot.child("ingredients").children.associate {
                    it.key!! to (it.value as? Long ?: 0L).toInt()
                }
                menuSnapshot.key!! to ingredientsMap
            }

            val sales = snapshot.child("sales").children.associate { saleSnapshot ->
                val menuSales = saleSnapshot.children.associate {
                    it.key!! to (it.value as? Long ?: 0L).toInt()
                }
                saleSnapshot.key!! to menuSales
            }

            // Calculate ingredient consumption
            val ingredientUsage = mutableMapOf<String, Int>()

            // Aggregate sales over last 7 days
            val last7DaysSales = sales.filterKeys { _ -> true } // same as before

            val daysCount = last7DaysSales.size.coerceAtLeast(1) // avoid div by zero

            last7DaysSales.forEach { (_, menuQuantities) ->
                menuQuantities.forEach { (menuName, quantitySold) ->
                    val menuIngredients = menus[menuName]
                    if (menuIngredients != null) {
                        for ((ingredient, amount) in menuIngredients) {
                            ingredientUsage[ingredient] = ingredientUsage.getOrDefault(ingredient, 0) + amount * quantitySold
                        }
                    }
                }
            }

            // Calculate daily average consumption
            val dailyConsumption = ingredientUsage.mapValues { it.value / daysCount }

            // Calculate status per ingredient
            val statusMap = mutableMapOf<String, String>()
            for ((ingredient, stock) in ingredients) {
                val consumption = dailyConsumption[ingredient] ?: 0
                val daysLeft = if (consumption > 0) stock.toDouble() / consumption else Double.MAX_VALUE

                val status = when {
                    daysLeft < 3 -> "critical"
                    daysLeft < 7 -> "need attention"
                    else -> "good"
                }
                statusMap[ingredient] = status
            }

            displayStatuses(statusMap)
        }.addOnFailureListener {
            Log.e("HomeActivity", "Failed to load user data")
        }
    }


    private fun displayStatuses(statusMap: Map<String, String>) {
        // For example, you could display in a RecyclerView or logs here
        statusMap.forEach { (ingredient, status) ->
            Log.d("IngredientStatus", "$ingredient: $status")
            // TODO: Update your UI components to show these statuses
        }
    }

    fun gotoNotifs(){
        val notifs = findViewById<ImageView>(R.id.notification_bell)
        notifs.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }
    }
}
