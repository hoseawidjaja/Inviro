package com.example.myapplication.Misc

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.ViewModels.MenuModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Context
import com.example.myapplication.ViewModels.StockModel

class MainRepository (context: Context){
    init {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    private val firebaseDatabase=FirebaseDatabase.getInstance()

    fun loadMenu():LiveData<MutableList<MenuModel>>{
        val listData = MutableLiveData<MutableList<MenuModel>>()
        val ref = firebaseDatabase.getReference("Menu_List")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list= mutableListOf<MenuModel>()
                for(childSnapshot in snapshot.children){
                    val item=childSnapshot.getValue(MenuModel::class.java)
                    item?.let{list.add(it)}
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return listData
    }

    fun loadStock():LiveData<MutableList<StockModel>>{
        val listData = MutableLiveData<MutableList<StockModel>>()
        val ref = firebaseDatabase.getReference("Stock_List")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list= mutableListOf<StockModel>()
                for(childSnapshot in snapshot.children){
                    val item=childSnapshot.getValue(StockModel::class.java)
                    item?.let{list.add(it)}
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return listData
    }
}
