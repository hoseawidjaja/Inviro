package com.example.myapplication.Misc

import android.app.Application
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.R

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.Misc.MainRepository
import com.example.myapplication.ViewModels.MenuModel
import com.example.myapplication.ViewModels.StockModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MainRepository(application)

    fun loadMenu(): LiveData<MutableList<MenuModel>> {
        return repository.loadMenu()
    }

    fun loadStock(): LiveData<MutableList<StockModel>> {
        return repository.loadStock()
    }
}