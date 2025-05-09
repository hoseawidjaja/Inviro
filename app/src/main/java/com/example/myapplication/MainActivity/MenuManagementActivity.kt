package com.example.myapplication.MainActivity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Misc.MenuAdapter
import com.example.myapplication.Misc.NavActivity
import com.example.myapplication.ViewModels.MenuModel
import com.google.firebase.database.*
import com.example.myapplication.R

class MenuManagementActivity : NavActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuAdapter
    private val menuList = mutableListOf<MenuModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        recyclerView = findViewById(R.id.views)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MenuAdapter(menuList)
        recyclerView.adapter = adapter

        val databaseRef = FirebaseDatabase.getInstance().getReference("Menu_List")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(MenuModel::class.java)
                    item?.let { menuList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
