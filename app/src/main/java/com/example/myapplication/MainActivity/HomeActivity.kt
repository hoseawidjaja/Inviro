package com.example.myapplication.MainActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity : NavActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        barChart = findViewById(R.id.menuBarChart)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            loadWeeklyMenuSales(userId) { menuSales ->
                updateMenuBarChart(menuSales)
            }
        }


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

    private fun loadWeeklyMenuSales(userId: String, callback: (Map<String, Int>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId/sales")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(6)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuSales = mutableMapOf<String, Int>()

                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue
                    try {
                        val localDate = LocalDate.parse(date, formatter)
                        if (localDate.isBefore(oneWeekAgo) || localDate.isAfter(today)) continue
                    } catch (e: Exception) {
                        continue
                    }

                    val dailySales = dateSnapshot.getValue(object : GenericTypeIndicator<Map<String, Int>>() {}) ?: continue
                    for ((menuItem, qty) in dailySales) {
                        menuSales[menuItem] = menuSales.getOrDefault(menuItem, 0) + qty
                    }
                }

                callback(menuSales)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Error loading weekly menu sales: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    private fun updateMenuBarChart(menuSales: Map<String, Int>) {
        if (menuSales.isEmpty()) return

        val entries = menuSales.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val labels = menuSales.keys.toList()

        val dataSet = BarDataSet(entries, "Weekly Menu Sales").apply {
            color = Color.rgb(104, 241, 175)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.35f
        barChart.data = barData

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelCount = labels.size
            setDrawLabels(true)
            labelRotationAngle = 0f      // ⬅ Keep labels horizontal
            textSize = 9f                // ⬅ Slightly smaller text
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            setAvoidFirstLastClipping(true)
            spaceMin = 0.5f
            spaceMax = 0.5f
            axisMinimum = - 0.5f
            axisMaximum = labels.size - 0.5f
        }


        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false

        barChart.invalidate()
    }

}
