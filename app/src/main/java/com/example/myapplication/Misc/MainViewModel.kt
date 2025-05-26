package com.example.myapplication.Misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.LiveData
import com.example.myapplication.ViewModels.ProductModel
import com.example.myapplication.ViewModels.StockModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MainRepository(application)

    fun loadMenu(): LiveData<MutableList<ProductModel>> {
        return repository.loadMenu()
    }

    fun loadStock(): LiveData<MutableList<StockModel>> {
        return repository.loadStock()
    }
}